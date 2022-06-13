package io.connect.app.activities.home

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserDetailModel {

    @SerializedName("email")
    @Expose
    lateinit var email: String

    @SerializedName("id")
    @Expose
    lateinit var id: String

    @SerializedName("browserPage")
    @Expose
    lateinit var browserPage: String



}