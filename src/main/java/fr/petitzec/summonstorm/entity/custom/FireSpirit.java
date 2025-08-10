package fr.petitzec.summonstorm.entity.custom;

import fr.petitzec.summonstorm.entity.custom_goals.AvoidNonSneakingPlayerGoal;
import fr.petitzec.summonstorm.entity.custom_goals.FireSpiritSeekLavaGoal;
import fr.petitzec.summonstorm.entity.custom_goals.FireSpiritWanderNearLavaGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;


import java.util.EnumSet;
import java.util.Random;

public class FireSpirit extends FlyingMob {

    public Vec3 moveTarget = Vec3.ZERO;
    private Vec3 lastPosition = Vec3.ZERO;
    private int stuckCounter = 0;
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    public boolean isFleeing = false;
    public int fireballCooldown = 0;


    public FireSpirit(EntityType<? extends FireSpirit> type, Level world) {
        super(type, world);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.lookControl = new MyFlyingLookControl(this);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FLYING_SPEED, 0.31D);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AvoidNonSneakingPlayerGoal(this));
        this.goalSelector.addGoal(1, new FireSpiritSeekLavaGoal(this));
        this.goalSelector.addGoal(2, new FireSpiritWanderNearLavaGoal(this));
        this.goalSelector.addGoal(3, new FlyAroundGoal());
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        return false; // Ne prend jamais de dégâts de chute
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new FlyingPathNavigation(this, world);
    }





    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (fireballCooldown > 0) {
            fireballCooldown--;
        }
        Vec3 currentPosition = this.position();
        if (currentPosition.distanceTo(lastPosition) < 0.05) {
            stuckCounter++;
            if (stuckCounter > 20) { // bloqué depuis 20 ticks (~1 seconde)
                this.moveTarget = null; // forcera la FlyAroundGoal à choisir une nouvelle cible
                stuckCounter = 0;
            }
        } else {
            stuckCounter = 0;
        }
        lastPosition = currentPosition;

    }

    // Sons, taille, etc. à définir selon besoin
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT; // ou un son perso
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    private void setupAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 40;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    public double getGroundHeight() {
        BlockPos pos = this.blockPosition();
        Level level = this.level();

        while (pos.getY() > level.getMinBuildHeight() && level.getBlockState(pos.below()).isAir()) {
            pos = pos.below();
        }

        return pos.getY();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    /*public static boolean canSpawnAtNight(EntityType<FireSpirit> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        // Vérifie qu'il fait nuit
        boolean isNight = !level.getLevel().isDay();

        // Vérifie les règles classiques de spawn d’animal (solide au sol, etc.)
        boolean canSpawnNormally = Animal.checkAnimalSpawnRules(type, level, reason, pos, random);

        return isNight && canSpawnNormally;
    }*/


    // Exemple simple de contrôle du mouvement
    class MyFlyingMoveControl extends MoveControl {
        public MyFlyingMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (!FireSpirit.this.isNoGravity()) {
                FireSpirit.this.setNoGravity(true);
            }

            if (FireSpirit.this.moveTarget != null) {
                double dx = moveTarget.x - FireSpirit.this.getX();
                double dy = moveTarget.y - FireSpirit.this.getY();
                double dz = moveTarget.z - FireSpirit.this.getZ();

                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist < 1.0) {
                    FireSpirit.this.setDeltaMovement(FireSpirit.this.getDeltaMovement().scale(0.5));
                    return;
                }

                double speed = FireSpirit.this.getAttributeValue(Attributes.FLYING_SPEED);
                double vx = dx / dist * speed;
                double vy = dy / dist * speed;
                double vz = dz / dist * speed;

                FireSpirit.this.setDeltaMovement(new Vec3(vx, vy, vz));
                FireSpirit.this.setYRot((float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90F);
                FireSpirit.this.yBodyRot = FireSpirit.this.getYRot();
                FireSpirit.this.yBodyRot = FireSpirit.this.getYRot();
                FireSpirit.this.yHeadRot = FireSpirit.this.getYRot();

            }
        }
    }


    class MyFlyingLookControl extends LookControl {
        public MyFlyingLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            // Optionnel, gérer la rotation de la tête si besoin
        }
    }


    public class FlyAroundGoal extends Goal {

        private static final int RANGE = 8; // distance max dans chaque direction
        private static final double HEIGHT_ABOVE_GROUND_MIN = 1.0;
        private static final double HEIGHT_ABOVE_GROUND_MAX = 5.0;

        public FlyAroundGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !FireSpirit.this.isFleeing;
        }

        @Override
        public void tick() {
            System.out.println("fly around");
            if (FireSpirit.this.moveTarget == null ||
                    FireSpirit.this.position().distanceTo(FireSpirit.this.moveTarget) < 2.0) {
                setNewRandomDirection();
            }
            FireSpirit.this.getNavigation().moveTo(moveTarget.x + 0.5, moveTarget.y + 1, moveTarget.z + 0.5, 1.0);
        }

        private void setNewRandomDirection() {
            RandomSource random = FireSpirit.this.getRandom();
            double groundY = FireSpirit.this.getGroundHeight();

            // Position cible aléatoire dans un cube autour du mob
            double x = FireSpirit.this.getX() + (random.nextDouble() - 0.5) * 2 * RANGE;
            double y = FireSpirit.this.getY() + (random.nextDouble() - 0.5) * 2 * RANGE;
            double z = FireSpirit.this.getZ() + (random.nextDouble() - 0.5) * 2 * RANGE;

            // Clamp la hauteur pour rester dans une zone autorisée
            double minY = groundY + HEIGHT_ABOVE_GROUND_MIN;
            double maxY = groundY + HEIGHT_ABOVE_GROUND_MAX;
            y = Mth.clamp(y, minY, maxY);

            Vec3 target = new Vec3(x, y, z);
            if (FireSpirit.this.level().noCollision(FireSpirit.this.getBoundingBox().move(target.subtract(FireSpirit.this.position())))) {
                FireSpirit.this.moveTarget = target;
            }else{
                FireSpirit.this.moveTarget = FireSpirit.this.position();
            }
        }
    }

}
