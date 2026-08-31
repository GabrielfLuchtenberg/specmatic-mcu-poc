package io.avengers.hq

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.web.client.RestClient
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    require(args.size == 1) { "Expected output path" }
    val context = SpringApplicationBuilder(AvengersHqApplication::class.java)
        .run("--server.port=0", "--spring.main.banner-mode=off")
    try {
        val port = (context as ServletWebServerApplicationContext).webServer.port
        val json = RestClient.create().get().uri("http://127.0.0.1:$port/v3/api-docs").retrieve().body(String::class.java)!!
        val tree = ObjectMapper().readTree(json)
        val yaml = ObjectMapper(YAMLFactory())
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .writeValueAsString(tree)
        val output = Path.of(args[0]).toAbsolutePath().normalize()
        Files.createDirectories(output.parent)
        Files.writeString(output, yaml)
    } finally {
        context.close()
    }
}
