package fr.petitzec.summonstorm.entity.custom;

import fr.petitzec.summonstorm.entity.custom_goals.AvoidNonSneakingPlayerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class FireSpirit extends FlyingMob {

    public Vec3 moveTarget = Vec3.ZERO;
    private Vec3 lastPosition = Vec3.ZERO;
    private int stuckCounter = 0;
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    public boolean isFleeing = false;


    public FireSpirit(EntityType<? extends FireSpirit> type, Level world) {
        super(type, world);
        this.moveControl = new MyFlyingMoveControl(this);
        this.lookControl = new MyFlyingLookControl(this);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FLYING_SPEED, 0.1D);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AvoidNonSneakingPlayerGoal(this));
        this.goalSelector.addGoal(1, new FlyAroundGoal());
        // Ajoute ici d'autres goals (attaque, fuite, etc.)
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

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

    // Exemple simple de but de vol aléatoire
    class FlyAroundGoal extends Goal {
        public FlyAroundGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !FireSpirit.this.isFleeing; // NE VOLE PAS si en fuite
        }

        @Override
        public void tick() {
            if (FireSpirit.this.moveTarget == null || FireSpirit.this.position().distanceTo(FireSpirit.this.moveTarget) < 2) {
                double x = FireSpirit.this.getX() + ((FireSpirit.this.getRandom().nextDouble() - 0.5) * 16);
                double y = FireSpirit.this.getY() + (FireSpirit.this.getRandom().nextDouble() - 0.5) * 8;
                double z = FireSpirit.this.getZ() + ((FireSpirit.this.getRandom().nextDouble() - 0.5) * 16);
                Vec3 target = new Vec3(x, y, z);
                if (FireSpirit.this.level().noCollision(FireSpirit.this.getBoundingBox().move(target.subtract(FireSpirit.this.position())))) {
                    FireSpirit.this.moveTarget = target;
                }
            }
        }

        @Override
        public void start() {
            double x = FireSpirit.this.getX() + ((FireSpirit.this.getRandom().nextDouble() - 0.5) * 16);
            double y = FireSpirit.this.getY() + (FireSpirit.this.getRandom().nextDouble() - 0.5) * 8;
            double z = FireSpirit.this.getZ() + ((FireSpirit.this.getRandom().nextDouble() - 0.5) * 16);
            FireSpirit.this.moveTarget = new Vec3(x, y, z);
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

    @Override
    public void tick() {
        super.tick();
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

    private double getGroundHeight() {
        BlockPos pos = this.blockPosition();
        Level level = this.level();

        while (pos.getY() > level.getMinBuildHeight() && level.getBlockState(pos.below()).isAir()) {
            pos = pos.below();
        }

        return pos.getY();
    }

}
