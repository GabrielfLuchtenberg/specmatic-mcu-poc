package io.avengers.hq.hero

import org.springframework.stereotype.Repository

@Repository
class HeroRegistry {
    private val heroes = listOf(
        Hero(1, "Tony Stark", "Iron Man", 95, InfinityStoneStatus.SECURED, "Avengers Tower"),
        Hero(2, "Thor Odinson", "God of Thunder", 99, InfinityStoneStatus.SECURED, "New Asgard"),
        Hero(3, "Steve Rogers", "Captain America", 90, InfinityStoneStatus.SECURED, "Brooklyn"),
        Hero(4, "Natasha Romanoff", "Black Widow", 88, InfinityStoneStatus.MISSING, "Unknown"),
    ).associateBy { it.id }

    fun findById(id: Long): Hero? = heroes[id]

    fun powerReport(id: Long): PowerReport? = findById(id)?.let { hero ->
        PowerReport(hero.id, hero.alias, hero.powerLevel, hero.powerLevel < 100 && hero.infinityStoneStatus != InfinityStoneStatus.COMPROMISED)
    }
}
