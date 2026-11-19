package com.tr4nce.rabbittools.mixin;

import com.tr4nce.rabbittools.world.ConnectionCuts;
import com.tr4nce.rabbittools.world.StairShapeLocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
abstract class LevelChunkMixin {
    @Shadow public abstract net.minecraft.world.level.Level getLevel();

    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void rabbittools$clearRemovedStair(BlockPos pos, BlockState state, boolean moving,
                                               CallbackInfoReturnable<BlockState> result) {
        if (!(getLevel() instanceof ServerLevel server)) return;
        BlockState old = result.getReturnValue();
        if (old == null || old.getBlock() == state.getBlock()) return;
        if (old.getBlock() instanceof StairBlock) StairShapeLocks.get(server).set(pos, null);
        if (ConnectionCuts.supports(old)) ConnectionCuts.get(server).clear(pos);
    }
}
