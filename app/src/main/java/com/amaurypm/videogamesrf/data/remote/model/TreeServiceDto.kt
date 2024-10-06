package com.amaurypm.videogamesrf.data.remote.model

import com.google.gson.annotations.SerializedName

data class TreeServiceDto (

    @SerializedName("id")
    var id: String? = null,

    @SerializedName("thumbnail")
    var thumbnail: String? = null,

    @SerializedName("title")
    var title: String? = null,

    @SerializedName("description")
    var description: String? = null,

    @SerializedName("price")
    var price: String? = null,

    @SerializedName("duration")
    var duration: String? = null

)