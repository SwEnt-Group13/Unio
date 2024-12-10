package com.android.unio.model.association

import androidx.appsearch.annotation.Document
import androidx.appsearch.annotation.Document.Id
import androidx.appsearch.annotation.Document.Namespace
import androidx.appsearch.annotation.Document.StringProperty
import androidx.appsearch.app.AppSearchSchema.StringPropertyConfig
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.ReferenceElement
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.firestore.UniquelyIdentifiable
import com.android.unio.model.user.User
import com.android.unio.ui.theme.badgeColorBlack
import com.android.unio.ui.theme.badgeColorBlue

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
 * @property displayNameId A human-readable name for the category.
 */
enum class AssociationCategory(val displayNameId: Int) {
  EPFL_BODIES(R.string.association_category_epfl_bodies),
  REPRESENTATION(R.string.association_category_representation),
  PROJECTS(R.string.association_category_projects),
  EPFL_STUDENTS(R.string.association_category_epfl_students),
  COUNTRIES(R.string.association_category_countries),
  SUSTAINABILITY(R.string.association_category_sustainability),
  SCIENCE_TECH(R.string.association_category_science_tech),
  CULTURE_SOCIETY(R.string.association_category_culture_society),
  ARTS(R.string.association_category_arts),
  ENTERTAINMENT(R.string.association_category_entertainment),
  SPORTS(R.string.association_category_sports),
  GUIDANCE(R.string.association_category_guidance),
  UNKNOWN(R.string.association_category_unknown)
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
class Role(
    val displayName: String,
    val permissions: Permissions,
    val color: Long,
    override val uid: String
) : UniquelyIdentifiable {

  companion object {
    // Predefined roles
    val ADMIN = Role("Administrator", Permissions.FULL_RIGHTS, badgeColorBlue, "Administrator")
    val COMITE =
        Role(
            "Committee",
            Permissions.PermissionsBuilder()
                .addPermission(PermissionType.VIEW_MEMBERS)
                .addPermission(PermissionType.EDIT_MEMBERS)
                .addPermission(PermissionType.VIEW_EVENTS)
                .build(),
            0xFF0000FF,
            "Committee")
    val MEMBER = Role("Member", Permissions.NONE, badgeColorBlue, "Member")
    val GUEST = Role("Guest", Permissions.NONE, badgeColorBlue, "Guest")
  }
}

/**
 * Represents a set of permissions for a role.
 *
 * @property grantedPermissions The permissions granted to this set.
 */
class Permissions private constructor(private val grantedPermissions: MutableSet<PermissionType>) {

  /** Returns true if the permission is granted, false otherwise. */
  fun hasPermission(permission: PermissionType): Boolean {
    return grantedPermissions.contains(permission) ||
        grantedPermissions.contains(PermissionType.FULL_RIGHTS)
  }

  /** Returns a set of all permissions granted by this set. */
  fun getGrantedPermissions(): Set<PermissionType> = grantedPermissions.toSet()

  /**
   * Adds a permission to the set grantedPermissions.
   *
   * @param permission Permission to add.
   * @return A new Permissions object with the added permission.
   */
  fun addPermission(permission: PermissionType): Permissions {
    grantedPermissions.add(permission)
    return this.copy()
  }

  /**
   * Adds a list of permissions to the set.
   *
   * @param permissionList List of permissions to add.
   * @return A new Permissions object with the added permissions.
   */
  fun addPermissions(permissionList: List<PermissionType>): Permissions {
    grantedPermissions.addAll(permissionList)
    return this.copy()
  }

  /**
   * Deletes a permission from the set.
   *
   * @param permission Permission to delete.
   * @return A new Permissions object with the deleted permission.
   */
  fun deletePermission(permission: PermissionType): Permissions {
    grantedPermissions.remove(permission)
    return this.copy()
  }

  /**
   * Deletes a list of permissions from the set.
   *
   * @param permissionList List of permissions to delete.
   * @return A new Permissions object with the deleted permissions.
   */
  fun deleteAllPermissions(permissionList: List<PermissionType>): Permissions {
    grantedPermissions.removeAll(permissionList)
    return this.copy()
  }

  /** Create and return a copy of the current Permissions object with updated permissions */
  private fun copy(): Permissions {
    return Permissions(grantedPermissions.toMutableSet())
  }

  companion object {
    // predefined permission sets
    val FULL_RIGHTS = Permissions(mutableSetOf(PermissionType.FULL_RIGHTS))
    val NONE = Permissions(mutableSetOf())
  }

  /** PermissionsBuilder class to create Permissions with a list of roles/permissions */
  class PermissionsBuilder {
    private val permissions = mutableSetOf<PermissionType>()

    /**
     * Add a specific permission to the permissions set
     *
     * @param permission Permission to add
     * @return The PermissionsBuilder object with the added permission
     */
    fun addPermission(permission: PermissionType): PermissionsBuilder {
      permissions.add(permission)
      return this
    }

    /**
     * Add a list of permissions to the permissions set
     *
     * @param permissionList List of permissions to add
     * @return The PermissionsBuilder object with the added permissions
     */
    fun addPermissions(permissionList: List<PermissionType>): PermissionsBuilder {
      permissions.addAll(permissionList)
      return this
    }

    /**
     * Build and return the Permissions object
     *
     * @return The Permissions object with the permissions added
     */
    fun build(): Permissions {
      // Ensure that FULL_RIGHTS is not included explicitly
      if (permissions.contains(PermissionType.FULL_RIGHTS)) {
        throw IllegalArgumentException("Cannot grant FULL_RIGHTS explicitly.")
      }
      return Permissions(permissions)
    }
  }
}

/**
 * Enum representing different types of permissions that can be granted to a role.
 *
 * @property stringName A human-readable name for the permission.
 */
enum class PermissionType(val stringName: String) {
  FULL_RIGHTS("Full rights"), // Special permission granting all rights
  VIEW_MEMBERS("View members"),
  EDIT_MEMBERS("Edit members"),
  DELETE_MEMBERS("Delete members"),
  VIEW_EVENTS("View events"),
  EDIT_EVENTS("Edit events"),
  DELETE_EVENTS("Delete Events"),
  ADD_EVENTS("Add Events")
}

/**
 * A class representing the association document for AppSearch indexing. It allows the search engine
 * to search on the name, fullName and description fields.
 *
 * @param namespace The namespace of the document: "unio" for our app
 * @param uid The unique identifier of the document
 * @param name The name of the association
 * @param fullName The full name of the association
 */
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

/**
 * Extension function to convert an Association object to an AssociationDocument object
 *
 * @return The AssociationDocument object created from the Association object
 */
fun Association.toAssociationDocument(): AssociationDocument {
  return AssociationDocument(
      uid = this.uid, name = this.name, fullName = this.fullName, description = this.description)
}
