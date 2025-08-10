package fr.petitzec.summonstorm.worldgen;

import fr.petitzec.summonstorm.SummonStorm;
import fr.petitzec.summonstorm.entity.ModEntities;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

import static net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS;


public class ModBiomeModifiers {

    public static final ResourceKey<BiomeModifier> ADD_INFUSED_STONE = registerKey("add_infused_stone");
    public static final ResourceKey<BiomeModifier> ADD_FIRE_INFUSED_STONE = registerKey("add_fire_infused_stone");
    public static final ResourceKey<BiomeModifier> ADD_WATER_INFUSED_STONE = registerKey("add_water_infused_stone");
    public static final ResourceKey<BiomeModifier> ADD_AIR_INFUSED_STONE = registerKey("add_air_infused_stone");
    public static final ResourceKey<BiomeModifier> ADD_EARTH_INFUSED_STONE = registerKey("add_earth_infused_stone");
    public static final ResourceKey<BiomeModifier> ADD_VERDANT_INFUSED_STONE = registerKey("add_verdant_infused_stone");
    public static final ResourceKey<BiomeModifier> SPAWN_FIRE_SPIRIT = registerKey("spawn_fire_spirit");

    /*public static final ResourceKey<BiomeModifier> ADD_NETHER_ALEXANDRITE_ORE = registerKey("add_nether_alexandrite_ore");
    public static final ResourceKey<BiomeModifier> ADD_END_ALEXANDRITE_ORE = registerKey("add_end_alexandrite_ore");*/

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeature = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        TagKey<Biome> FIRE_BIOMES = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath("summonstorm", "fire_biomes")
        );

        TagKey<Biome> AIR_BIOMES = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath("summonstorm", "air_biomes")
        );

        TagKey<Biome> WATER_BIOMES = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath("summonstorm", "water_biomes")
        );

        TagKey<Biome> EARTH_BIOMES = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath("summonstorm", "earth_biomes")
        );

        TagKey<Biome> VERDANT_BIOMES = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath("summonstorm", "verdant_biomes")
        );

        //System.out.println("Fire biomes: " + biomes.getOrThrow(FIRE_BIOMES).stream().toList());



        context.register(ADD_INFUSED_STONE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeature.getOrThrow(ModPlacedFeatures.INFUSED_STONE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_FIRE_INFUSED_STONE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(FIRE_BIOMES),
                HolderSet.direct(placedFeature.getOrThrow(ModPlacedFeatures.FIRE_INFUSED_STONE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_WATER_INFUSED_STONE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(WATER_BIOMES),
                HolderSet.direct(placedFeature.getOrThrow(ModPlacedFeatures.WATER_INFUSED_STONE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_AIR_INFUSED_STONE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(AIR_BIOMES),
                HolderSet.direct(placedFeature.getOrThrow(ModPlacedFeatures.AIR_INFUSED_STONE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_VERDANT_INFUSED_STONE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(VERDANT_BIOMES),
                HolderSet.direct(placedFeature.getOrThrow(ModPlacedFeatures.VERDANT_INFUSED_STONE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_EARTH_INFUSED_STONE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(EARTH_BIOMES),
                HolderSet.direct(placedFeature.getOrThrow(ModPlacedFeatures.EARTH_INFUSED_STONE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(SPAWN_FIRE_SPIRIT, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(FIRE_BIOMES),
                List.of(new MobSpawnSettings.SpawnerData(ModEntities.FIRE_SPIRIT.get(), 25, 1, 4))));

    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(SummonStorm.MOD_ID, name));
    }

}