package com.android.unio.model.association

import androidx.appsearch.annotation.Document
import androidx.appsearch.annotation.Document.Id
import androidx.appsearch.annotation.Document.Namespace
import androidx.appsearch.annotation.Document.StringProperty
import androidx.appsearch.app.AppSearchSchema.StringPropertyConfig
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.ReferenceElement
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.firestore.UniquelyIdentifiable
import com.android.unio.model.strings.AssociationStrings
import com.android.unio.model.user.User

/**
 * Association data class
 *
 * Make sure to update the hydration and serialization methods when changing the data class
 *
 * @property uid association id
 * @property url association url
 * @property name association acronym
 * @property fullName association full name
 * @property category association category
 * @property description association description
 * @property followersCount number of association followers
 * @property members list of association members
 * @property image association image
 * @property events list of association events
 */
data class Association(
    override val uid: String,
    val url: String,
    val name: String,
    val fullName: String,
    val category: AssociationCategory,
    val description: String,
    val followersCount: Int,
    val members: List<Member>,
    val roles: List<Role>,
    var image: String,
    val events: ReferenceList<Event>,
    val principalEmailAddress: String

) : UniquelyIdentifiable {
  companion object
}

enum class AssociationCategory(val displayName: String) {
  EPFL_BODIES(AssociationStrings.EPFL_BODIES),
  REPRESENTATION(AssociationStrings.REPRESENTATION),
  PROJECTS(AssociationStrings.PROJECTS),
  EPFL_STUDENTS(AssociationStrings.EPFL_STUDENTS),
  COUNTRIES(AssociationStrings.COUNTRIES),
  SUSTAINABILITY(AssociationStrings.SUSTAINABILITY),
  SCIENCE_TECH(AssociationStrings.SCIENCE_TECH),
  CULTURE_SOCIETY(AssociationStrings.CULTURE_SOCIETY),
  ARTS(AssociationStrings.ARTS),
  ENTERTAINMENT(AssociationStrings.ENTERTAINMENT),
  SPORTS(AssociationStrings.SPORTS),
  GUIDANCE(AssociationStrings.GUIDANCE),
  UNKNOWN(AssociationStrings.UNKNOWN)
}

data class Member(
    val user: ReferenceElement<User>,
    val role: Role
) : UniquelyIdentifiable {
    class Companion {

    }

    override val uid: String
        get() = user.uid
}

fun compareMemberLists(expected: List<Member>, actual: List<Member>): Boolean {
    if (expected.size != actual.size) return false

    // Sort both lists by Member.uid for consistent comparison
    val sortedExpected = expected.sortedBy { it.uid }
    val sortedActual = actual.sortedBy { it.uid }

    // Compare each Member in the sorted lists
    return sortedExpected.zip(sortedActual).all { (expectedMember, actualMember) ->
        expectedMember.uid == actualMember.uid &&
                expectedMember.user.uid == actualMember.user.uid &&
                expectedMember.role.uid == actualMember.role.uid
    }
}

fun compareRoleLists(expected: List<Role>, actual: List<Role>): Boolean {
    // Convert both lists to sets of Role.uid for comparison
    val expectedUids = expected.map { it.uid }.toSet()
    val actualUids = actual.map { it.uid }.toSet()

    // Return true if the sets of UIDs are identical
    return expectedUids == actualUids
}


class Role(
    val displayName: String,
    val permissions: Permissions,
    override val uid: String
) : UniquelyIdentifiable {

    companion object {
        // Predefined roles
        val ADMIN = Role("Administrator", Permissions.FULL_RIGHTS, "Administrator")
        val COMITE = Role("Committee", Permissions.PermissionsBuilder()
            .addPermission(PermissionType.VIEW_MEMBERS)
            .addPermission(PermissionType.EDIT_MEMBERS)
            .addPermission(PermissionType.VIEW_EVENTS).build(), "Committee")
        val MEMBER = Role("Member", Permissions.NONE, "Member")
        val GUEST = Role("Guest", Permissions.NONE, "Guest")

        // Factory method to create new roles
        fun createRole(displayName: String, permissions: Permissions, uid: String): Role {
            return Role(displayName, permissions, uid)
        }
    }

    // Optionally, you can add additional methods to manipulate roles if needed
}


class Permissions private constructor(private val grantedPermissions: MutableSet<PermissionType>) {

    // check if specific permission is granted
    fun hasPermission(permission: PermissionType): Boolean {
        return grantedPermissions.contains(permission) || grantedPermissions.contains(PermissionType.FULL_RIGHTS)
    }

    // get all granted permissions as a list
    fun getGrantedPermissions(): Set<PermissionType> = grantedPermissions.toSet()

    // Add a specific permission to the permissions set (returns a new Permissions object)
    fun addPermission(permission: PermissionType): Permissions {
        grantedPermissions.add(permission)
        return this.copy()
    }

    // Add a list of permissions to the permissions set (returns a new Permissions object)
    fun addPermissions(permissionList: List<PermissionType>): Permissions {
        grantedPermissions.addAll(permissionList)
        return this.copy()
    }

    // Delete a specific permission from the permissions set (returns a new Permissions object)
    fun deletePermission(permission: PermissionType): Permissions {
        grantedPermissions.remove(permission)
        return this.copy()
    }

    // Delete a list of permissions from the permissions set (returns a new Permissions object)
    fun deletePermissions(permissionList: List<PermissionType>): Permissions {
        grantedPermissions.removeAll(permissionList)
        return this.copy()
    }

    // Create and return a copy of the current Permissions object with updated permissions
    private fun copy(): Permissions {
        return Permissions(grantedPermissions.toMutableSet())
    }

    companion object {
        // Predefined permission sets
        val FULL_RIGHTS = Permissions(mutableSetOf(PermissionType.FULL_RIGHTS))
        val NONE = Permissions(mutableSetOf())
    }

    // PermissionsBuilder class to create Permissions with a list of roles/permissions
    class PermissionsBuilder {
        private val permissions = mutableSetOf<PermissionType>()

        // Add a specific permission to the permissions set
        fun addPermission(permission: PermissionType): PermissionsBuilder {
            permissions.add(permission)
            return this
        }

        // Add a list of permissions to the permissions set
        fun addPermissions(permissionList: List<PermissionType>): PermissionsBuilder {
            permissions.addAll(permissionList)
            return this
        }

        // Build and return the Permissions object
        fun build(): Permissions {
            // Ensure that FULL_RIGHTS is not included explicitly
            if (permissions.contains(PermissionType.FULL_RIGHTS)) {
                throw IllegalArgumentException("Cannot grant FULL_RIGHTS explicitly.")
            }
            return Permissions(permissions)
        }
    }
}



enum class PermissionType(val stringName : String) {
    FULL_RIGHTS("Full rights"), // Special permission granting all rights
    VIEW_MEMBERS("View members"),
    EDIT_MEMBERS("Edit members"),
    DELETE_MEMBERS("Delete members"),
    VIEW_EVENTS("View events"),
    EDIT_EVENTS("Edit events"),
    DELETE_EVENTS("Delete Events")
}



@Document
data class AssociationDocument(
    @Namespace val namespace: String = "unio",
    @Id val uid: String,
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val name: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val fullName: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val description: String = ""
)

fun Association.toAssociationDocument(): AssociationDocument {
  return AssociationDocument(
      uid = this.uid, name = this.name, fullName = this.fullName, description = this.description)
}
