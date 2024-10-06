package com.amaurypm.videogamesrf.data.remote

import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDetailDto
import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface TreeServicesApi {

    // Apiary

    // https://private-c0eaf-treeservices1.apiary-mock.com/treeServices/treeServices_list
    @GET("treeServices/treeServices_list")
    fun getTreeServicesApiary(): Call<MutableList<TreeServiceDto>>

    // https://private-c0eaf-treeservices1.apiary-mock.com/treeServices/treeService_detail/21357
    @GET("treeServices/treeService_detail/{id}")
    fun getTreeServiceDetailApiary(
        @Path("id") id: String?
    ): Call<TreeServiceDetailDto>

}