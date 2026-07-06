package com.embystream.data.model

data class EmbyItem(
    val Id: String?,
    val Name: String?,
    val Type: String?,
    val Overview: String?,
    val ProductionYear: Int?,
    val CommunityRating: Double?,
    val Tags: List<String>?,
    val People: List<Person>?,
    val MediaSources: List<MediaSource>?,
    val ImageTags: ImageTags?
)

data class ImageTags(
    val Primary: String?
)

data class Person(
    val Id: String?,
    val Name: String?,
    val Role: String?,
    val PrimaryImageTag: String?
)

data class ViewsResponse(
    val Items: List<EmbyItem>?
)

data class ItemsResponse(
    val Items: List<EmbyItem>?
)
