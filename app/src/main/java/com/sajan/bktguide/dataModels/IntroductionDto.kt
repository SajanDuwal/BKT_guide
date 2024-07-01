package com.sajan.bktguide.dataModels

import android.os.Parcel
import android.os.Parcelable

class IntroductionDto() : Parcelable {
    var id: Int = 0
    var title: String? = null
    var content: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        title = parcel.readString()
        content = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<IntroductionDto> {
        override fun createFromParcel(parcel: Parcel): IntroductionDto {
            return IntroductionDto(parcel)
        }

        override fun newArray(size: Int): Array<IntroductionDto?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "IntroductionDto(id=$id, title=$title, content=$content)"
    }
}