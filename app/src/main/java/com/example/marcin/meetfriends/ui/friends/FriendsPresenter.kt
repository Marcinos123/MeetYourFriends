package com.example.marcin.meetfriends.ui.friends

import com.example.marcin.meetfriends.di.ScreenScope
import com.example.marcin.meetfriends.models.User
import com.example.marcin.meetfriends.mvp.BasePresenter
import com.example.marcin.meetfriends.ui.common.EventIdParams
import com.example.marcin.meetfriends.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by MARCIN on 2017-11-13.
 */
@ScreenScope
class FriendsPresenter @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val getFriendsFromFacebook: GetFriendsFromFacebook,
    private val getFriendsFromFirebase: GetFriendsFromFirebase,
    private val eventIdParams: EventIdParams,
    private val auth: FirebaseAuth
) : BasePresenter<FriendsContract.View>(), FriendsContract.Presenter {

  override fun resume() {
    super.resume()
    val disposable = getFriendsFromFacebook.getFriends()
        .subscribe({ profiles ->
          val disposable = getFriendsFromFirebase.get()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .doOnSubscribe { view.showLoading() }
              .doFinally { view.hideLoading() }
              .subscribe({ users ->
                view.showFriendsList(
                    users.filter { user ->
                      profiles.any { it.id == user.facebookId }
                    }
                )
              })
          disposables?.add(disposable)
        })
    disposables?.add(disposable)
  }

  override fun handleInviteFriendEvent(observable: Observable<User>) {
    val disposable = observable.subscribe({ friend ->
      val chosenEventId = eventIdParams.eventId
      val disposable = RxFirebaseDatabase
          .setValue(firebaseDatabase.reference.child(Constants.FIREBASE_EVENTS).child(chosenEventId)
              .child(Constants.FIREBASE_PARTICIPANTS)
              .child("${Constants.FIREBASE_PARTICIPANT}_${DateTime.now().millis}"), friend.uid)
          .doFinally { view.showInvitedFriendSnackBar(friend, chosenEventId) }
          .subscribe()
      disposables?.add(disposable)
    })
    disposables?.add(disposable)
  }

  override fun removeParticipantFromEvent(friendId: String, eventId: String) {
    firebaseDatabase.reference.child(Constants.FIREBASE_EVENTS)
        .child(eventId)
        .child(Constants.FIREBASE_PARTICIPANTS)
        .orderByValue()
        .equalTo(friendId)
        .addListenerForSingleValueEvent(object : ValueEventListener {
          override fun onDataChange(dataSnapshot: DataSnapshot) {
            dataSnapshot.ref.child(dataSnapshot.children.firstOrNull { it.value == friendId }?.key.toString()).removeValue()
          }

          override fun onCancelled(p0: DatabaseError?) {
            Timber.d("Canncelled remove participant with id $friendId")
          }
        })
  }
}
