package fr.petitzec.summonstorm.worldgen;

import fr.petitzec.summonstorm.SummonStorm;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES =
            DeferredRegister.create(Registries.CONFIGURED_FEATURE, SummonStorm.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES =
            DeferredRegister.create(Registries.PLACED_FEATURE, SummonStorm.MOD_ID);

    // Tu peux enregistrer des "dummy" ici si besoin, mais Forge s’occupera des JSON à la place.
    // Pas besoin de RegistryObject si tu fais tout par data gen, mais ça force Forge à les reconnaître.
}
