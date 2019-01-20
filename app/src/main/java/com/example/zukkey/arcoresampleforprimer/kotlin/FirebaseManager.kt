package com.example.zukkey.arcoresampleforprimer.kotlin

import android.content.Context
import android.widget.Toast
import com.google.ar.sceneform.utilities.Preconditions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import timber.log.Timber


class FirebaseManager(context: Context) {

  private val app: FirebaseApp?
  private val spotListRef: DatabaseReference?
  private val roomCodeRef: DatabaseReference?
  private var currentRoomRef: DatabaseReference? = null
  private var currentRoomListener: ValueEventListener? = null
  private val ROOT_FIREBASE_SPOTS = "spot_list"
  private val ROOT_LAST_ROOM_CODE = "last_room_code"
  private val KEY_ANCHOR_ID = "hosted_anchor_id"
  private val KEY_MEMO = "memo"

  interface RoomCodeListener {
    fun onNewRoomCode(newRoomCode: Long?)
    fun onError(databaseError: DatabaseError?)
  }

  interface CloudAnchorIdListener {
    fun onNewCloudAnchorId(cloudAnchorId: String)
    fun onSetMemo(memo: String, cloudAnchorId: String)
  }

  init {
    app = FirebaseApp.initializeApp(context)
    if (app != null) {
      val rootRef = FirebaseDatabase.getInstance(app).reference
      spotListRef = rootRef.child(ROOT_FIREBASE_SPOTS)
      roomCodeRef = rootRef.child(ROOT_LAST_ROOM_CODE)

      DatabaseReference.goOnline()
    } else {
      Timber.d("Could not connect to Firebase Database!")
      spotListRef = null
      roomCodeRef = null
    }
  }

  fun createNewRoom(newRoomCode: Long?, listener: RoomCodeListener) {
    Preconditions.checkNotNull(app, "Firebase Appが存在しません")
    if (newRoomCode == null) {
      Toast.makeText(app?.applicationContext, "Room Codeが存在しません", Toast.LENGTH_SHORT).show()
      return
    }

    roomCodeRef?.runTransaction(
        object : Transaction.Handler {
          override fun doTransaction(mutableData: MutableData): Transaction.Result {
            mutableData.value = newRoomCode
            return Transaction.success(mutableData)
          }

          override fun onComplete(databaseError: DatabaseError?, completed: Boolean, dataSnapshot: DataSnapshot?) {
            if (!completed) {
              listener.onError(databaseError)
              return
            }
            listener.onNewRoomCode(dataSnapshot?.getValue(Long::class.java))
          }
        }
    )
  }

  fun storeAnchorIdInRoom(roomCode: Long?, cloudAnchorId: String, memo: String) {
    Preconditions.checkNotNull(app, "Firebase Appが存在しません")
    val roomRef = spotListRef?.child(roomCode.toString()) ?: return
    roomRef.child(KEY_ANCHOR_ID).setValue(cloudAnchorId)
    roomRef.child(KEY_MEMO).setValue(memo)
  }

  fun resolvingRoom(roomCode: Long?, listener: CloudAnchorIdListener) {
    Preconditions.checkNotNull(app, "Firebase Appが存在しません")
    clearRoomListener()
    currentRoomRef = spotListRef?.child(roomCode.toString())
    currentRoomListener = object : ValueEventListener {
      override fun onDataChange(dataSnapshot: DataSnapshot) {
        val valObj = dataSnapshot.child(KEY_ANCHOR_ID).value
        val valMemo = dataSnapshot.child(KEY_MEMO).value
        if (valObj != null && valMemo != null) {
          val anchorId = valObj.toString()
          val memoText = valMemo.toString()
          if (!anchorId.isEmpty()) {
            listener.onNewCloudAnchorId(anchorId)
            listener.onSetMemo(memoText, anchorId)
          }
        }
      }

      override fun onCancelled(databaseError: DatabaseError) {
        Timber.e(databaseError.message, databaseError)
      }
    }
    currentRoomRef?.addValueEventListener(currentRoomListener!!)
  }

  private fun clearRoomListener() {
    if (currentRoomListener != null && currentRoomRef != null) {
      currentRoomRef?.removeEventListener(currentRoomListener!!)
      currentRoomListener = null
      currentRoomRef = null
    }
  }
}
