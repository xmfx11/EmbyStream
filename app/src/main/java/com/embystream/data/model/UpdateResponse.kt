package com.embystream.data.model

data class UpdateResponse(
    val tag_name: String?,
    val name: String?,
    val body: String?,
    val assets: List<UpdateAsset>?,
    val published_at: String?
)

data class UpdateAsset(
    val name: String?,
    val browser_download_url: String?
)
