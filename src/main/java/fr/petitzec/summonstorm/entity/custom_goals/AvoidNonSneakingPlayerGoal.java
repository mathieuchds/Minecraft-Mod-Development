package fr.petitzec.summonstorm.entity.custom_goals;

import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AvoidNonSneakingPlayerGoal extends Goal {
    private final FireSpirit fireSpirit;
    private Player targetPlayer = null;
    private int fleeTicks = 0;
    private final int MAX_FLEE_TICKS = 40; // Durée de la fuite (2s)

    public AvoidNonSneakingPlayerGoal(FireSpirit fireSpirit) {
        this.fireSpirit = fireSpirit;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Player player = fireSpirit.level().getNearestPlayer(fireSpirit, 8.0);
        if (player != null && !player.isCrouching()) {
            targetPlayer = player;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return fleeTicks < MAX_FLEE_TICKS;
    }

    @Override
    public void start() {
        fireSpirit.isFleeing = true;
        fleeTicks = 0;
    }

    @Override
    public void stop() {
        fireSpirit.isFleeing = false;
        targetPlayer = null;
        fleeTicks = 0;
        fireSpirit.moveTarget = null;
    }

    @Override
    public void tick() {
        fleeTicks++;
        if (targetPlayer == null) return;

        Vec3 mobPos = fireSpirit.position();
        Vec3 playerPos = targetPlayer.position();

        Vec3 away = mobPos.subtract(playerPos).normalize();
        fireSpirit.setDeltaMovement(away.scale(0.6)); // fuite rapide

        // Tourner dans la bonne direction
        float yaw = (float) (Math.atan2(away.z, away.x) * (180F / Math.PI)) - 90F;
        fireSpirit.setYRot(yaw);
        fireSpirit.yBodyRot = yaw;
        fireSpirit.yHeadRot = yaw;

        fireSpirit.moveTarget = null; // empêche le vol aléatoire
    }
}




