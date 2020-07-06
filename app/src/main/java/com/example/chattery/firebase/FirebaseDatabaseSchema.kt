package com.example.chattery.firebase

//This Singleton provides a schema for the Firebase realtime database
open class UsersColumns{
    companion object{
        val Users = "Users"
        val UserName = "username"
        val Status = "userstatus"
        val Image = "userimage"
        val ImageThumbnail = "userimagethumbnail"
        val TokenId = "device_token"
        val Online = "online"

        //Firebase storage columns
        val ProfileImagesDirectory = "profile_images"
        val ProfileThumbnailDirectory = "thumbnails"
    }
}

class RequestColumns{
    companion object{
        val Requests = "Requests"
        val Request_state = "requeststate"
    }
}

class FriendsColumns{
    companion object{
        val Friends =  "Friends"
        val FriendsSince = "friends_since"
    }
}

class NotificationsColumns{
    companion object{
        val Notification = "Notifications"
        val From = "from"
        val type = "type"
    }
}
 enum class RequestState{
     NOT_FRIENDS,
     FRIENDS,
     SENT,
     RECIEVED,

 }

class RequestLabel{
    companion object{
        val SEND_REQUEST = "Send Request"
        val CANCEL_REQUEST = "Cancel Request"
        val ACCEPT_REQUEST = "Accept"
        val UNFRIEND = "Unfriend"
    }
}
