
// The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
const { logger } = require("firebase-functions");
const { onRequest } = require("firebase-functions/v2/https");
const nodemailer = require('nodemailer');
const admin = require("firebase-admin");
const { getAuth } = require('firebase-admin/auth');


// The Firebase Admin SDK to access Firestore.
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, Timestamp } = require("firebase-admin/firestore");

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

/**
 * Verifies that the code given by the user is the same that the one sent, and if so, give admin rights of this association to the user.
 */
exports.verifyCode = onRequest(async (req, res) => {
  try {
    const code = req.body.data?.code;
    const associationUid = req.body.data?.associationUid;
    const userUid = req.body.data?.userUid

    if (!code || !associationUid) {
      return res.status(400).json({ message: "invalid-request", error: "Code and associationUid are required." });
    }

    const verificationDoc = await db.collection('emailVerifications').doc(associationUid).get();

    if (!verificationDoc.exists) {
      return res.status(404).json({ message: "not-found", error: "Verification document not found." });
    }

    const verificationData = verificationDoc.data();
    const currentTime = Timestamp.now();
    const codeGeneratedTime = verificationData.timestamp;

    if (verificationData.code === code && currentTime.seconds - codeGeneratedTime.seconds < 600) {
      await db.collection('emailVerifications').doc(associationUid).update({ status: 'verified' });
      await db.collection('associations').doc(associationUid).update({
        adminUid: userUid, // Add user.uid to the admins array
      });
      return res.status(200).json({ data: "Verification successful" });
    } else {
      // This case is specifically for incorrect or expired code
      return res.status(400).json({ message: "invalid-code", error: "The code is invalid or has expired." });
    }
  } catch (error) {
    // General catch-all error handler for unexpected issues
    console.error(error);
    return res.status(500).json({ message: "server-error", error: "An unexpected error occurred." });
  }
});

/**
 * Adds a new role to an association in Firestore.
 *
 * @param {Object} newRole - The role to add. Must include `uid`, `displayName`, `permissions` (array), and optionally `color`.
 * @param {Object} associationDocRef - Firestore reference to the association document.
 * @returns {Promise<void>} - Resolves if the role is added successfully, otherwise throws an error.
 * @throws {Error} - If the role data is invalid or the role already exists.
 */
async function addRoleToAssociation(newRole, associationDocRef) {
  if (!newRole || !newRole.uid || !newRole.displayName || !Array.isArray(newRole.permissions)) {
    console.log("Error : Invalid role data. Role must have `uid`, `displayName`, and `permissions`.");
      throw new Error("Invalid role data. Role must have `uid`, `displayName`, and `permissions`.");
  }

  // Fetch the association document
  const associationDoc = await associationDocRef.get();

  if (!associationDoc.exists) {
    console.log("Error : Association not found.");
      throw new Error("Association not found.");
  }

  const associationData = associationDoc.data();

  // Check if the role already exists
  const existingRoles = associationData.roles || {};
  if (existingRoles[newRole.uid]) {
    console.log("Error : Role with this UID already exists.");
      throw new Error("Role with this UID already exists.");
  }

  // Prepare the new role for Firestore
  const newRoleData = {
      displayName: newRole.displayName,
      color: newRole.color || 0xFFFF0000, // Default color
      permissions: newRole.permissions, // List of permissions
  };

  // Update the roles in Firestore
  const updatedRoles = {
      ...existingRoles,
      [newRole.uid]: newRoleData,
  };

  await associationDocRef.update({ roles: updatedRoles });

  console.log(`New role ${newRole.uid} added to association ${associationDocRef.id}`);
}


exports.addRole = onRequest(async (req, res) => {
  try {
      const tokenId = req.body.data?.tokenId; // Token ID given by the user
      const newRole = req.body.data?.newRole; // Role to add
      const associationUid = req.body.data?.associationUid; // Association UID from the client

      if (!tokenId || !newRole || !associationUid) {
          return res.status(400).json({ message: "Missing required parameters" });
      }

      console.log("New Role:", newRole);
      console.log("Association UID:", associationUid);

      // Get the UID of the current user
      const uid = await getCurrentUserUid(tokenId);
      console.log("User UID:", uid);

      // Fetch the association document reference
      const firestore = getFirestore();
      const associationDocRef = firestore.collection("associations").doc(associationUid);

      // Fetch association data for permission checks
      const associationDoc = await associationDocRef.get();
      if (!associationDoc.exists) {
        console.log("Error : Association not found");
          return res.status(404).json({ message: "Association not found" });
      }

      const associationData = associationDoc.data();

      // Hydrate roles and members
      const roles = hydrateRoles(associationData.roles || {});
      const members = hydrateMembers(associationData.members || {}, roles);

      // Find the current user and their role
      const currentMember = members.find((member) => member.userUid === uid);
      if (!currentMember) {
        console.log(members);
        console.log("Error : User is not a member of the association");
          return res.status(403).json({ message: "User is not a member of the association" });
      }
      

      const userPermissions = currentMember.role.permissions;

      // Check if the user has the required permission
      if (!hasPermission(userPermissions, "Add & Edit Roles")) {
        console.log("Permission denied: ADD_EDIT_ROLES required for this association");
          return res.status(403).json({ message: "Permission denied: ADD_EDIT_ROLES required for this association" });
      }

      // Add the new role to the association
      await addRoleToAssociation(newRole, associationDocRef);
      console.log("Role added successfully");
      return res.status(200).json({ data: "Role added successfully", userId: uid });
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

