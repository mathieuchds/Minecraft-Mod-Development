package fr.petitzec.summonstorm.entity.custom_goals;

import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FireSpiritWanderNearLavaGoal extends Goal {

    private final FireSpirit fireSpirit;
    private BlockPos centerLavaPos;
    private int cooldown;

    public FireSpiritWanderNearLavaGoal(FireSpirit fireSpirit) {
        this.fireSpirit = fireSpirit;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos pos = fireSpirit.blockPosition();
        LevelReader level = fireSpirit.level();

        for (BlockPos around : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 2, 4))) {
            if (level.getBlockState(around).is(Blocks.LAVA) &&
                    level.getBlockState(around.above()).isAir()) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void start() {
        cooldown = 0;
    }

    @Override
    public boolean canContinueToUse() {
        BlockPos pos = fireSpirit.blockPosition();
        LevelReader level = fireSpirit.level();

        for (BlockPos around : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 2, 4))) {
            if (level.getBlockState(around).is(Blocks.LAVA) &&
                    level.getBlockState(around.above()).isAir()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        System.out.println("wanderingggg");
        RandomSource random = fireSpirit.getRandom();
        double radius = 4.0;

        double x = centerLavaPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2 * radius;
        double y = centerLavaPos.getY() + 1.0 + (random.nextDouble() - 0.5) * 2; // un peu de hauteur
        double z = centerLavaPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2 * radius;

        fireSpirit.getNavigation().moveTo(x, y, z, 1.0);

        cooldown = 40 + random.nextInt(40); // attend un peu avant de changer de direction
    }
}
