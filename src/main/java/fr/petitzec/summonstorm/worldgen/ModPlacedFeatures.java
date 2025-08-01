package fr.petitzec.summonstorm.worldgen;

import fr.petitzec.summonstorm.SummonStorm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> INFUSED_STONE_PLACED_KEY = registerKey("infused_stone_placed");
    //public static final ResourceKey<PlacedFeature> NETHER_ALEXANDRITE_ORE_PLACED_KEY = registerKey("nether_alexandrite_ore_placed");
    //public static final ResourceKey<PlacedFeature> END_ALEXANDRITE_ORE_PLACED_KEY = registerKey("end_alexandrite_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {

        System.out.println("---- Placed Features registered ----");

        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, INFUSED_STONE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.INFUSED_STONE_KEY),
                ModOrePlacement.commonOrePlacement(12,
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80))));
        /*register(context, NETHER_ALEXANDRITE_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.NETHER_ALEXANDRITE_ORE_KEY),
                ModOrePlacement.commonOrePlacement(12,
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80))));
        register(context, END_ALEXANDRITE_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.END_ALEXANDRITE_ORE_KEY),
                ModOrePlacement.commonOrePlacement(12,
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80))));*/

    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(SummonStorm.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        System.out.println("register placed");
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
        System.out.println("register placed after");
    }

    /*public static final DeferredRegister<PlacedFeature> PLACED_FEATURES =
            DeferredRegister.create(Registries.PLACED_FEATURE, SummonStorm.MOD_ID);

    public static final RegistryObject<PlacedFeature> INFUSED_STONE_PLACED =
            PLACED_FEATURES.register("infused_stone_placed", () ->
                    new PlacedFeature(Holder.direct(null), List.of())
            );


    public static void register(IEventBus eventBus) {
        PLACED_FEATURES.register(eventBus);
    }*/


}