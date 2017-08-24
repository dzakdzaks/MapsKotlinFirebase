package com.project.dzakdzak.mapskotlinfirebase.Init

import com.google.gson.annotations.SerializedName

class ResponseJSON {

    @SerializedName("routes")
    var routes: List<Route>? = null
    @SerializedName("status")
    var status: String? = null

}
