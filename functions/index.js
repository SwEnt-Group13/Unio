
// The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
const { logger } = require("firebase-functions");
const { onRequest } = require("firebase-functions/v2/https");
const nodemailer = require('nodemailer');
const admin = require("firebase-admin");
const { getAuth } = require('firebase-admin/auth');


// The Firebase Admin SDK to access Firestore.
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, Timestamp, FieldValue } = require("firebase-admin/firestore");

const { getMessaging } = require("firebase-admin/messaging");

//Scheduale management
const { onSchedule } = require("firebase-functions/v2/scheduler");

const { defineString } = require('firebase-functions/params')


initializeApp();
const db = getFirestore();
const messaging = getMessaging();

/**
 * Verifies the user's ID token and returns the user's UID or an error.
 *
 * @param {string} tokenId - The ID token of the user.
 * @returns {Promise<string>} - A promise that resolves to the UID of the user or rejects with an error.
 */
async function getCurrentUserUid(tokenId) {
  try {
      if (!tokenId) {
          throw new Error("No token provided");
      }

      const decodedToken = await getAuth().verifyIdToken(tokenId);
      return decodedToken.uid; // Return the UID of the user
  } catch (error) {
      console.error("Error verifying ID token:", error);
      throw new Error("Unauthorized: Invalid token"); // Re-throw the error for the caller to handle
  }
}

/**
 * Function to check if a user has a specific permission.
 * @param {Set} grantedPermissions - Set of the user's permissions.
 * @param {string} requiredPermission - The permission to check.
 * @returns {boolean} - True if the permission is granted, false otherwise.
 */
function hasPermission(grantedPermissions, requiredPermission) {
  console.log("User has permission ? : ", requiredPermission)
  console.log("as his permissions are: ", grantedPermissions)
  return (
      grantedPermissions.has(requiredPermission) ||
      (grantedPermissions.has("Full Rights") && requiredPermission !== "Owner")
  );
}

/**
* Function to hydrate roles from raw data (similar to the Kotlin logic).
* @param {Object} rolesMap - Map of roles with their data.
* @returns {Array} - Array of hydrated roles.
*/
function hydrateRoles(rolesMap) {
  return Object.entries(rolesMap).map(([roleUid, roleData]) => ({
      uid: roleUid,
      displayName: roleData.displayName || "",
      color: roleData.color || 0xFFFF0000,
      permissions: new Set(roleData.permissions || []),
  }));
}

/**
* Function to hydrate members and link roles.
* @param {Object} membersMap - Map of members with their role UIDs.
* @param {Array} roles - Array of hydrated roles.
* @returns {Array} - Array of hydrated members.
*/
function hydrateMembers(membersMap, roles) {
  return Object.entries(membersMap).map(([userUid, roleUid]) => {
      const role = roles.find((r) => r.uid === roleUid) || {
          uid: "GUEST",
          displayName: "Guest",
          permissions: new Set(),
      };
      return { userUid, role };
  });
}

/**
 * Sends a verification email to the user with a 6-digit code.
 */
exports.sendVerificationEmail = onRequest(async (req, res) => {

  const recipientEmail = req.body.data?.email;
  const associationUid = req.body.data?.associationUid;

  // Generate a random 6-digit verification code
  const code = Math.floor(100000 + Math.random() * 900000).toString();

  // Store verification details in Firestore
  await db.collection('emailVerifications').doc(associationUid).set({
    email: recipientEmail,
    code,
    timestamp: Timestamp.now(),
    status: 'pending',
  });

  // Send email with the code
  const mailOptions = {
    from: 'software.entreprise@gmail.com',
    to: recipientEmail,
    subject: 'Your Verification Code',
    text: `Your verification code is ${code}. This code will expire in 10 minutes.`,
  };

  const senderEmail = defineString('FUNCTIONS_COMPANY_EMAIL')
  const password = defineString('FUNCTIONS_COMPANY_PASSWORD')

  const transporter = nodemailer.createTransport({
    service: 'gmail', 
    auth: {
      user: senderEmail.value(), 
      pass: password.value(), 
    },
  });

  try {
    await transporter.sendMail(mailOptions);
    res.json({ data: `Association with ID ${associationUid}, bloublou` });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Email not sent", error: error.message });
  }
});

exports.verifyCode = onRequest(async (req, res) => {
  try {
    const code = req.body.data?.code;
    const associationUid = req.body.data?.associationUid;
    const userUid = req.body.data?.userUid;

    if (!code || !associationUid || !userUid) {
      return res.status(400).json({
        message: "invalid-request",
        error: "Code, associationUid, and userUid are required.",
      });
    }

    const verificationDoc = await db.collection("emailVerifications").doc(associationUid).get();

    if (!verificationDoc.exists) {
      return res.status(404).json({
        message: "not-found",
        error: "Verification document not found.",
      });
    }

    const verificationData = verificationDoc.data();
    const currentTime = Timestamp.now();
    const codeGeneratedTime = verificationData.timestamp;

    if (
      verificationData.code === code &&
      currentTime.seconds - codeGeneratedTime.seconds < 600
    ) {
      await db
        .collection("emailVerifications")
        .doc(associationUid)
        .update({ status: "verified" });

      // Fetch the association document
      const associationDocRef = db.collection("associations").doc(associationUid);
      const associationDoc = await associationDocRef.get();

      if (!associationDoc.exists) {
        return res.status(404).json({
          message: "not-found",
          error: "Association not found.",
        });
      }

      const associationData = associationDoc.data();
      const existingRoles = associationData.roles || {};
      const existingMembers = associationData.members || {};

      // Function to generate an 8-digit unique ID
      const generateUniqueRoleUid = () => {
        return Math.floor(10000000 + Math.random() * 90000000).toString();
      };

      // Generate a unique role ID that doesn't exist in current roles
      let newRoleUid;
      do {
        newRoleUid = generateUniqueRoleUid();
      } while (existingRoles[newRoleUid]);

      // Create the new Owner role
      const ownerRole = {
        displayName: "Owner",
        permissions: ["Owner", "Full Rights"],
        color: 0xFFFF0000, // Default red color
      };

      // Update the roles with the new Owner role
      const updatedRoles = {
        ...existingRoles,
        [newRoleUid]: ownerRole,
      };

      // Update the members to map the user to the new Owner role
      const updatedMembers = {
        ...existingMembers,
        [userUid]: newRoleUid,
      };

      // Update the association document with new roles and members
      await associationDocRef.update({
        roles: updatedRoles,
        members: updatedMembers,
      });

      return res.status(200).json({ data: "Verification successful" });
    } else {
      return res.status(400).json({
        message: "invalid-code",
        error: "The code is invalid or has expired.",
      });
    }
  } catch (error) {
    console.error("Error in verifyCode:", error.message);
    return res.status(500).json({
      message: "server-error",
      error: "An unexpected error occurred.",
    });
  }
});


/**
 * Adds or updates a role in an association in Firestore.
 *
 * @param {Object} role - The role to add or update. Must include `uid`, `displayName`, `permissions` (array), and optionally `color`.
 * @param {Object} associationDocRef - Firestore reference to the association document.
 * @param {boolean} isNewRole - Determines whether the role is new or being updated.
 * @returns {Promise<void>} - Resolves if the role is added/updated successfully, otherwise throws an error.
 * @throws {Error} - If the role data is invalid or if there's a conflict when adding a new role.
 */
async function addOrUpdateRoleInAssociation(role, associationDocRef, isNewRole) {
  if (!role || !role.uid || !role.displayName || !Array.isArray(role.permissions)) {
    console.log("Error: Invalid role data. Role must have `uid`, `displayName`, and `permissions`.");
    throw new Error("Invalid role data. Role must have `uid`, `displayName`, and `permissions`.");
  }

  // Fetch the association document
  const associationDoc = await associationDocRef.get();

  if (!associationDoc.exists) {
    console.log("Error: Association not found.");
    throw new Error("Association not found.");
  }

  const associationData = associationDoc.data();
  const existingRoles = associationData.roles || {};

  if (isNewRole) {
    // Check if the role already exists when adding a new role
    if (existingRoles[role.uid]) {
      console.log("Error: Role with this UID already exists.");
      throw new Error("Role with this UID already exists.");
    }
  } else {
    // Check if the role exists when updating
    if (!existingRoles[role.uid]) {
      console.log("Error: Role with this UID does not exist.");
      throw new Error("Role with this UID does not exist.");
    }
  }

  // Prepare the role data for Firestore
  const updatedRoleData = {
    displayName: role.displayName,
    color: role.color || 0xFFFF0000, // Default color
    permissions: role.permissions,  // List of permissions
  };

  // Update the roles in Firestore
  const updatedRoles = {
    ...existingRoles,
    [role.uid]: updatedRoleData,
  };

  await associationDocRef.update({ roles: updatedRoles });

  console.log(`Role ${role.uid} ${isNewRole ? "added to" : "updated in"} association ${associationDocRef.id}`);
}

// Cloud Function to save or update events.
exports.saveEvent = onRequest(async (req, res) => {
  try {
    const tokenId = req.body.data?.tokenId; // Token ID of the user
    const event = req.body.data?.event; // Event data to save or update
    const associationUid = req.body.data?.associationUid; // Association UID the event is associated with
    const isNewEvent = req.body.data?.isNewEvent; // Boolean indicating whether it's a new event

    if (!tokenId || !event || !associationUid || typeof isNewEvent !== "boolean") {
      return res.status(400).json({ message: "Missing or invalid required parameters" });
    }



    // Convert string back to Firebase Timestamp
  const startDateString = event.startDate;
  const endDateString = event.endDate;

  // Check if the strings are valid dates and convert to Firebase Timestamp
  let startDate, endDate;
  if (startDateString && endDateString) {
    startDate = Timestamp.fromDate(new Date(startDateString));  // Convert string to Date and then to Timestamp
    endDate = Timestamp.fromDate(new Date(endDateString));      // Convert string to Date and then to Timestamp

    console.log('Start Date:', startDate.toDate());  // Log the converted date
    console.log('End Date:', endDate.toDate());      // Log the converted date
  } else {
    throw new functions.https.HttpsError('invalid-argument', 'Start date and end date are required');
  }

  // Ensure the event object is updated with the correct Timestamp fields
  event.startDate = startDate;
  event.endDate = endDate;

    // Get the UID of the current user
    const uid = await getCurrentUserUid(tokenId);

    // Fetch the association document
    const associationDocRef = db.collection("associations").doc(associationUid);
    const associationDoc = await associationDocRef.get();

    if (!associationDoc.exists) {
      return res.status(404).json({ message: "Association not found." });
    }

    const associationData = associationDoc.data();

    // Hydrate roles and members
    const roles = hydrateRoles(associationData.roles || {});
    const members = hydrateMembers(associationData.members || {}, roles);

    // Find the current user and their role
    const currentMember = members.find((member) => member.userUid === uid);
    if (!currentMember) {
      return res.status(403).json({ message: "User is not a member of the association." });
    }

    const userPermissions = currentMember.role.permissions;

    // Check if the user has the required permission
    if (!hasPermission(userPermissions, "Add & Edit Event")) {
      return res.status(403).json({ message: "Permission denied: ADD_EDIT_EVENT required for this association." });
    }

    // Save or update the event
    const eventsCollectionRef = db.collection("events");

    if (isNewEvent) {
      // Save a new event
      const newEventRef = eventsCollectionRef.doc();
      event.uid = newEventRef.id; // Assign UID to the event
      await newEventRef.set(event);

      // Debugging: Check if the association data contains events
      console.log("Association Data:", associationData);

      // Ensure 'events' field exists and is an array, then update the association document
      let currentEvents = associationData.events || []; // Default to an empty array if 'events' doesn't exist
      console.log("Current events in the association:", currentEvents);

      // Check if currentEvents is an array
      if (!Array.isArray(currentEvents)) {
        console.error("Error: 'events' field is not an array");
        return res.status(500).json({ message: "'events' field should be an array." });
      }

      // Now we are safe to use arrayUnion
      console.log("Updating association with event UID:", event.uid);


      // Add the event UID to the association's events array
      await associationDocRef.update({
        events: FieldValue.arrayUnion(event.uid) // Add the event UID to the events array
      });

      return res.status(200).json({
        data: `Event created successfully`,
        eventUid: event.uid,
      });
    } else {
      // Update an existing event
      if (!event.uid) {
        return res.status(400).json({ message: "Event UID is required for updating." });
      }
      const eventDocRef = eventsCollectionRef.doc(event.uid);
      await eventDocRef.update(event);

      return res.status(200).json({
        data: `Event updated successfully`,
        eventUid: event.uid,
      });
    }
  } catch (error) {
    console.error("Error in saveEvent function:", error.message);
    return res.status(500).json({ message: "server-error", error: error.message });
  }
});


// Updated Cloud Function
exports.saveRole = onRequest(async (req, res) => {
  try {
    const tokenId = req.body.data?.tokenId; // Token ID given by the user
    const role = req.body.data?.role; // Role to add or update
    const isNewRole = req.body.data?.isNewRole; // Boolean indicating whether it's a new role
    const associationUid = req.body.data?.associationUid; // Association UID from the client

    if (!tokenId || !role || !associationUid || typeof isNewRole !== "boolean") {
      console.log("is isNewRole a Boolean :", (typeof isNewRole));
      console.log("tokenId : ", tokenId)
      console.log("role : ", role)
      console.log("associationUid : ", associationUid)
      return res.status(400).json({ message: "Missing or invalid required parameters" });
    }

    console.log("Role Data:", role);
    console.log("Association UID:", associationUid);
    console.log("isNewRole:", isNewRole);

    // Get the UID of the current user
    const uid = await getCurrentUserUid(tokenId);
    console.log("User UID:", uid);

    // Fetch the association document reference
    const firestore = getFirestore();
    const associationDocRef = firestore.collection("associations").doc(associationUid);

    // Fetch association data for permission checks
    const associationDoc = await associationDocRef.get();
    if (!associationDoc.exists) {
      console.log("Error: Association not found.");
      return res.status(404).json({ message: "Association not found." });
    }

    const associationData = associationDoc.data();

    // Hydrate roles and members
    const roles = hydrateRoles(associationData.roles || {});
    const members = hydrateMembers(associationData.members || {}, roles);

    // Find the current user and their role
    const currentMember = members.find((member) => member.userUid === uid);
    if (!currentMember) {
      console.log(members);
      console.log("Error: User is not a member of the association.");
      return res.status(403).json({ message: "User is not a member of the association." });
    }

    const userPermissions = currentMember.role.permissions;

    // Check if the user has the required permission
    if (!hasPermission(userPermissions, "Add & Edit Roles")) {
      console.log("Permission denied: ADD_EDIT_ROLES required for this association.");
      return res.status(403).json({ message: "Permission denied: ADD_EDIT_ROLES required for this association." });
    }

    // Add or update the role in the association
    await addOrUpdateRoleInAssociation(role, associationDocRef, isNewRole);
    console.log(`Role ${isNewRole ? "added" : "updated"} successfully.`);
    return res.status(200).json({ data: `Role ${isNewRole ? "added" : "updated"} successfully`, userId: uid });
  } catch (error) {
    console.error("Error in addRole function:", error.message);
    return res.status(500).json({ message: "server-error", error: error.message });
  }
});



/**
 * Broadcasts a message to a topic.
 * 
 * The request body must contain an object with the following properties:
 * - type: The type of message being sent.
 * - topic: The topic to which the message will be sent.
 * - payload: The data to be sent in the message.
 */
exports.broadcastMessage = onRequest(async (req, res) => {
  const type = req.body.data?.type;
  const topic = req.body.data?.topic;
  const payload = req.body.data?.payload;

  if (!type || !topic || !payload) {
    return res.status(400).json({ message: "invalid-request", error: "Type, topic, and payload are required." });
  }

  const response = {
    data: {
      ...payload, type
    },
    topic
  };

  try {
    await messaging.send(response);
    return res.status(200).json({ data: "Message sent successfully" });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "server-error", error: "An unexpected error occurred." });
  }

});

/* PROOF OF CONCEPT, need to upgrade to Blaze plan according to "https://firebase.google.com/docs/functions/schedule-functions?gen=2nd"
exports.cleanupExpiredCodes = onSchedule("every day 18:05", async (event) => {
  const expiredTime = Timestamp.now().seconds - 600; // 10 minutes in seconds
  const expiredDocs = await db.collection('emailVerifications')
    .where('timestamp', '<', expiredTime)
    .where('status', '==', 'pending')
    .get();

  const deletePromises = [];
  expiredDocs.forEach(doc => deletePromises.push(doc.ref.delete()));

  return Promise.all(deletePromises);
});*/


/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */


// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

