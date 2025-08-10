package fr.petitzec.summonstorm.event.custom_events;

import fr.petitzec.summonstorm.SummonStorm;
import fr.petitzec.summonstorm.entity.ModEntities;
import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SummonStorm.MOD_ID)
public class NightSpawnHandler {

    private static boolean nightStarted = false;
    private static long nightStartTime = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;

        ServerLevel level = (ServerLevel) event.level;
        long timeOfDay = level.getDayTime() % 24000; // 0 = lever du jour, 13000 = début de nuit

        // Début de nuit
        if (!nightStarted && timeOfDay >= 13000 && timeOfDay < 14000) {
            nightStarted = true;
            nightStartTime = level.getGameTime();
        }

        // Retour au jour → reset
        if (timeOfDay < 13000 && nightStarted) {
            nightStarted = false;
        }

        // Pendant la première minute de nuit (1200 ticks)
        if (nightStarted && level.getGameTime() - nightStartTime <= 1200) {
            for (ServerPlayer player : level.players()) {
                if (level.random.nextFloat() < 0.005f) { // 0.5% de chance par tick par joueur
                    spawnFireSpiritNear(level, player);
                }
            }
        }
    }

    private static void spawnFireSpiritNear(ServerLevel level, ServerPlayer player) {
        double x = player.getX() + (level.random.nextDouble() - 0.5) * 20;
        double z = player.getZ() + (level.random.nextDouble() - 0.5) * 20;

        // Cherche une hauteur adaptée
        BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos((int) x, 0, (int) z));
        double y = groundPos.getY() + 4 + level.random.nextInt(5); // 4-8 blocs au-dessus du sol

        FireSpirit fireSpirit = new FireSpirit(ModEntities.FIRE_SPIRIT.get(), level);
        fireSpirit.moveTo(x, y, z, level.random.nextFloat() * 360F, 0F);
        level.addFreshEntity(fireSpirit);
    }
}

