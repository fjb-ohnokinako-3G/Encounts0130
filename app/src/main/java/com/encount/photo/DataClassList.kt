package com.encount.photo

data class like(
    val flag: Boolean
)

data class MapsList(
    val imgpath: String,
    val imglat: Double,
    val imglng: Double
)

data class MapsDataClassList (
    val imgPath: String,
    val imgLat: Double,
    val imgLng: Double
)

data class PostList(
    val postId: String,
    val userId: String,
    val likeFlag: Boolean,
    val image: String,
    val imageLat: Double,
    val imageLng: Double
)

data class ReplyList(
    val userId: String,
    val userName: String,
    val postText: String,
    val postDate: String
)

data class PostDataClassList(
    val postId: String,
    val userId: String,
    val userName: String,
    val userIcon: String,
    val postImage: String,
    val postText: String,
    val postDate: String,
    val likeFlag: Boolean,
    val postLikeCnt: Long,
    val imageLat: Double,
    val imageLng: Double
)

data class LoginDataClassList(
    val flag: Boolean,
    val result: String,
    val userId: Long
)

data class UserDataClassList(
    val userName: String,
    val userNumber: Long,
    val userBio: String,
    val userIcon: String,
    val postCount: String,
    val likeCount: String
)

data class SinginDataClassList (
    val flag: Boolean,
    val result: String
)

data class FriendDataClassList (
    val mapsLat: Double,
    val mapsLng: Double,
    val imagePath: String
)

data class PostList2(
    val imageId: String,
    val userId: String,
    val imagePath: String,
    val imageLat: String,
    val imageLng: String,
    val postId: String,
    val likeFlag: Boolean
)