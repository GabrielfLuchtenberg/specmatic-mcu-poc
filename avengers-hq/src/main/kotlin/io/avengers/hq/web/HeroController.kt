package io.avengers.hq.web

import io.avengers.hq.hero.ApiError
import io.avengers.hq.hero.Hero
import io.avengers.hq.hero.HeroRegistry
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
    @GetMapping("/heroes/{id}")
    fun getHero(@PathVariable id: Long): ResponseEntity<Any> {
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
