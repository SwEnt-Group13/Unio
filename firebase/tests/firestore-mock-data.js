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
  url: "alice-association.com",
  name: "AA",
  fullName: "Alice Association",
  category: "UNKNOWN",
  description: "Description",
  followersCount: 0,
  members: {"123": "alice"},
  roles: {"alice": {color:4294901760, displayName:"displayNameRole",
    permissions: ["Full Rights"]}},

  image: "",
  events: [],
  principalEmailAddress: "alice@gmail.com"
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
  startDate: new Date(),
  endDate: new Date(),
  location: {
    name: "Location",
    address: "Address",
    latitude: 0.0,
    longitude: 0.0,
  },
  types: ["OTHER"],
  maxNumberOfPlaces: -1,
  numberOfSaved: 0,
  eventPictures: []
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
  startDate: new Date(),
  endDate: new Date(),
  location: {
    name: "Location",
    address: "Address",
    latitude: 0.0,
    longitude: 0.0,
  },
  types: ["OTHER"],
  maxNumberOfPlaces: -1,
  numberOfSaved: 0,
  eventPictures: []
};

export const otherAssociation = {
  uid: "other-association",
  url: "other-association.com",
  name: "OA",
  fullName: "Other Association",
  category: "UNKNOWN",
  description: "Description",
  followersCount: 0,
  members: {"123": "other"},
  roles: {"other": {color:4294901760, displayName:"displayNameRole",
  permissions: ["Full Rights"]}},
  image: "",
  events: [],
  principalEmailAddress: "otherassociation@gmail.com"
};

export async function setupFirestore(testEnv) {
  testEnv.clearFirestore();

  await testEnv.withSecurityRulesDisabled(async env => {
    const db = env.firestore();

    await setDoc(doc(db, `/users/${alice.uid}`), alice);
    await setDoc(doc(db, `/events/${aliceEvent.uid}`), aliceEvent);
    await setDoc(doc(db, `/associations/${aliceAssociation.uid}`), aliceAssociation);

    await setDoc(doc(db, `/users/${otherUser.uid}`), otherUser);
    await setDoc(doc(db, `/associations/${otherAssociation.uid}`), otherAssociation);
    await setDoc(doc(db, `/events/${otherEvent.uid}`), otherEvent);
  });
}
