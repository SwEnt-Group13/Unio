
// The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
const { logger } = require("firebase-functions");
const { onRequest } = require("firebase-functions/v2/https");
const nodemailer = require('nodemailer');

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

