package fr.petitzec.summonstorm.sound;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class ChargingSoundInstance extends AbstractTickableSoundInstance {

    private final LocalPlayer player;
    private boolean stopped = false;

    public ChargingSoundInstance(SoundEvent sound, LocalPlayer player) {
        super(sound, SoundSource.PLAYERS, RandomSource.create(player.getRandom().nextLong()));
        this.player = player;
        this.looping = true;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        // Suivre le joueur
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();

        // Si le joueur arrête d’utiliser l’item, on demande l’arrêt du son
        if (!player.isUsingItem()) {
            this.stop(); // On ne l’override pas, on l’utilise
        }
    }
}
