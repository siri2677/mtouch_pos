package com.kwonps.domain.model.payment


data class VanData(
    val van: String?,
    val vanTrackId: String?,
    val vanId: String?,
    val dptId: String
) {
    constructor(dptId: String) : this(null, null, null, dptId)
}
