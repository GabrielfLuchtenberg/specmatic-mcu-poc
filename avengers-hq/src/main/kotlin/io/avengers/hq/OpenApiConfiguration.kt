package io.avengers.hq

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {
    @Bean
    fun serviceOpenApi(): OpenAPI = OpenAPI()
        .info(
            Info().title("Avengers HQ Superhero Registry")
                .description(
                    """Executable contract shared by Avengers HQ (provider) and Thanos Gauntlet
                    |(consumer). Specmatic uses it both to test the real provider and to run a
                    |mock that the consumer tests call.""".trimMargin() + "\n",
                )
                .version("1.0.0"),
        )
        .servers(listOf(Server().url("http://localhost:8080").description("Local Avengers HQ")))
        .components(Components())

    @Bean
    fun specmaticExamples(): OpenApiCustomizer = OpenApiCustomizer { api ->
        val operation = api.paths["/heroes/{id}"]!!.get
        operation.tags = null
        val parameterExamples = linkedMapOf(
            "IRON_MAN_200_OK" to Example().value(1),
            "THOR_200_OK" to Example().value(2),
            "UNKNOWN_HERO_404" to Example().value(666),
        )
        operation.parameters.single { it.name == "id" }.apply {
            description = null
            example = null
            examples = parameterExamples
        }
        api.components.schemas["Hero"]!!.properties["powerLevel"]!!.format = null
        api.components.schemas["Error"]!!.properties["status"]!!.format = null
        api.components.schemas["Hero"]!!.required = listOf("id", "name", "alias", "powerLevel", "infinityStoneStatus")
        api.components.schemas["Error"]!!.required = listOf("status", "message")
        operation.responses["200"]!!.content["application/json"]!!.examples = linkedMapOf(
            "IRON_MAN_200_OK" to Example().value(linkedMapOf("id" to 1, "name" to "Tony Stark", "alias" to "Iron Man", "powerLevel" to 95, "infinityStoneStatus" to "SECURED", "location" to "Avengers Tower")),
            "THOR_200_OK" to Example().value(linkedMapOf("id" to 2, "name" to "Thor Odinson", "alias" to "God of Thunder", "powerLevel" to 99, "infinityStoneStatus" to "SECURED", "location" to "New Asgard")),
        )
        operation.responses["404"]!!.content["application/json"]!!.examples = linkedMapOf(
            "UNKNOWN_HERO_404" to Example().value(linkedMapOf("status" to 404, "message" to "Hero not found", "path" to "/heroes/666")),
        )
    }
}
