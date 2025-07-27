package fr.petitzec.summonstorm.item;

import fr.petitzec.summonstorm.SummonStorm;
import fr.petitzec.summonstorm.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODS_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SummonStorm.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SUMMONING_ITEM_TAB = CREATIVE_MODS_TABS.register("summoning_items_tab",
            () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.SUMMONING_SHARD.get()))
                    .title(Component.translatable("creativetab.summonstorm.summoning_items"))
                    .displayItems((itemDisplayParameters, output)-> {

                        // Items
                        output.accept(ModItems.SUMMONING_SHARD.get());
                        output.accept(ModItems.SUMMONING_GEM.get());
                        output.accept(ModItems.FIRE_SUMMONING_SHARD.get());
                        output.accept(ModItems.FIRE_SUMMONING_GEM.get());
                        output.accept(ModItems.WATER_SUMMONING_SHARD.get());
                        output.accept(ModItems.WATER_SUMMONING_GEM.get());
                        output.accept(ModItems.EARTH_SUMMONING_SHARD.get());
                        output.accept(ModItems.EARTH_SUMMONING_GEM.get());
                        output.accept(ModItems.VERDANT_SUMMONING_SHARD.get());
                        output.accept(ModItems.VERDANT_SUMMONING_GEM.get());
                        output.accept(ModItems.AIR_SUMMONING_SHARD.get());
                        output.accept(ModItems.AIR_SUMMONING_GEM.get());


                        // Blocs
                        output.accept(ModBlocks.RITUAL_STONE.get());

                        output.accept(ModBlocks.INFUSED_STONE.get());
                        output.accept(ModBlocks.FIRE_INFUSED_STONE.get());
                        output.accept(ModBlocks.WATER_INFUSED_STONE.get());
                        output.accept(ModBlocks.AIR_INFUSED_STONE.get());
                        output.accept(ModBlocks.EARTH_INFUSED_STONE.get());
                        output.accept(ModBlocks.VERDANT_INFUSED_STONE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODS_TABS.register(eventBus);
    }
}
