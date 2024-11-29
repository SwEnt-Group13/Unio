package com.android.unio.model.association

/**
 * Utility function to compare two lists of members.
 *
 * @param expected Expected list of members.
 * @param actual Actual list of members.
 * @return `true` if the lists are identical in content and order, otherwise `false`.
 */
fun compareMemberLists(expected: List<Member>, actual: List<Member>): Boolean {
  if (expected.size != actual.size) return false

  // sort both lists by Member.uid for consistent comparison
  val sortedExpected = expected.sortedBy { it.uid }
  val sortedActual = actual.sortedBy { it.uid }

  // compare each Member in the sorted lists
  return sortedExpected.zip(sortedActual).all { (expectedMember, actualMember) ->
    expectedMember.uid == actualMember.uid &&
        expectedMember.user.uid == actualMember.user.uid &&
        expectedMember.role.uid == actualMember.role.uid
  }
}

/**
 * Utility function to compare two lists of roles.
 *
 * @param expected Expected list of roles.
 * @param actual Actual list of roles.
 * @return `true` if the sets of UIDs are identical, otherwise `false`.
 */
fun compareRoleLists(expected: List<Role>, actual: List<Role>): Boolean {
  // convert both lists to sets of Role.uid for comparison
  val expectedUids = expected.map { it.uid }.toSet()
  val actualUids = actual.map { it.uid }.toSet()

  // return true if the sets of UIDs are identical
  return expectedUids == actualUids
}
