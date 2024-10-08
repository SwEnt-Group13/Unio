package com.android.unio.model.association

enum class AssociationType {
  MUSIC,
  FESTIVALS,
  INNOVATION,
  FACULTIES,
  SOCIAL,
  TECH,
  OTHER
}

data class MockAssociation(val association: Association, val type: AssociationType)

val mockAssociations =
    listOf(
        MockAssociation(
            Association(
                uid = "1",
                acronym = "Musical",
                fullName = "Musical Association",
                description =
                    "AGEPoly Commission – stimulation of the practice of music on the campus",
                members = emptyList()),
            AssociationType.MUSIC),
        MockAssociation(
            Association(
                uid = "2",
                acronym = "Nuit De la Magistrale",
                fullName = "Nuit De la Magistrale Association",
                description =
                    "AGEPoly Commission – party following the formal Magistrale Graduation Ceremony",
                members = emptyList()),
            AssociationType.FESTIVALS),
        MockAssociation(
            Association(
                uid = "3",
                acronym = "Balélec",
                fullName = "Festival Balélec",
                description = "Open-air unique en Suisse, organisée par des bénévoles étudiants.",
                members = emptyList()),
            AssociationType.FESTIVALS),
        MockAssociation(
            Association(
                uid = "4",
                acronym = "Artiphys",
                fullName = "Festival Artiphys",
                description = "Festival à l'EPFL",
                members = emptyList()),
            AssociationType.FESTIVALS),
        MockAssociation(
            Association(
                uid = "5",
                acronym = "Sysmic",
                fullName = "Festival Sysmic",
                description = "Festival à l'EPFL",
                members = emptyList()),
            AssociationType.FESTIVALS),
        MockAssociation(
            Association(
                uid = "6",
                acronym = "IFL",
                fullName = "Innovation Forum Lausanne",
                description = "Innovation Forum Lausanne",
                members = emptyList()),
            AssociationType.INNOVATION),
        MockAssociation(
            Association(
                uid = "7",
                acronym = "Clic",
                fullName = "Clic Association",
                description = "Association of EPFL Students of IC Faculty",
                members = emptyList()),
            AssociationType.FACULTIES),
    )
