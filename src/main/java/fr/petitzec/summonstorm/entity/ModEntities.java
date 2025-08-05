package fr.petitzec.summonstorm.entity;

import fr.petitzec.summonstorm.SummonStorm;
import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SummonStorm.MOD_ID);

    public static final RegistryObject<EntityType<FireSpirit>> FIRE_SPIRIT =
            ENTITY_TYPES.register("fire_spirit", () -> EntityType.Builder.of(FireSpirit::new, MobCategory.CREATURE)
                    .sized(1.5f, 1.5f).build("fire_spirit"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
