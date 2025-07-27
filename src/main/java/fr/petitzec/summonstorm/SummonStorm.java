package fr.petitzec.summonstorm;

import com.mojang.logging.LogUtils;
import fr.petitzec.summonstorm.block.ModBlocks;
import fr.petitzec.summonstorm.item.ModCreativeModeTabs;
import fr.petitzec.summonstorm.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SummonStorm.MOD_ID)
public class SummonStorm {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "summonstorm";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public SummonStorm(FMLJavaModLoadingContext context) {

        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.SUMMONING_SHARD);
            event.accept(ModItems.SUMMONING_GEM);
            event.accept(ModItems.FIRE_SUMMONING_SHARD);
            event.accept(ModItems.FIRE_SUMMONING_GEM);
            event.accept(ModItems.WATER_SUMMONING_SHARD);
            event.accept(ModItems.WATER_SUMMONING_GEM);
            event.accept(ModItems.EARTH_SUMMONING_SHARD);
            event.accept(ModItems.EARTH_SUMMONING_GEM);
            event.accept(ModItems.VERDANT_SUMMONING_SHARD);
            event.accept(ModItems.VERDANT_SUMMONING_GEM);
            event.accept(ModItems.AIR_SUMMONING_SHARD);
            event.accept(ModItems.AIR_SUMMONING_GEM);
        }

        if(event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.RITUAL_STONE);

            event.accept(ModBlocks.INFUSED_STONE);
            event.accept(ModBlocks.FIRE_INFUSED_STONE);
            event.accept(ModBlocks.WATER_INFUSED_STONE);
            event.accept(ModBlocks.EARTH_INFUSED_STONE);
            event.accept(ModBlocks.AIR_INFUSED_STONE);
            event.accept(ModBlocks.VERDANT_INFUSED_STONE);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){

        }
    }
}
