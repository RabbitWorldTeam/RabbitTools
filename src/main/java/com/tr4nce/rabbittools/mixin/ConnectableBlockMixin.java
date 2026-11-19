package com.tr4nce.rabbittools.mixin;

import com.tr4nce.rabbittools.world.ConnectionCuts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FenceBlock.class, IronBarsBlock.class, WallBlock.class})
abstract class ConnectableBlockMixin {
    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void rabbittools$keepDisconnected(BlockState state, Direction direction, BlockState neighbor,
                                              LevelAccessor level, BlockPos pos, BlockPos neighborPos,
                                              CallbackInfoReturnable<BlockState> result) {
        if (level instanceof ServerLevel server) {
            result.setReturnValue(ConnectionCuts.get(server).apply(result.getReturnValue(), pos));
        }
    }
}
