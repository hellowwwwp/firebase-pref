package com.example.study.retrofit

import com.example.study.retrofit.model.RepoResponse
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * @author: wangpan
 * @email: p.wang@aftership.com
 * @date: 2022/2/22
 */
interface RetrofitApi {

    @GET("search1/repositories?sort=stars&per_page=1&page=1")
    fun searchRepos_call(@Query("q") keyword: String): Call<RepoResponse>

    @GET("search/repositories?sort=stars&per_page=1&page=1")
    fun searchRepos_rxjava(@Query("q") keyword: String): Single<RepoResponse>

    companion object {

        private const val BASE_URL = "https://api.github.com/"

        private fun createOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build()
        }

        fun create(): RetrofitApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
                .create(RetrofitApi::class.java)
        }

    }

}