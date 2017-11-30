package com.example.marcin.meetfriends.ui.event_detail

import com.example.marcin.meetfriends.models.User
import com.example.marcin.meetfriends.utils.Constants
import com.google.firebase.database.FirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Maybe
import javax.inject.Inject

/**
 * Created by marci on 2017-11-29.
 */
class GetParticipantsUseCase @Inject constructor(
        private val firebaseDatabase: FirebaseDatabase
) {

    fun get(participantsIds: List<String>): Maybe<MutableList<User>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
                firebaseDatabase.reference.child(Constants.FIREBASE_USERS))
        { dataSnapshot ->
            val participants = mutableListOf<User>()
            dataSnapshot.children.filter { userKey -> participantsIds.any { it == userKey.key } }.forEach {
                participants.add(it.getValue(User::class.java).let { it!! })
            }
            return@observeSingleValueEvent participants
        }
    }
}