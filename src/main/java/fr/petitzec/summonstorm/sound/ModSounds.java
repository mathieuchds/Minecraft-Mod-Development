package fr.petitzec.summonstorm.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "summonstorm");

    /*public static final RegistryObject<SoundEvent> STORM_CHARGE =
            SOUND_EVENTS.register("storm_charge",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("summonstorm", "storm_charge")));

    public static final RegistryObject<SoundEvent> LIGHTNING_CRACK =
            SOUND_EVENTS.register("lightning_crack",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("summonstorm", "lightning_crack")));
    */
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
