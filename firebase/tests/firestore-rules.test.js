import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment
} from "@firebase/rules-unit-testing"
import { mkdir, readFile, writeFile } from "fs/promises"
import { setDoc, doc, updateDoc, getDoc, getDocs, deleteDoc, collection } from "firebase/firestore";
import { alice, aliceAssociation, aliceEvent, otherEvent, otherAssociation, otherUser, setupFirestore } from './firestore-mock-data.js';

test("Testing Firestore Rules", async () => {
  const host = "127.0.0.1";
  const port = 8080;

  /** Check that emulators are running **/
  try {
    await fetch(`http://${host}:${port}`);
  } catch(e) {
    console.error(e);
    throw new Error("Emulators are not running, please start them before running tests.");
  }

  /** Initialize testing environment **/
  const testEnv = await initializeTestEnvironment({
    projectId: "unio-1b8ee",
    firestore: {
      rules: await readFile("firestore.rules", "utf8"),
      host, port
    },
  });
  process.env['FIRESTORE_EMULATOR_HOST'] = `${host}:${port}`;
  
  /** Load data **/
  await setupFirestore(testEnv);

  /** Run tests **/
  try {
    await runTests(testEnv);
  } catch(e) {
    await testEnv.clearFirestore();
    throw e;
  }

  /** Generate coverage report **/
  await generateCoverageReport(host, port);
  
  /** Cleanup **/
  await testEnv.clearFirestore();
  await testEnv.cleanup();
});

async function runTests(testEnv) {
  const aliceAuth = testEnv.authenticatedContext(alice.uid, {
    email_verified: true,
    email: alice.email
  });  
  const aliceDb = aliceAuth.firestore();

  /** Read and write operations on users **/
  await assertSucceeds(setDoc(doc(aliceDb, `/users/${alice.uid}`), alice));
  await assertSucceeds(getDoc(doc(aliceDb, `/users/${alice.uid}`)));
  await assertFails(setDoc(doc(aliceDb, `/users/${alice.uid}`), { ...alice, uid: "other" }));
  await assertFails(setDoc(doc(aliceDb, `/users/${alice.uid}`), { ...alice, email: "other" }));
  await assertFails(setDoc(doc(aliceDb, `/users/${alice.uid}`), { ...alice, joinedAssociations: ["other-association"] }));
  await assertFails(updateDoc(doc(aliceDb, `/users/${otherUser.uid}`), alice));
  await assertFails(setDoc(doc(aliceDb, `/users/new-user`), { ...alice, uid: "new-user" }));
  await assertSucceeds(getDoc(doc(aliceDb, `/users/${otherUser.uid}`)));
  await assertSucceeds(getDocs(collection(aliceDb, `/users`)));
  await assertSucceeds(deleteDoc(doc(aliceDb, `/users/${alice.uid}`)));
  await assertSucceeds(setDoc(doc(aliceDb, `/users/${alice.uid}`), alice));
  await assertFails(setDoc(doc(aliceDb, `/users/${alice.uid}`), {
    uid: alice.uid,
    email: alice.email,
  }));
  await assertFails(setDoc(doc(aliceDb, `/users/${alice.uid}`), {
    ...alice,
    savedEvents: "invalid type"
  }));

  /** Read and write operations on associations **/
  await assertSucceeds(setDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`), aliceAssociation));
  await assertFails(setDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`), { ...aliceAssociation, uid: "other" }));
  await assertSucceeds(getDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`)));
  await assertFails(updateDoc(doc(aliceDb, `/associations/${otherAssociation.uid}`), aliceAssociation));
  await assertFails(setDoc(doc(aliceDb, `/associations/new-association`), { ...aliceAssociation, uid: "new-association" }));
  await assertSucceeds(updateDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`), { ...aliceAssociation, name: "New name" }));
  await assertSucceeds(updateDoc(doc(aliceDb, `/associations/${otherAssociation.uid}`), { ...otherAssociation, followersCount: 1 }));
  await assertSucceeds(updateDoc(doc(aliceDb, `/associations/${otherAssociation.uid}`), { ...otherAssociation, followersCount: 0 }));
  await assertFails(updateDoc(doc(aliceDb, `/associations/${otherAssociation.uid}`), { ...otherAssociation, followersCount: 1000 }));
  await assertSucceeds(getDocs(collection(aliceDb, `/associations`)));
  await assertFails(deleteDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`)));
  await assertFails(setDoc(doc(aliceDb, `/associations/new-association`), aliceAssociation));
  await assertFails(setDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`), {
    uid: aliceAssociation.uid
  }));
  await assertFails(setDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`), {
    ...aliceAssociation,
    members: "invalid type"
  }));

  /** Read and write operations on events **/
  await assertSucceeds(setDoc(doc(aliceDb, `/events/${aliceEvent.uid}`), aliceEvent));
  await assertFails(setDoc(doc(aliceDb, `/events/${aliceEvent.uid}`), { ...aliceEvent, uid: "other" }));
  await assertSucceeds(getDoc(doc(aliceDb, `/events/${aliceEvent.uid}`)));
  await assertFails(updateDoc(doc(aliceDb, `/events/${otherEvent.uid}`), aliceEvent));
  await assertFails(setDoc(doc(aliceDb, `/events/new-event`), aliceEvent));
  await assertSucceeds(setDoc(doc(aliceDb, `/events/new-event`), { ...aliceEvent, uid: "new-event" }));
  await assertSucceeds(updateDoc(doc(aliceDb, `/events/${aliceEvent.uid}`), { ...aliceEvent, title: "New title" }));
  await assertSucceeds(getDocs(collection(aliceDb, `/events`)));
  await assertSucceeds(deleteDoc(doc(aliceDb, `/events/${aliceEvent.uid}`)));
  await assertSucceeds(setDoc(doc(aliceDb, `/events/${aliceEvent.uid}`), aliceEvent));
  await assertFails(setDoc(doc(aliceDb, `/events/${aliceEvent.uid}`), {
    uid: aliceEvent.uid
  }));
  await assertFails(setDoc(doc(aliceDb, `/events/${aliceEvent.uid}`), {
    ...aliceEvent,
    organisers: "invalid type"
  }));

  /** All unauthenticated requests should be denied **/
  const unAuthenticated = testEnv.unauthenticatedContext();
  const unAuthenticatedDb = unAuthenticated.firestore();
  await assertFails(getDoc(doc(unAuthenticatedDb, `/users/${alice.uid}`)));
  await assertFails(getDocs(collection(unAuthenticatedDb, `/users`)));
  await assertFails(getDoc(doc(unAuthenticatedDb, `/events/${aliceEvent.uid}`)));
  await assertFails(getDocs(collection(unAuthenticatedDb, `/events`)));
  await assertFails(getDoc(doc(unAuthenticatedDb, `/associations/${alice.uid}`)));
  await assertFails(getDocs(collection(unAuthenticatedDb, `/associations`)));

  /** All authenticated requests should be denied if email is not verified **/
  const unverified = testEnv.authenticatedContext(alice.uid, {
    email_verified: false,
    email: alice.email
  });
  const unVerifiedDb = unverified.firestore();
  await assertFails(getDoc(doc(unVerifiedDb, `/users/${alice.uid}`)));
  await assertFails(getDocs(collection(unVerifiedDb, `/users`)));
  await assertFails(getDoc(doc(unVerifiedDb, `/events/${aliceEvent.uid}`)));
  await assertFails(getDocs(collection(unVerifiedDb, `/events`)));
  await assertFails(getDoc(doc(unVerifiedDb, `/associations/${alice.uid}`)));
  await assertFails(getDocs(collection(unVerifiedDb, `/associations`)));
}

async function generateCoverageReport(host, port) {
  const data = await fetch(`http://${host}:${port}/emulator/v1/projects/unio-1b8ee:ruleCoverage.html`);
  const coverage = await data.text();

  await mkdir("firebase/tests/coverage", { recursive: true });
  await writeFile("firebase/tests/coverage/firestore-rules-coverage.html", coverage);
}
