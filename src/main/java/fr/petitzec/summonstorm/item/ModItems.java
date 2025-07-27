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

    public static final RegistryObject<Item> SUMMONING_SHARD = ITEMS.register("summoningshard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SUMMONINGS_STONE = ITEMS.register("summoningstone",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
