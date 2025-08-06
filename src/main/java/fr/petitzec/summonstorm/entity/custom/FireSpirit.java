package fr.petitzec.summonstorm.entity.custom;

import fr.petitzec.summonstorm.entity.custom_goals.AvoidNonSneakingPlayerGoal;
import fr.petitzec.summonstorm.entity.custom_goals.FireSpiritSeekLavaGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;


import java.util.EnumSet;

public class FireSpirit extends PathfinderMob {

    public Vec3 moveTarget = Vec3.ZERO;
    private Vec3 lastPosition = Vec3.ZERO;
    private int stuckCounter = 0;
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    public boolean isFleeing = false;
    public int fireballCooldown = 0;


    public FireSpirit(EntityType<? extends FireSpirit> type, Level world) {
        super(type, world);
        this.moveControl = new MyFlyingMoveControl(this);
        this.lookControl = new MyFlyingLookControl(this);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FLYING_SPEED, 0.1D);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AvoidNonSneakingPlayerGoal(this));
        this.goalSelector.addGoal(2, new FlyAroundGoal());
        this.goalSelector.addGoal(1, new FireSpiritSeekLavaGoal(this, 0.2D));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        return false; // Ne prend jamais de dégâts de chute
    }

    public boolean pathfindDirectlyTowards(BlockPos pos) {
        this.navigation.setMaxVisitedNodesMultiplier(10.0F);
        this.navigation.moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0); // vitesse 1.0, à ajuster
        return this.navigation.getPath() != null && this.navigation.getPath().canReach();
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


    class FlyAroundGoal extends Goal {

        double groundY = FireSpirit.this.getGroundHeight();
        double maxHeight = groundY + 5;  // limite à 5 blocs au-dessus du sol
        double minHeight = groundY + 1;  // limite basse pour éviter qu'il soit collé au sol

        double y = FireSpirit.this.getY() + (FireSpirit.this.getRandom().nextDouble() - 0.5) * 8;
// Clamp y entre minHeight et maxHeight


        public FlyAroundGoal() {
            y = Mth.clamp(y, minHeight, maxHeight);
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

                // Vérifier le bloc sous cette position (y-1) pour éviter lave ou feu
                int yInt = Mth.floor(y);  // Mth.floor arrondit vers le bas
                BlockPos posBelow = new BlockPos(Mth.floor(x), yInt - 1, Mth.floor(z));

                Level level = FireSpirit.this.level();

                if (!level.isEmptyBlock(posBelow)) {
                    // Récupérer l’état du bloc sous la cible
                    var blockState = level.getBlockState(posBelow);

                    if (blockState.is(Blocks.LAVA) || blockState.is(Blocks.FIRE) || blockState.getFluidState().getType() == Fluids.LAVA) {
                        y = posBelow.getY() + 3; // remonter au-dessus
                    }
                }

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
}
