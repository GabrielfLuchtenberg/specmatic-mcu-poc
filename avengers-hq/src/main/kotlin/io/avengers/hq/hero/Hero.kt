package io.avengers.hq.hero

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@JsonIgnoreProperties(ignoreUnknown = false)
@Schema(name = "Hero", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
data class Hero(
    @field:Schema(format = "int64")
    val id: Long,
    val name: String,
    val alias: String,
    @field:Min(0)
    @field:Max(100)
    @field:Schema(description = "SHIELD-rated combat power. Renaming this field is a breaking change.", minimum = "0", maximum = "100")
    val powerLevel: Int,
    val infinityStoneStatus: InfinityStoneStatus,
    @field:Schema(description = "Optional last-known location.", nullable = true)
    val location: String? = null,
)

@Schema(enumAsRef = true)
enum class InfinityStoneStatus {
    SECURED,
    MISSING,
    COMPROMISED,
}

@JsonIgnoreProperties(ignoreUnknown = false)
@Schema(name = "Error", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
data class ApiError(
    val status: Int,
    val message: String,
    val path: String? = null,
)
