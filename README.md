# Unio

## The problem being solved
University associations, specifically the ones at EPFL, lack ways of interacting with their community. This can be seen through their single e-mail per semester allowance, or their use of the Instagram platform to communicate about projects or events, when only a small proportion of their audience sees it. Our project aims to resolve this issue by providing a single platform where campus associations can inform about, manage, and prepare their events. This would take shape as a social media-style application, where associations would be presented to users who could then choose to learn more about them, follow them (letting them know of future events etc.), or even join them. Once one has followed a few associations, their feed would then consist of their future events. As such, the core audience we aim for are students both on the user side and the association side. The application can be viewed as an extension of the EPFL Campus application, but it could also potentially be later adapted for non-university events.

## Cloud services in use
We intend to use Firebase services:
- Firestore for user data storage
- Firebase Auth for user authentication, and/or Microsoft identity platform (to enable EPFL-only login)
- Firebase ML to host a content moderation model (applied to user-uploaded pictures after events)
  We also intend to use the Google Maps API to display a map-view of the events near the user.

## Authentication services
Authentication will be through Firebase Auth using the Microsoft provider, which will then allow only @epfl.ch emails. At a later point in time, this could be scaled to allow other universities to use the application.
Users are then enabled to interact with events, ask questions directly to association committee members, share pictures after events etc.

## Sensors used
The following sensors would be used:
- GPS localisation, for the map feature, will enable users to see upcoming events near them
- Camera access, so that users can upload pictures after events

## Offline mode
Most of the features, such as viewing associations, events, the map, would still work in offline mode, with the only difference being that the visible data would be out of date. Messaging, uploading pictures etc. would not work offline but there could be a queue system so that they are immediately uploaded when connectivity is re-established.