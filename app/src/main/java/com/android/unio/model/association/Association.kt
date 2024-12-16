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
import com.android.unio.ui.theme.badgeColorBlue
import com.android.unio.ui.theme.badgeColorCyan
import com.android.unio.ui.theme.badgeColorRed
import com.android.unio.ui.theme.badgeColorYellow

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
    val ADMIN = Role("Administrator", Permissions.FULL_RIGHTS, badgeColorCyan, "Administrator")
    val COMITE =
        Role(
            "Committee",
            Permissions.PermissionsBuilder()
                .addPermission(PermissionType.ADD_EDIT_EVENTS)
                .addPermission(PermissionType.ADD_MEMBERS)
                .addPermission(PermissionType.SEE_STATISTICS)
                .addPermission(PermissionType.SEND_NOTIFICATIONS)
                .build(),
            badgeColorYellow,
            "Committee")
    val MEMBER = Role("Member", Permissions.NONE, badgeColorRed, "Member")
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
        (grantedPermissions.contains(PermissionType.FULL_RIGHTS) &&
            permission != PermissionType.OWNER)
  }

  /** Returns a set of all permissions granted by this set. */
  fun getGrantedPermissions(): Set<PermissionType> = grantedPermissions.toSet()

  /** Return true if the set of permissions is not empty. */
  fun hasAnyPermission(): Boolean {
    return grantedPermissions.isNotEmpty()
  }

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
      if (permissions.contains(PermissionType.OWNER)) {
        this.addPermission(PermissionType.FULL_RIGHTS)
      }
      return Permissions(permissions.toMutableSet())
    }
  }
}

/**
 * Enum representing different types of permissions that can be granted to a role.
 *
 * @property stringName A human-readable name for the permission.
 */
enum class PermissionType(val stringName: String) {
  // ADMIN
  OWNER("Owner"), // Special permission granting FULL_RIGHTS & Add give Full Rights to people. Can
  // also edit & delete the association.
  FULL_RIGHTS("Full Rights"), // Special permission granting all permissions except owner

  // MEMBERS
  VIEW_INVISIBLE_MEMBERS(
      "View Invisible Members"), // See all members of the association including invisible ones
  ADD_MEMBERS("Add Members"),
  DELETE_MEMBERS("Delete Members"),

  // GENERAL
  SEE_STATISTICS("See Statistics"), // See all statistics of the association
  SEND_NOTIFICATIONS(
      "Send Notification"), // Send notifications to every people who liked a certain event
  VALIDATE_PICTURES(
      "Validate Pictures"), // Validate pictures taken by other people, making them visible for
  // other users
  BETTER_OVERVIEW(
      "Better Overview"), // Add the coloured strips to this association (If you don't have any
  // other permission. Otherwise it is done automatically)

  // EVENTS
  VIEW_INVISIBLE_EVENTS("View Events"), // View events that will be launched soon, or drafts
  ADD_EDIT_EVENTS("Add & Edit Events"),
  DELETE_EVENTS("Delete Events")
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

@Document
data class MemberDocument(
    @Id val uid: String, // Unique identifier for the MemberDocument (can be member's uid)
    @Namespace
    val namespace: String = "unio", // Namespace for the document (similar to associations/events)
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val userUid: String, // The UID of the user (linked to the member)
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val role: String, // The role of the member
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val associationUid: String // The UID of the association this member belongs to
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
