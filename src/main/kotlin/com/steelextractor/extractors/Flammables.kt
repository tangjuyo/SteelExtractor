package com.steelextractor.extractors

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.steelextractor.SteelExtractor
import it.unimi.dsi.fastutil.objects.Object2IntMap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FireBlock

/**
 * Extracts `FireBlock`'s private `igniteOdds` / `burnOdds` tables, populated by
 * `FireBlock.bootStrap`, keyed by block path in block registry order.
 */
class Flammables : SteelExtractor.Extractor {
    override fun fileName(): String {
        return "steel-core/build/flammables.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val fire = Blocks.FIRE as FireBlock
        val igniteOdds = readOdds(fire, "igniteOdds")
        val burnOdds = readOdds(fire, "burnOdds")

        val topLevelJson = JsonObject()
        for (block in BuiltInRegistries.BLOCK) {
            if (!igniteOdds.containsKey(block) && !burnOdds.containsKey(block)) {
                continue
            }

            val entry = JsonObject()
            entry.addProperty("ignite_odds", igniteOdds.getInt(block))
            entry.addProperty("burn_odds", burnOdds.getInt(block))
            topLevelJson.add(BuiltInRegistries.BLOCK.getKey(block).path, entry)
        }

        return topLevelJson
    }

    private fun readOdds(fire: FireBlock, fieldName: String): Object2IntMap<Block> {
        val field = FireBlock::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(fire) as Object2IntMap<Block>
    }
}
