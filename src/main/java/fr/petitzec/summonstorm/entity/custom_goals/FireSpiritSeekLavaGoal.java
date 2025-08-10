package fr.petitzec.summonstorm.entity.custom_goals;

import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FireSpiritSeekLavaGoal extends Goal {

    private final FireSpirit fireSpirit;
    private BlockPos targetLava;
    private int ticksStuck = 0;
    private Path lastPath;

    public FireSpiritSeekLavaGoal(FireSpirit fireSpirit) {
        this.fireSpirit = fireSpirit;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (fireSpirit.isInLava()) return false;

        BlockPos mobPos = fireSpirit.blockPosition();
        double closestDistance = Double.MAX_VALUE;
        BlockPos closestLavaPos = null;

        for (BlockPos pos : BlockPos.betweenClosed(mobPos.offset(-18, -6, -18), mobPos.offset(18, 6, 18))) {
            if (fireSpirit.level().getBlockState(pos).is(Blocks.LAVA)) {
                double distance = fireSpirit.distanceToSqr(Vec3.atCenterOf(pos.above(2)));
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestLavaPos = pos.immutable().above(2);
                }
            }
        }

        if (closestLavaPos != null && closestDistance > 6) {
            targetLava = closestLavaPos;
            //System.out.println("canUse: targetLava = " + targetLava + ", distance = " + closestDistance);
            return true;
        }

        return false;
    }


    @Override
    public void start() {
        ticksStuck = 0;
        lastPath = null;
    }

    @Override
    public void stop() {
        targetLava = getRandomValidPositionOrStay();
        fireSpirit.moveTarget = Vec3.atCenterOf(targetLava);
    }

    private BlockPos getRandomValidPositionOrStay() {
        RandomSource random = fireSpirit.getRandom();
        BlockPos current = fireSpirit.blockPosition();

        for (int i = 0; i < 10; i++) { // on tente 10 positions aléatoires
            double x = current.getX() + (random.nextInt(16) - 8);
            double y = current.getY() + (random.nextInt(8) - 4);
            double z = current.getZ() + (random.nextInt(16) - 8);

            BlockPos candidate = new BlockPos((int) x, (int) y, (int) z);

            // Vérifie si la position est valide (pas dans un mur, collision possible)
            if (fireSpirit.level().noCollision(fireSpirit.getBoundingBox().move(Vec3.atCenterOf(candidate).subtract(fireSpirit.position())))) {
                return candidate.immutable(); // on a trouvé un point valide
            }
        }

        // Si aucune position valide, on reste sur place
        return current;
    }


    @Override
    public boolean canContinueToUse() {
        //System.out.println("canContinue :" + targetLava + " " + fireSpirit.distanceToSqr(Vec3.atCenterOf(targetLava)));
        return targetLava != null && fireSpirit.distanceToSqr(Vec3.atCenterOf(targetLava)) > 6;
    }


    @Override
    public void tick() {
        System.out.println("seek lava");

        if (targetLava == null) return;

        if (!fireSpirit.getNavigation().isInProgress()) {
            //System.out.println("find target");
            fireSpirit.getNavigation().setMaxVisitedNodesMultiplier(10.0F);

            //System.out.println("try path to: " + targetLava);
            Path path = fireSpirit.getNavigation().createPath(targetLava, 0);
            if (path == null) {
                System.out.println("NO PATH FOUND");
            } else {
                System.out.println("Path exists");
            }

            fireSpirit.getNavigation().moveTo(targetLava.getX() + 0.5, targetLava.getY() + 1, targetLava.getZ() + 0.5, 1.0);
        }
        //System.out.println("cond : "+ lastPath +"  " + fireSpirit.getNavigation().getPath() +"  " + fireSpirit.getNavigation().getPath().sameAs(lastPath));
        if(lastPath != null)
            //System.out.println(fireSpirit.getNavigation().getPath().sameAs(lastPath));

        if (lastPath != null && fireSpirit.getNavigation().getPath() != null &&
                fireSpirit.getNavigation().getPath().sameAs(lastPath)) {
            ticksStuck++;
            System.out.println("stuck" + fireSpirit.onGround());
            if (ticksStuck > 60) {
                System.out.println("abandon");
                stop(); // abandon si bloqué
            }
        } else {
            //System.out.println("tick");
            lastPath = fireSpirit.getNavigation().getPath();
            ticksStuck = 0;
        }
    }
}

