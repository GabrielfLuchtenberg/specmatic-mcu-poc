package io.avengers.hq

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiGenerationTest(@Autowired private val rest: TestRestTemplate, @LocalServerPort private val port: Int) {
    @Test
    fun `generated document preserves executable semantics`() {
        val document = rest.getForObject("http://localhost:$port/v3/api-docs", Map::class.java)
        val text = document.toString()
        assertThat(text).contains("getHeroById", "IRON_MAN_200_OK", "UNKNOWN_HERO_404", "powerLevel", "minimum=0", "maximum=100")
        assertThat(text).contains("SECURED", "MISSING", "COMPROMISED", "additionalProperties=false")
    }
}
