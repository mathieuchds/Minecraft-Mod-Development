package fr.petitzec.summonstorm.entity.custom_goals;

import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.server.level.ServerLevel;


import java.util.EnumSet;

public class AvoidNonSneakingPlayerGoal extends Goal {
    private enum State { LOOKING, FLEEING }

    private final FireSpirit fireSpirit;
    private Player targetPlayer = null;
    private int fleeTicks = 0;
    private int lookTicks = 0;
    private final int MAX_LOOK_TICKS = 10;
    private final int MAX_FLEE_TICKS = 40;  // 2 secondes
    private State currentState = State.LOOKING;

    public AvoidNonSneakingPlayerGoal(FireSpirit fireSpirit) {
        this.fireSpirit = fireSpirit;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
        return (currentState == State.LOOKING && lookTicks < MAX_LOOK_TICKS) ||
                (currentState == State.FLEEING && fleeTicks < MAX_FLEE_TICKS);
    }

    @Override
    public void start() {
        fireSpirit.isFleeing = false;
        lookTicks = 0;
        fleeTicks = 0;
        currentState = State.LOOKING;
    }

    @Override
    public void stop() {
        fireSpirit.isFleeing = false;
        targetPlayer = null;
        fleeTicks = 0;
        lookTicks = 0;
        currentState = State.LOOKING;
        fireSpirit.moveTarget = null;
        fireSpirit.runAnimationState.stop();
        fireSpirit.runAnimationTimeout = 0;
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;

        if (currentState == State.LOOKING) {
            lookTicks++;

            if (lookTicks < 10) {
                // Pendant les 10 premiers ticks, le mob regarde le joueur
                fireSpirit.getLookControl().setLookAt(
                        targetPlayer.getX(),
                        targetPlayer.getEyeY(),
                        targetPlayer.getZ(),
                        10.0F, // maxYawChange
                        10.0F  // maxPitchChange
                );

                double dx = targetPlayer.getX() - fireSpirit.getX();
                double dz = targetPlayer.getZ() - fireSpirit.getZ();
                float yaw = (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;
                fireSpirit.setYRot(yaw);
                fireSpirit.yBodyRot = yaw;
                fireSpirit.yHeadRot = yaw;

                fireSpirit.startAttackAnimation();
            }


            if (lookTicks >= MAX_LOOK_TICKS) {
                currentState = State.FLEEING;
                fireSpirit.isFleeing = true;
                fireSpirit.attackAnimationState.stop();
                fireSpirit.attackAnimationTimeout = 0;
            }

        } else if (currentState == State.FLEEING) {
            fleeTicks++;

            fireSpirit.startRunAnimation();
            Vec3 mobPos = fireSpirit.position();
            Vec3 playerPos = targetPlayer.position();
            Vec3 away = mobPos.subtract(playerPos).normalize();

            // Calculer la position cible initiale (fuite)
            Vec3 fleeTarget = mobPos.add(away.scale(8));

            // Récupérer la hauteur du sol sous le mob
            double groundY = fireSpirit.getGroundHeight();

            // Corriger la hauteur cible (pas plus de 5 blocs au-dessus du sol)
            double targetY = Mth.clamp(fleeTarget.y, groundY + 1, groundY + 5);
            Vec3 correctedFleeTarget = new Vec3(fleeTarget.x, targetY, fleeTarget.z);

            // Appliquer le mouvement vers la direction "away", en prenant en compte la hauteur corrigée
            Vec3 delta = correctedFleeTarget.subtract(mobPos).normalize().scale(0.6);
            fireSpirit.setDeltaMovement(delta);

            fireSpirit.setYRot((float) (Math.atan2(away.z, away.x) * (180F / Math.PI)) - 90F);
            fireSpirit.yBodyRot = fireSpirit.getYRot();
            fireSpirit.yHeadRot = fireSpirit.getYRot();
            fireSpirit.moveTarget = null;

            // Fireball
            if (fireSpirit.fireballCooldown <= 0 && fireSpirit.level() instanceof ServerLevel) {
                Vec3 fireballDir = playerPos.subtract(mobPos).normalize().scale(0.5);

                SmallFireball fireball = new SmallFireball(
                        fireSpirit.level(),     // Level
                        fireSpirit,             // Owner
                        fireballDir             // Direction
                );

                fireball.setPos(
                        fireSpirit.getX(),
                        fireSpirit.getY() + fireSpirit.getBbHeight() / 2.0,
                        fireSpirit.getZ()
                );

                fireSpirit.level().addFreshEntity(fireball);
                fireSpirit.fireballCooldown = 40;
            }
        }
    }
}
