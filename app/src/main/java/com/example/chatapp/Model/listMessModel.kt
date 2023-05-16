package com.example.chatapp.Model

data class listMessModel(
    var uidUser : String? = null,
    var imageURL : String? = null,
    var nameUser : String? = null,
    var lastMess : String? = null,
    var timeHour : String? = null,
    var timeDay : String? = null,
    var isRead : Int = 0
)
