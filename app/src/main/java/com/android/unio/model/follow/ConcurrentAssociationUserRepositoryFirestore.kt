package com.android.unio.model.follow

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class ConcurrentAssociationUserRepositoryFirestore @Inject constructor(
    private val db: FirebaseFirestore, private val userRepository: UserRepository,
    private val associationRepository: AssociationRepository
) : ConcurrentAssociationUserRepository {

    override fun updateFollow(user: User, association: Association) {
        val batch = db.batch()
    }
}