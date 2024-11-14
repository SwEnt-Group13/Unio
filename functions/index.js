// The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
const {logger} = require("firebase-functions");
const {onRequest} = require("firebase-functions/v2/https");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const nodemailer = require('nodemailer');

// The Firebase Admin SDK to access Firestore.
const {initializeApp} = require("firebase-admin/app");
const {getFirestore, Timestamp, arrayUnion} = require("firebase-admin/firestore");

//Scheduale management
const {onSchedule} = require("firebase-functions/v2/scheduler");

const {defineString} = require('firebase-functions/params')


initializeApp();

const db = getFirestore();

const email = defineString('EMAIL')
const password = defineString('PASSWORD')
console.log("ok")
console.log(password.value())

console.log("Nodemailer is loaded:", nodemailer); // Log this to verify

const transporter = nodemailer.createTransport({
  service: 'gmail', 
  auth: {
    user: email.value(), 
    pass: password.value(), 
  },
});

exports.sendVerificationEmail = onRequest(async (req, res) => {
  console.log(req.body)
  console.log(req.body.data?.uid)

  const email = req.body.data?.email;
  const associationUid = req.body.data?.associationUid;

  // Generate a random 6-digit verification code
  const code = Math.floor(100000 + Math.random() * 900000).toString();

  // Store verification details in Firestore
  await db.collection('emailVerifications').doc(associationUid).set({
    email,
    code,
    timestamp: Timestamp.now(),
    status: 'pending',
  });

  // Send email with the code
  const mailOptions = {
    from: 'software.entreprise@gmail.com',
    to: email,
    subject: 'Your Verification Code',
    text: `Your verification code is ${code}. This code will expire in 10 minutes.`,
  };

  try {
    await transporter.sendMail(mailOptions);
    res.json({ data: `Association with ID ${associationUid}, bloublou` });
  } catch (error) {
    console.error('Error sending email:', error);
    res.status(500).json({ message: "Email not sent", error: error.message });
  }
});

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
    console.error("Error verifying code:", error);
    return res.status(500).json({ message: "server-error", error: "An unexpected error occurred." });
  }
});

/* PROOF OF CONCEPT, need to upgrade to Blaze plan according to "https://firebase.google.com/docs/functions/schedule-functions?gen=2nd"
exports.cleanupExpiredCodes = onSchedule("every day 18:05", async (event) => {
  console.log("okokok")
  const expiredTime = Timestamp.now().seconds - 600; // 10 minutes in seconds
  const expiredDocs = await db.collection('emailVerifications')
    .where('timestamp', '<', expiredTime)
    .where('status', '==', 'pending')
    .get();

  const deletePromises = [];
  expiredDocs.forEach(doc => deletePromises.push(doc.ref.delete()));

  return Promise.all(deletePromises);
});*/




















