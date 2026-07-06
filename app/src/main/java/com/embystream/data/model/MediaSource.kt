package com.embystream.data.model

data class MediaSource(
    val Id: String?,
    val DirectStreamUrl: String?,
    val Path: String?
)

data class PlaybackInfoResponse(
    val MediaSources: List<MediaSource>?
)
