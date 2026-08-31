package io.avengers.hq.web

import io.avengers.hq.hero.ApiError
import io.avengers.hq.hero.Hero
import io.avengers.hq.hero.HeroRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
class HeroController(
    private val registry: HeroRegistry,
) {
    @Operation(operationId = "getHeroById", summary = "Retrieve a registered Avenger by id")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hero found in the registry", content = [Content(mediaType = "application/json", schema = Schema(implementation = Hero::class))]),
            ApiResponse(responseCode = "404", description = "Hero not found — perhaps dusted, or never assembled", content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiError::class))]),
        ],
    )
    @GetMapping("/heroes/{id}")
    fun getHero(
        @Parameter(description = "Registered hero id", example = "1", required = true)
        @PathVariable @Min(1) id: Long,
    ): ResponseEntity<Any> {
        val hero: Hero = registry.findById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiError(
                    status = 404,
                    message = "Hero not found",
                    path = ServletUriComponentsBuilder.fromCurrentRequest().build().path,
                ),
            )
        return ResponseEntity.ok(hero)
    }
}
