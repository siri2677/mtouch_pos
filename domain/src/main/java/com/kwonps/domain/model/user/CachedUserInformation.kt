package com.kwonps.domain.model.user

/**
 * Represents cached user information persisted locally. Consumers can rely on
 * this sealed type to avoid handling nullable raw JSON strings directly.
 */
sealed class CachedUserInformation {
    data class Raw(val json: String) : CachedUserInformation()
    data class Parsed(val data: UserDetailData) : CachedUserInformation()
    object Empty : CachedUserInformation()
}
