package com.example.chatapp.Model

import com.google.firebase.firestore.PropertyName

data class userInfomationModel(
    @get:PropertyName("name") @set:PropertyName("name") var nameUser : String,
    @get:PropertyName("urlImage") @set:PropertyName("urlImage") var imageUserURL :String,
    @get:PropertyName("birthday") @set:PropertyName("birthday") var birthdayUser : String,
    @get:PropertyName("gender") @set:PropertyName("gender") var genderUser : String,
    @get:PropertyName("email") @set:PropertyName("email") var emailUser : String,
    @get:PropertyName("phoneNumber") @set:PropertyName("phoneNumber") var phoneNumberUser : String,
    @get:PropertyName("uid") @set:PropertyName("uid") var uidUser : String,
    @get:PropertyName("checkPrivatePhone") @set:PropertyName("checkPrivatePhone") var checkPrivatePhone : Boolean = true,
    @get:PropertyName("checkPrivateEmail") @set:PropertyName("checkPrivateEmail") var checkPrivateEmail : Boolean = true,
    @get:PropertyName("checkPrivateBirthday") @set:PropertyName("checkPrivateBirthday") var checkPrivateBirthday : Boolean = true

    ) {
    // Empty constructor required by Firestore
    constructor() : this("", "", "", "", "", "", "", true, true, true)
}
