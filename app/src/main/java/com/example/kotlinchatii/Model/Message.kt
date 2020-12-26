package com.example.kotlinchatii.Model

data class Message(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    var date: String = "",
    var type: String = ""
) {
}