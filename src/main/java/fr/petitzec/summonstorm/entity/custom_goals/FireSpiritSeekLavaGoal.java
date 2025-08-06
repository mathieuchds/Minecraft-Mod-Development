package fr.petitzec.summonstorm.entity.custom_goals;

import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class FireSpiritSeekLavaGoal extends MoveToBlockGoal {

    private final PathfinderMob mob;

    public FireSpiritSeekLavaGoal(PathfinderMob mob, double speed) {
        // mob, speedModifier, searchRange (25), verticalSearchRange (4)
        super(mob, speed, 25, 4);
        this.mob = mob;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        // Le bloc doit être de la lave et le bloc au-dessus doit être "traversable"
        return level.getBlockState(pos).is(Blocks.LAVA)
                && level.getBlockState(pos.above()).isPathfindable(PathComputationType.LAND);
    }

    @Override
    public void tick() {
        if (targetLava != null) {
            boolean pathStarted = FireSpirit.pathfindDirectlyTowards(targetLava);
            if (!pathStarted) {
                // fallback : movement direct par setDeltaMovement si pas de path possible
                Vec3 direction = Vec3.atCenterOf(targetLava).subtract(fireSpirit.position()).normalize().scale(0.3);
                fireSpirit.setDeltaMovement(direction);
            }
        }
    }




    @Override
    public boolean canUse() {
        // Active seulement si pas déjà dans la lave
        return !mob.isInLava() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        // Continue tant que pas dans la lave
        return !mob.isInLava() && super.canContinueToUse();
    }

    @Override
    public boolean shouldRecalculatePath() {
        // Recalcule le chemin toutes les 20 ticks
        return this.tryTicks % 20 == 0;
    }

    @Override
    public BlockPos getMoveToTarget() {
        return this.blockPos;
    }
}
