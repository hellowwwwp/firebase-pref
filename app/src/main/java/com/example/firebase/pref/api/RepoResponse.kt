package com.example.study.retrofit.model

import com.google.gson.annotations.SerializedName

/**
 * @author: wangpan
 * @email: p.wang@aftership.com
 * @date: 2021/5/9
 */
data class RepoResponse(
    @SerializedName("items")
    val items: List<RepoItem> = emptyList()
)