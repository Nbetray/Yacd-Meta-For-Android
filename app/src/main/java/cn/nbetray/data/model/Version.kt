package cn.nbetray.data.model

import com.google.gson.annotations.SerializedName

data class VersionInfo(
    val version: String,
    val meta: Boolean = false,
    @SerializedName("premium")
    val premium: Boolean = false
)
