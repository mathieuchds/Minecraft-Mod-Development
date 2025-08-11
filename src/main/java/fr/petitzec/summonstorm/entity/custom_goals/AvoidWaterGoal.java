package fr.petitzec.summonstorm.entity.custom_goals;

import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AvoidWaterGoal extends Goal {

    private static final int CHECK_RADIUS = 10;
    private static final int FLEE_DURATION = 60; // ticks (~3 sec)
    private static final int HEIGHT_VARIATION = 3;
    private final FireSpirit fireSpirit;
    private Vec3 lastShoreDirection = null;
    private int continueTowardsShoreTicks = 0;
    private static final int CONTINUE_TICKS = 20;


    private int fleeTicks = 0;

    public AvoidWaterGoal(FireSpirit fireSpirit) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.fireSpirit = fireSpirit;
    }

    @Override
    public boolean canUse() {
        // Si on trouve de l'eau à proximité (y compris juste en dessous), on fuit
        return findNearestSurfaceWater(fireSpirit.blockPosition(), CHECK_RADIUS) != null;
    }

    @Override
    public boolean canContinueToUse() {
        return fleeTicks > 0;
    }

    @Override
    public void start() {
        fleeTicks = FLEE_DURATION;
        moveAwayFromWater();
    }

    @Override
    public void tick() {
        fleeTicks--;
        if (fleeTicks % 10 == 0) { // toutes les 0.5 sec, on recalcule une direction
            moveAwayFromWater();
        }
    }

    private void moveAwayFromWater() {
        BlockPos currentPos = this.fireSpirit.blockPosition();

        // 1. Chercher eau proche dans CHECK_RADIUS
        BlockPos waterPos = findNearestSurfaceWater(currentPos, CHECK_RADIUS);

        // 2. Si pas trouvé, chercher en descendant sous le mob
        if (waterPos == null) {
            waterPos = findNearestWaterBelow(currentPos);
            if (waterPos == null) {
                // Pas d'eau proche ni sous le mob, pas de fuite nécessaire
                return;
            }
        }

        boolean isAboveWater = isAboveWaterBlock(currentPos);

        Vec3 target;

        if (isAboveWater) {
            BlockPos shorePos = findNearestShore(waterPos, 30);
            if (shorePos == null) {
                this.fireSpirit.startDespawnAnimation();
                this.fireSpirit.remove(Entity.RemovalReason.DISCARDED);
                return;
            }

            Vec3 shoreVec = new Vec3(shorePos.getX() + 0.5, shorePos.getY() + 1, shorePos.getZ() + 0.5);
            shoreVec = shoreVec.add(0, 0.8, 0); // offset vertical

            double distToShore = this.fireSpirit.position().distanceTo(shoreVec);

            if (distToShore > 3.0 || continueTowardsShoreTicks <= 0 || lastShoreDirection == null) {
                // On recalcule la direction normale vers la rive
                lastShoreDirection = shoreVec.subtract(this.fireSpirit.position()).normalize();
                continueTowardsShoreTicks = CONTINUE_TICKS;
            } else {
                // On continue dans la direction précédente
                continueTowardsShoreTicks--;
            }

            // La cible est la position actuelle + direction * distance (pousse un peu plus loin pour éviter blocage)
            target = this.fireSpirit.position().add(lastShoreDirection.scale(4));

            // Petite variation verticale pour éviter blocage
            target = target.add(0, (this.fireSpirit.getRandom().nextFloat() - 0.5) * 0.5, 0);

            System.out.println("FireSpirit target (shore chasing): " + target);
        } else {
            target = fleeFrom(waterPos);
        }


        if (this.fireSpirit.level().noCollision(this.fireSpirit.getBoundingBox().move(target.subtract(this.fireSpirit.position())))) {
            this.fireSpirit.moveTarget = target;
            this.fireSpirit.getNavigation().moveTo(target.x, target.y, target.z, 1.6); // vitesse légèrement augmentée
        }
    }

    // Cherche verticalement vers le bas le premier bloc d'eau ou autre
    private BlockPos findNearestWaterBelow(BlockPos startPos) {
        for (int y = startPos.getY(); y >= 0; y--) {
            BlockPos pos = new BlockPos(startPos.getX(), y, startPos.getZ());
            if (!this.fireSpirit.level().getBlockState(pos).isAir()) {
                if (this.fireSpirit.level().getBlockState(pos).getFluidState().isSourceOfType(Fluids.WATER)) {
                    return pos;
                } else {
                    // Bloc solide non eau -> on arrête
                    break;
                }
            }
        }
        return null;
    }


    private boolean isAboveWaterBlock(BlockPos pos) {
        // Descend depuis la hauteur actuelle pour trouver le premier bloc non-air
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        while (cursor.getY() > this.fireSpirit.level().getMinBuildHeight()
                && this.fireSpirit.level().isEmptyBlock(cursor)) {
            cursor.move(0, -1, 0);
        }
        return this.fireSpirit.level().getBlockState(cursor).getFluidState().isSourceOfType(Fluids.WATER);
    }

    private Vec3 fleeFrom(BlockPos waterPos) {
        Vec3 fromWater = this.fireSpirit.position()
                .subtract(waterPos.getX() + 0.5, this.fireSpirit.getY(), waterPos.getZ() + 0.5)
                .normalize()
                .scale(8);

        double newY = this.fireSpirit.getY() + this.fireSpirit.getRandom().nextInt(HEIGHT_VARIATION) - 1;

        return this.fireSpirit.position().add(fromWater.x, newY - this.fireSpirit.getY(), fromWater.z);
    }

    private BlockPos findNearestSurfaceWater(BlockPos origin, int radius) {
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos checkPos = origin.offset(dx, 0, dz);

                BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                        checkPos.getX(), origin.getY(), checkPos.getZ());

                while (cursor.getY() > this.fireSpirit.level().getMinBuildHeight()
                        && this.fireSpirit.level().isEmptyBlock(cursor)) {
                    cursor.move(0, -1, 0);
                }

                if (this.fireSpirit.level().getBlockState(cursor).getFluidState().isSourceOfType(Fluids.WATER)) {
                    double distSq = cursor.distSqr(origin);
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearest = cursor.immutable();
                    }
                }
            }
        }
        return nearest;
    }

    private BlockPos findNearestShore(BlockPos origin, int maxRadius) {
        int surfaceY = this.fireSpirit.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        BlockPos surfacePos = new BlockPos(origin.getX(), surfaceY - 1, origin.getZ());

        for (int radius = 1; radius <= maxRadius; radius++) {
            // Explore le carré d’anneau à distance radius autour de surfacePos
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // On est sur le bord du carré (anneau)
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;

                    int checkX = surfacePos.getX() + dx;
                    int checkZ = surfacePos.getZ() + dz;
                    int checkY = this.fireSpirit.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, checkX, checkZ) - 1;
                    BlockPos checkPos = new BlockPos(checkX, checkY, checkZ);

                    // Ce bloc n'est pas de l'eau ?
                    if (!this.fireSpirit.level().getBlockState(checkPos).getFluidState().isSourceOfType(Fluids.WATER)) {
                        // Vérifier s’il est adjacent à de l’eau → c’est une rive
                        if (isAdjacentToWater(checkPos)) {
                            return checkPos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isAdjacentToWater(BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = pos.relative(dir);
            if (this.fireSpirit.level().getBlockState(adjacent).getFluidState().isSourceOfType(Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }



}

