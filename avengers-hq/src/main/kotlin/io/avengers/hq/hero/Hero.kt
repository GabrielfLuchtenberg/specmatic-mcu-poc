package io.avengers.hq.hero

data class Hero(
    val id: Long,
    val name: String,
    val alias: String,
    val powerLevel: Int,
    val infinityStoneStatus: InfinityStoneStatus,
    val location: String? = null,
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

data class PowerReport(
    val heroId: Long,
    val alias: String,
    val powerLevel: Int,
    val snapViable: Boolean,
)
