import { setDoc, doc } from "firebase/firestore";

export const alice = {
  uid: "alice",
  email: "alice@wonderland.com",
  firstName: "Alice",
  lastName: "Wonderland",
  biography: "I'm a curious",
  savedEvents: [],
  followedAssociations: ["other-association"],
  joinedAssociations: ["alice-association"],
  interests: [],
  socials: [],
  profilePicture: "",
};

export const aliceAssociation = {
  uid: "alice-association",
  url: "alice-association",
  name: "AA",
  fullName: "Alice Association",
  category: "UNKNOWN",
  description: "Description",
  followersCount: 0,
  members: ["alice"],
  image: "",
  events: [],
};

export const aliceEvent = {
  uid: "event",
  title: "Event",
  organisers: ["alice-association"],
  taggedAssociations: [],
  image: "",
  description: "Description",
  catchyDescription: "Catchy description",
  price: 0.0,
  date: new Date(),
  location: {
    name: "Location",
    address: "Address",
    latitude: 0.0,
    longitude: 0.0,
  },
  types: ["OTHER"],
};

export const otherUser = {
  uid: "other",
  email: "other@unknown.com",
  firstName: "Other",
  lastName: "Person",
  biography: "I'm a curious",
  savedEvents: [],
  followedAssociations: ["alice-association"],
  joinedAssociations: ["other-association"],
  interests: [],
  socials: [],
  profilePicture: "",
}

export const otherEvent = {
  uid: "other-event",
  title: "Other Event",
  organisers: ["other-association"],
  taggedAssociations: [],
  image: "",
  description: "Description",
  catchyDescription: "Catchy description",
  price: 0.0,
  date: new Date(),
  location: {
    name: "Location",
    address: "Address",
    latitude: 0.0,
    longitude: 0.0,
  },
  types: ["OTHER"],
};

export const otherAssociation = {
  uid: "other-association",
  url: "other-association",
  name: "OA",
  fullName: "Other Association",
  category: "UNKNOWN",
  description: "Description",
  followersCount: 0,
  members: ["other"],
  image: "",
  events: [],
};

export async function setupFirestore(testEnv) {
  testEnv.clearFirestore();

  console.log("Setting up Firestore data...");
  await testEnv.withSecurityRulesDisabled(async env => {
    const db = env.firestore();

    await setDoc(doc(db, `/users/${alice.uid}`), alice);
    await setDoc(doc(db, `/events/${aliceEvent.uid}`), aliceEvent);
    await setDoc(doc(db, `/associations/${aliceAssociation.uid}`), aliceAssociation);

    await setDoc(doc(db, `/users/${otherUser.uid}`), otherUser);
    await setDoc(doc(db, `/associations/${otherAssociation.uid}`), otherAssociation);
    await setDoc(doc(db, `/events/${otherEvent.uid}`), otherEvent);
  });
  console.log("Done.");
}
