package fr.petitzec.summonstorm.entity.custom;

import fr.petitzec.summonstorm.entity.custom_goals.AvoidNonSneakingPlayerGoal;
import fr.petitzec.summonstorm.entity.custom_goals.AvoidWaterGoal;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
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
    public boolean isFleeing = false;
    public int fireballCooldown = 0;
    public int despawnTimer = -1; // -1 = pas de despawn prévu

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState despawnAnimationState = new AnimationState();
    public final AnimationState spawnAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();

    private int idleAnimationTimeout = 0;
    public int despawnAnimationTimeout = 0;
    public int spawnAnimationTimeout = 0;
    public int attackAnimationTimeout = 0;
    public int runAnimationTimeout = 0;
    public int walkAnimationTimeout = 0;


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
        this.goalSelector.addGoal(3, new AvoidWaterGoal(this)); // ajouté ici
        this.goalSelector.addGoal(4, new FlyAroundGoal());
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
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

        if(this.level().isClientSide()) {
            this.setupAnimationStates();
        }

        long timeOfDay = this.level().getDayTime() % 24000;
        boolean nuit = timeOfDay >= 13000 || timeOfDay < 0; // vrai nuit sombre

        // Si c'est le jour et pas encore de timer → planifier un despawn
        if (!nuit && despawnTimer == -1) {
            despawnTimer = this.random.nextInt(1200) + 100;
        }

        // Si un timer est actif → décompte
        if (despawnTimer > 0) {
            despawnTimer--;

            // Début de l'animation quand il reste 20 ticks (1 seconde)
            if (despawnTimer == 40) {
                startDespawnAnimation();
            }

            // Quand timer fini → supprimer l'entité
            if (despawnTimer == 0) {
                this.discard();
            }
        }

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
        boolean playingOtherAnim =
                this.attackAnimationTimeout > 0 ||
                        this.despawnAnimationTimeout > 0 ||
                        this.spawnAnimationTimeout > 0 ||
                        this.walkAnimationTimeout > 0 ||
                        this.runAnimationTimeout > 0;

        // Idle seulement si aucune autre anim
        if (!playingOtherAnim) {
            if (this.idleAnimationTimeout <= 0) {
                this.idleAnimationTimeout = 80;
                this.idleAnimationState.start(this.tickCount);
            } else {
                --this.idleAnimationTimeout;
            }
        }

        if (this.attackAnimationTimeout > 0) {
            --this.attackAnimationTimeout;
        }

        if (this.despawnAnimationTimeout > 0) {
            --this.despawnAnimationTimeout;
        }

        if (this.spawnAnimationTimeout > 0) {
            --this.spawnAnimationTimeout;
        }

        if (this.walkAnimationTimeout > 0) {
            --this.walkAnimationTimeout;
        }

        if (this.runAnimationTimeout > 0) {
            --this.runAnimationTimeout;
        }
    }

    public void startDespawnAnimation() {
        if(this.despawnAnimationTimeout <= 0) {
            this.despawnAnimationTimeout = 40;
            this.idleAnimationTimeout = 0;
            this.despawnAnimationState.start(this.tickCount);
        }
    }

    public void startSpawnAnimation() {
        if(this.spawnAnimationTimeout <= 0) {
            this.spawnAnimationTimeout = 40;
            this.idleAnimationTimeout = 0;
            this.spawnAnimationState.start(this.tickCount);
        }
    }

    public void startWalkAnimation() {
        if(this.walkAnimationTimeout <= 0) {
            this.walkAnimationTimeout = 40;
            this.idleAnimationTimeout = 0;
            this.walkAnimationState.start(this.tickCount);
        }
    }

    public void startRunAnimation() {
        if(this.runAnimationTimeout <= 0) {
            this.runAnimationTimeout = 40;
            this.idleAnimationTimeout = 0;
            this.runAnimationState.start(this.tickCount);
        }
    }

    public void startAttackAnimation() {
        if(this.attackAnimationTimeout <= 0) {
            this.attackAnimationTimeout = 40;
            this.idleAnimationTimeout = 0;
            this.attackAnimationState.start(this.tickCount);
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

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return true; // Autorise le despawn naturel
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true; // Disparaît aussi en mode Peaceful
    }

    public static boolean canSpawnAtNight(EntityType<FireSpirit> type,
                                          ServerLevelAccessor levelAccessor,
                                          MobSpawnType reason,
                                          BlockPos pos,
                                          RandomSource random) {
        // Assure-toi d'avoir bien un ServerLevel si besoin d'API serveur
        if (!(levelAccessor instanceof ServerLevel level)) {
            return false;
        }

        // ------------- conditions "nuit / lumière / hauteur" -------------
        long time = level.getDayTime() % 24000L;
        boolean isNight = time >= 13000L && time <= 23000L; // ajustable
        if (!isNight) return false;

        if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) return false;
        if (level.getBrightness(LightLayer.BLOCK, pos) > 0) return false;

        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if ((pos.getY() - groundY) > 8) return false;

        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) return false;

        // ------------- recherche de lave proche (sécurisée) -------------
        final int rx = 18, ry = 6, rz = 18;
        boolean lavaNearby = false;
        double closestDist = Double.MAX_VALUE;

        // On parcourt, mais on skip les accès qui lèvent IllegalStateException (chunk non dispo)
        // On s'arrête dès qu'on trouve de la lave.
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-rx, -ry, -rz), pos.offset(rx, ry, rz))) {
            try {
                // getBlockState peut lancer IllegalStateException si chunk non prêt -> on skip
                if (level.getBlockState(checkPos).is(Blocks.LAVA)) {
                    lavaNearby = true;
                    closestDist = Math.sqrt(pos.distSqr(checkPos));
                    break; // on s'arrête dès la première lave trouvée
                }
            } catch (IllegalStateException ex) {
                // chunk indisponible pendant worldgen : ne plantons pas, passons à la suite
                continue;
            }
        }

        if (!lavaNearby) return false;

        // debug optionnel
        // System.out.println("[FireSpiritSpawnCheck] Pos=" + pos + " => LavaNearby=true (≈" + closestDist + " blocks)");

        return true;
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
        public void stop() {
            FireSpirit.this.walkAnimationState.stop();
            FireSpirit.this.walkAnimationTimeout = 0;
        }

        @Override
        public void tick() {
            //System.out.println("fly around");
            if (FireSpirit.this.moveTarget == null ||
                    FireSpirit.this.position().distanceTo(FireSpirit.this.moveTarget) < 2.0) {
                setNewRandomDirection();
            }
            FireSpirit.this.startWalkAnimation();
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
