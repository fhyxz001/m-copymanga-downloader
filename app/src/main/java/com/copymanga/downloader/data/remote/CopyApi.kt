package com.copymanga.downloader.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface CopyApi {

    @FormUrlEncoded
    @POST("/api/v3/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("source") source: String = "freeSite",
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("/api/v3/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("salt") salt: Int = 1729,
    ): Response<ResponseBody>

    @GET("/api/v3/member/info")
    suspend fun getUserProfile(
        @Header("authorization") authorization: String,
    ): Response<ResponseBody>

    @GET("/api/v3/search/comic")
    suspend fun search(
        @QueryMap params: Map<String, String>,
    ): Response<ResponseBody>

    @GET("/api/v3/comic2/{path_word}")
    suspend fun getComic(
        @Path("path_word") pathWord: String,
        @QueryMap params: Map<String, String>,
    ): Response<ResponseBody>

    @GET("/api/v3/comic2/{path_word}/group/{group_path_word}/chapters")
    suspend fun getChapters(
        @Path("path_word") pathWord: String,
        @Path("group_path_word") groupPathWord: String,
        @QueryMap params: Map<String, String>,
    ): Response<ResponseBody>

    @GET("/api/v3/comic2/{path_word}/group/{group_path_word}/chapter/{chapter_uuid}")
    suspend fun getChapter(
        @Path("path_word") pathWord: String,
        @Path("group_path_word") groupPathWord: String,
        @Path("chapter_uuid") chapterUuid: String,
        @QueryMap params: Map<String, String>,
    ): Response<ResponseBody>

    @GET("/api/v3/member/collect/comics")
    suspend fun getFavorite(
        @Header("authorization") authorization: String,
        @QueryMap params: Map<String, String>,
    ): Response<ResponseBody>

    @GET
    suspend fun downloadImage(@Url url: String): Response<ResponseBody>
}
