package com.example.kotlinchatii.Model

import android.os.Parcel
import android.os.Parcelable

data class Friend(
    var name: String? = "",
    val status: String? = "",
    val image: String? = "",
    var number: String? = "",
    val uid: String? = "",
    val online: String? = "offline",
    val typing: String? = "false"
) : Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(status)
        parcel.writeString(image)
        parcel.writeString(number)
        parcel.writeString(uid)
        parcel.writeString(online)
        parcel.writeString(typing)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Friend> {
        override fun createFromParcel(parcel: Parcel): Friend {
            return Friend(parcel)
        }

        override fun newArray(size: Int): Array<Friend?> {
            return arrayOfNulls(size)
        }
    }
}