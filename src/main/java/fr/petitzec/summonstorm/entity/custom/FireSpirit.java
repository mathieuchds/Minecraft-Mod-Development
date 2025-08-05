package fr.petitzec.summonstorm.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FireSpirit extends PathfinderMob {

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public FireSpirit(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noPhysics = true; // allows flying through air
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidNonSneakingPlayerGoal(this, 8.0D, 1.2D, 1.5D));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Animation idle
        this.setupAnimationStates();

        if (!this.level().isClientSide) {
            // Génère des particules de feu
            if (this.random.nextInt(5) == 0) {
                ((ServerLevel) this.level()).sendParticles(
                        ParticleTypes.FLAME,
                        this.getX(), this.getY(), this.getZ(),
                        1,
                        (random.nextDouble() - 0.5) * 0.1,
                        (random.nextDouble() - 0.5) * 0.1,
                        (random.nextDouble() - 0.5) * 0.1,
                        0.01
                );
            }

            // Mouvement flottant réaliste
            Vec3 currentVelocity = this.getDeltaMovement();
            double hoverSpeed = 0.02;
            double yaw = this.getYRot() * ((float)Math.PI / 180F);

            // Mouvement aléatoire
            double dx = (random.nextDouble() - 0.5) * hoverSpeed;
            double dy = (random.nextDouble() - 0.3) * hoverSpeed * 2;
            double dz = (random.nextDouble() - 0.5) * hoverSpeed;

            Vec3 newVelocity = new Vec3(dx, dy, dz);
            this.setDeltaMovement(currentVelocity.add(newVelocity));

            // Applique le mouvement
            this.move(MoverType.SELF, this.getDeltaMovement().scale(0.5));

            // Rotation fluide
            this.setYRot(this.getYRot() + (float)(random.nextDouble() - 0.5) * 4.0F);
            this.setYHeadRot(this.getYRot());
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }



    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.FLYING_SPEED, 0.3D);
    }

    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean isImmobile() {
        return false;
    }

    public static boolean checkSpawnRules(EntityType<FireSpirit> type, LevelReader level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (!(level instanceof Level lvl)) return false;

        // Vérifie si c'est la nuit (jour Minecraft = 24000 ticks)
        long time = lvl.getDayTime() % 24000L;
        boolean isNight = time >= 13000L && time <= 23000L;

        return isNight && level.getBlockState(pos.below()).isSolid();

    }

    static class AvoidNonSneakingPlayerGoal extends Goal {
        private final FireSpirit spirit;
        private Player targetPlayer;
        private final double farSpeed;
        private final double nearSpeed;
        private final double distance;

        public AvoidNonSneakingPlayerGoal(FireSpirit spirit, double distance, double farSpeed, double nearSpeed) {
            this.spirit = spirit;
            this.farSpeed = farSpeed;
            this.nearSpeed = nearSpeed;
            this.distance = distance;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            this.targetPlayer = this.spirit.level().getNearestPlayer(this.spirit, distance);
            return this.targetPlayer != null && !targetPlayer.isCrouching();
        }

        @Override
        public void start() {
            Vec3 dir = this.spirit.position().subtract(this.targetPlayer.position()).normalize().scale(8);
            Vec3 target = this.spirit.position().add(dir);
            this.spirit.getNavigation().moveTo(target.x, target.y, target.z, this.farSpeed);
        }

        @Override
        public boolean canContinueToUse() {
            return targetPlayer != null && spirit.distanceTo(targetPlayer) < distance && !targetPlayer.isCrouching();
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        ResourceLocation id = source.typeHolder().unwrapKey().orElseThrow().location();
        return id.getPath().contains("fire") || id.getPath().contains("lava") || id.getPath().contains("hot_floor");
    }



    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Silent movement
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    private void setupAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 40;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }
}

