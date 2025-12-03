package com.kwonps.domain.parser

import com.kwonps.domain.model.user.UserDetailData

/**
 * Abstraction for parsing user detail information. Implementations may rely on
 * different JSON libraries or formats; callers receive null when parsing fails
 * or the payload is absent.
 */
interface UserDetailParser {
    fun parse(json: String?): UserDetailData?
}
