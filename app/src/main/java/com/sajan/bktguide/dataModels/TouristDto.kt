package com.sajan.bktguide.dataModels

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class TouristDto() : Parcelable {
    var id: Int = 0
    var name: String? = null
    var email: String? = null
    var username: String? = null
    var password: String? = null
    var imageUri: Uri? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        name = parcel.readString()
        email = parcel.readString()
        username = parcel.readString()
        password = parcel.readString()
        imageUri = parcel.readParcelable(Uri::class.java.classLoader)
    }


    override fun toString(): String {
        return "TouristDto(id=$id,name=$name, email=$email, username=$username, password=$password, imageUri=$imageUri)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeParcelable(imageUri, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TouristDto> {
        override fun createFromParcel(parcel: Parcel): TouristDto {
            return TouristDto(parcel)
        }

        override fun newArray(size: Int): Array<TouristDto?> {
            return arrayOfNulls(size)
        }
    }

}

