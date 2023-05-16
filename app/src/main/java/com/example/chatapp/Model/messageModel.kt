package com.example.chatapp.Model

data class messageModel(
    var idUserSend : String? =null,
    var idUserRecevied : String? =null,
    var content : String? =null,
    var type : String? =null,
    var timeHour : String? =null,
    var timedate : String? =null,
    var isRead : String? = null,
    var isLastSend : String? = null
)
