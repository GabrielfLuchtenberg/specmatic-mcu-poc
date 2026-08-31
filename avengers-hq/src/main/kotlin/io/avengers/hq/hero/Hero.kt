package io.avengers.hq.hero

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Hero(
    val id: Long,
    val name: String,
    val alias: String,
    val powerLevel: Int,
    val infinityStoneStatus: InfinityStoneStatus,
    val location: String? = null,
    val team: String? = null,
)

enum class InfinityStoneStatus {
    SECURED,
    MISSING,
    COMPROMISED,
}

data class ApiError(
    val status: Int,
    val message: String,
    val path: String? = null,
)
