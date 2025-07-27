package fr.petitzec.summonstorm.item;

import com.google.common.eventbus.EventBus;
import fr.petitzec.summonstorm.SummonStorm;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, SummonStorm.MOD_ID);

    public static final RegistryObject<Item> SUMMONING_SHARD = ITEMS.register("summoning_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SUMMONING_GEM = ITEMS.register("summoning_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FIRE_SUMMONING_SHARD = ITEMS.register("fire_summoning_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FIRE_SUMMONING_GEM = ITEMS.register("fire_summoning_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WATER_SUMMONING_SHARD = ITEMS.register("water_summoning_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WATER_SUMMONING_GEM = ITEMS.register("water_summoning_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VERDANT_SUMMONING_SHARD = ITEMS.register("verdant_summoning_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VERDANT_SUMMONING_GEM = ITEMS.register("verdant_summoning_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> EARTH_SUMMONING_SHARD = ITEMS.register("earth_summoning_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> EARTH_SUMMONING_GEM = ITEMS.register("earth_summoning_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> AIR_SUMMONING_SHARD = ITEMS.register("air_summoning_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> AIR_SUMMONING_GEM = ITEMS.register("air_summoning_gem",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
