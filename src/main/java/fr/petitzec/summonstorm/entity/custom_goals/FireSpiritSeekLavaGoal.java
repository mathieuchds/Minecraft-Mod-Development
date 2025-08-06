package fr.petitzec.summonstorm.entity.custom_goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class FireSpiritSeekLavaGoal extends MoveToBlockGoal {

    private final Mob mob;

    public FireSpiritSeekLavaGoal(Mob mob, double speed) {
        // mob, speedModifier, searchRange (25), verticalSearchRange (4)
        super(mob, speed, 25, 4);
        this.mob = mob;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        // Le bloc doit être de la lave et le bloc au-dessus doit être "traversable"
        return level.getBlockState(pos).is(Blocks.LAVA)
                && level.getBlockState(pos.above()).isPathfindable(level, pos.above(), PathComputationType.LAND);
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
