package com.example.study.retrofit.model

import com.google.gson.annotations.SerializedName

/**
 * @author: wangpan
 * @email: p.wang@aftership.com
 * @date: 2021/5/9
 */
data class RepoItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("stargazers_count")
    val starCount: Int
)