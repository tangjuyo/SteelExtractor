package com.steelextractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import com.steelextractor.SteelExtractor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer
import net.minecraft.world.attribute.EnvironmentAttribute

/**
 * Extracts the built-in `ENVIRONMENT_ATTRIBUTE` registry: attribute type, encoded default value,
 * behavior flags, and float value range.
 *
 * `AttributeRange` is an anonymous interface implementation, so float bounds are probed through
 * `sanitizeValue`: sanitizing an infinity yields the matching bound, or the infinity itself when
 * that side is unbounded (omitted from the output).
 */
class EnvironmentAttributes : SteelExtractor.Extractor {
    override fun fileName(): String {
        return "steel-registry/build_assets/environment_attributes.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val attributes = JsonArray()
        for (attribute in BuiltInRegistries.ENVIRONMENT_ATTRIBUTE) {
            attributes.add(attributeJson(attribute))
        }

        val topLevelJson = JsonObject()
        topLevelJson.add("environment_attributes", attributes)
        return topLevelJson
    }

    private fun <Value : Any> attributeJson(attribute: EnvironmentAttribute<Value>): JsonObject {
        val json = JsonObject()
        json.addProperty("name", BuiltInRegistries.ENVIRONMENT_ATTRIBUTE.getKey(attribute).toString())
        json.addProperty("type", BuiltInRegistries.ATTRIBUTE_TYPE.getKey(attribute.type()).toString())
        json.add(
            "default",
            attribute.valueCodec().encodeStart(JsonOps.INSTANCE, attribute.defaultValue()).getOrThrow()
        )
        json.addProperty("syncable", attribute.isSyncable)
        json.addProperty("positional", attribute.isPositional)
        json.addProperty("spatially_interpolated", attribute.isSpatiallyInterpolated)
        json.addProperty("full_resolution_biomes", attribute.isFullResolutionBiomes)

        if (attribute.defaultValue() is Float) {
            @Suppress("UNCHECKED_CAST")
            val floatAttribute = attribute as EnvironmentAttribute<Float>
            val min = floatAttribute.sanitizeValue(Float.NEGATIVE_INFINITY)
            val max = floatAttribute.sanitizeValue(Float.POSITIVE_INFINITY)
            if (min.isFinite()) {
                json.addProperty("min", min)
            }
            if (max.isFinite()) {
                json.addProperty("max", max)
            }
        }

        return json
    }
}
