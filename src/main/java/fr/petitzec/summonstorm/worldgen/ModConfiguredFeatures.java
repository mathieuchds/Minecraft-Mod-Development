package fr.petitzec.summonstorm.worldgen;

import fr.petitzec.summonstorm.SummonStorm;
import fr.petitzec.summonstorm.block.ModBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> INFUSED_STONE_KEY = registerKey("infused_stone");
    //public static final ResourceKey<ConfiguredFeature<?, ?>> INFUSED_DEEPSLATE_KEY = registerKey("infused_deepslate");
    //public static final ResourceKey<ConfiguredFeature<?, ?>> NETHER_ALEXANDRITE_ORE_KEY = registerKey("nether_alexandrite_ore");
    //public static final ResourceKey<ConfiguredFeature<?, ?>> END_ALEXANDRITE_ORE_KEY = registerKey("end_alexandrite_ore");


    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        //RuleTest netherrackReplaceables = new BlockMatchTest(Blocks.NETHERRACK);
        //RuleTest endReplaceables = new BlockMatchTest(Blocks.END_STONE);

        List<OreConfiguration.TargetBlockState> overworldInfusedOres = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.INFUSED_STONE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.INFUSED_DEEPSLATE.get().defaultBlockState()));

        register(context, INFUSED_STONE_KEY, Feature.ORE, new OreConfiguration(overworldInfusedOres, 9));
        /*register(context, NETHER_ALEXANDRITE_ORE_KEY, Feature.ORE, new OreConfiguration(netherrackReplaceables,
                ModBlocks.ALEXANDRITE_NETHER_ORE.get().defaultBlockState(), 9));
        register(context, END_ALEXANDRITE_ORE_KEY, Feature.ORE, new OreConfiguration(endReplaceables,
                ModBlocks.ALEXANDRITE_END_ORE.get().defaultBlockState(), 9));*/

    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(SummonStorm.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}