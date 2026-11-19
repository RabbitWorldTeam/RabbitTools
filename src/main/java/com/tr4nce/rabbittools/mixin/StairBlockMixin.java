package com.tr4nce.rabbittools.mixin;

import com.tr4nce.rabbittools.world.StairShapeLocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StairBlock.class)
abstract class StairBlockMixin {
    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void rabbittools$keepManualShape(BlockState state, Direction direction, BlockState neighbor,
                                             LevelAccessor level, BlockPos pos, BlockPos neighborPos,
                                             CallbackInfoReturnable<BlockState> result) {
        if (!(level instanceof ServerLevel server) || !direction.getAxis().isHorizontal()) return;
        BlockState updated = result.getReturnValue();
        if (updated.getBlock() != state.getBlock()) return;
        var shape = StairShapeLocks.get(server).get(pos);
        if (shape != null) result.setReturnValue(updated.setValue(StairBlock.SHAPE, shape));
    }
}
