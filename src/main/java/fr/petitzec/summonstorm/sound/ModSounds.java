package fr.petitzec.summonstorm.sound;

import fr.petitzec.summonstorm.SummonStorm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SummonStorm.MOD_ID);


    public static final RegistryObject<SoundEvent> WEATHER_STAFF_USE = registerSoundEvent("weather_staff_use");
    public static final RegistryObject<SoundEvent> WEATHER_STAFF_USE_END_SUN = registerSoundEvent("weather_staff_use_end_sun");
    public static final RegistryObject<SoundEvent> WEATHER_STAFF_USE_END_RAIN = registerSoundEvent("weather_staff_use_end_rain");

    /*public static final RegistryObject<SoundEvent> MAGIC_BLOCK_BREAK = registerSoundEvent("magic_block_break");
    public static final RegistryObject<SoundEvent> MAGIC_BLOCK_STEP = registerSoundEvent("magic_block_step");
    public static final RegistryObject<SoundEvent> MAGIC_BLOCK_PLACE = registerSoundEvent("magic_block_place");
    public static final RegistryObject<SoundEvent> MAGIC_BLOCK_HIT = registerSoundEvent("magic_block_hit");
    public static final RegistryObject<SoundEvent> MAGIC_BLOCK_FALL = registerSoundEvent("magic_block_fall");

    public static final ForgeSoundType MAGIC_BLOCK_SOUNDS = new ForgeSoundType(1f, 1f,
            ModSounds.MAGIC_BLOCK_BREAK, ModSounds.MAGIC_BLOCK_STEP, ModSounds.MAGIC_BLOCK_PLACE,
            ModSounds.MAGIC_BLOCK_HIT, ModSounds.MAGIC_BLOCK_FALL);*/



    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SummonStorm.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
