package com.sajan.bktguide.dataModels

import android.os.Parcel
import android.os.Parcelable

class PlaceDataModel() : Parcelable {
    var placeId: Int = 0
    var placeName: String? = null
    var longitude: Float = 0f
    var latitude: Float = 0f
    var content: String? = null

    constructor(parcel: Parcel) : this() {
        placeId = parcel.readInt()
        placeName = parcel.readString()
        longitude = parcel.readFloat()
        latitude = parcel.readFloat()
        content = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(placeId)
        parcel.writeString(placeName)
        parcel.writeFloat(longitude)
        parcel.writeFloat(latitude)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaceDataModel> {
        override fun createFromParcel(parcel: Parcel): PlaceDataModel {
            return PlaceDataModel(parcel)
        }

        override fun newArray(size: Int): Array<PlaceDataModel?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "PlaceDataModel(placeId=$placeId, placeName=$placeName, longitude=$longitude, latitude=$latitude, content=$content)"
    }


}