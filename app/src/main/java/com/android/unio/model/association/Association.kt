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
 * Represents an association within the system. This class holds various details about the
 * association and its related entities.
 *
 * **Note:** When modifying this class, ensure the serialization and hydration methods are updated
 * accordingly to reflect the changes.
 *
 * @property uid Unique identifier for the association.
 * @property url URL of the association's homepage.
 * @property name Acronym or short name of the association.
 * @property fullName Full name of the association.
 * @property category The category under which the association falls.
 * @property description Brief description of the association's purpose and activities.
 * @property followersCount Number of users following this association.
 * @property members List of members associated with the association.
 * @property roles List of roles defined within the association.
 * @property image URL or path to the association's image/logo.
 * @property events A reference list containing events associated with the association.
 * @property principalEmailAddress Primary email address for contacting the association.
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

/**
 * Enum representing different categories of associations.
 *
 * @property displayName A human-readable name for the category.
 */
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

/**
 * Represents a member of an association.
 *
 * @property user Reference to the user who is a member.
 * @property role The role assigned to the member within the association.
 */
data class Member(val user: ReferenceElement<User>, val role: Role) : UniquelyIdentifiable {
  class Companion {}

  override val uid: String
    get() = user.uid
}

/**
 * Represents a role within an association.
 *
 * @property displayName Name of the role.
 * @property permissions Set of permissions assigned to this role.
 * @property uid Unique identifier for the role.
 */
class Role(val displayName: String, val permissions: Permissions, override val uid: String) :
    UniquelyIdentifiable {

  companion object {
    // Predefined roles
    val ADMIN = Role("Administrator", Permissions.FULL_RIGHTS, "Administrator")
    val COMITE =
        Role(
            "Committee",
            Permissions.PermissionsBuilder()
                .addPermission(PermissionType.VIEW_MEMBERS)
                .addPermission(PermissionType.EDIT_MEMBERS)
                .addPermission(PermissionType.VIEW_EVENTS)
                .build(),
            "Committee")
    val MEMBER = Role("Member", Permissions.NONE, "Member")
    val GUEST = Role("Guest", Permissions.NONE, "Guest")

    // Factory method to create new roles
    fun createRole(displayName: String, permissions: Permissions, uid: String): Role {
      return Role(displayName, permissions, uid)
    }
  }
}

/**
 * Represents a set of permissions for a role.
 *
 * @property grantedPermissions The permissions granted to this set.
 */
class Permissions private constructor(private val grantedPermissions: MutableSet<PermissionType>) {

  fun hasPermission(permission: PermissionType): Boolean {
    return grantedPermissions.contains(permission) ||
        grantedPermissions.contains(PermissionType.FULL_RIGHTS)
  }

  fun getGrantedPermissions(): Set<PermissionType> = grantedPermissions.toSet()

  fun addPermission(permission: PermissionType): Permissions {
    grantedPermissions.add(permission)
    return this.copy()
  }

  fun addPermissions(permissionList: List<PermissionType>): Permissions {
    grantedPermissions.addAll(permissionList)
    return this.copy()
  }

  fun deletePermission(permission: PermissionType): Permissions {
    grantedPermissions.remove(permission)
    return this.copy()
  }

  fun deleteAllPermissions(permissionList: List<PermissionType>): Permissions {
    grantedPermissions.removeAll(permissionList)
    return this.copy()
  }

  // create and return a copy of the current Permissions object with updated permissions
  private fun copy(): Permissions {
    return Permissions(grantedPermissions.toMutableSet())
  }

  companion object {
    // predefined permission sets
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

enum class PermissionType(val stringName: String) {
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
