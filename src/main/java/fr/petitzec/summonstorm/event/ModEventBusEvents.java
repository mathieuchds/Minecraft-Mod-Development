package fr.petitzec.summonstorm.event;

import fr.petitzec.summonstorm.SummonStorm;
import fr.petitzec.summonstorm.entity.ModEntities;
import fr.petitzec.summonstorm.entity.client.FireSpiritModel;
import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SummonStorm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FireSpiritModel.LAYER_LOCATION, FireSpiritModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FIRE_SPIRIT.get(), FireSpirit.createAttributes().build());
    }
}
