package com.tr4nce.rabbittools.items.tool;

import com.tr4nce.rabbittools.registry.ModSounds;
import com.tr4nce.rabbittools.world.ConnectionCuts;
import com.tr4nce.rabbittools.world.StairShapeLocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.ArrayList;

public class ScrewdriverItem extends Item {
    private static final StairsShape[] SHAPES = {
            StairsShape.STRAIGHT, StairsShape.INNER_LEFT, StairsShape.INNER_RIGHT,
            StairsShape.OUTER_LEFT, StairsShape.OUTER_RIGHT
    };

    public ScrewdriverItem(Properties properties) {
        super(properties);
    }

    public static BlockState nextShape(BlockState state) {
        StairsShape current = state.getValue(StairBlock.SHAPE);
        for (int i = 0; i < SHAPES.length; i++) {
            if (SHAPES[i] == current) return state.setValue(StairBlock.SHAPE, SHAPES[(i + 1) % SHAPES.length]);
        }
        return state;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null || player.isSpectator() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) return InteractionResult.PASS;

        BlockState state = level.getBlockState(pos);
        if (ConnectionCuts.supports(state)) return cycleConnections(context, state);
        if (!(state.getBlock() instanceof StairBlock)) return InteractionResult.PASS;

        if (level instanceof ServerLevel server) {
            StairShapeLocks locks = StairShapeLocks.get(server);
            StairsShape previous = locks.get(pos);
            if (player.isShiftKeyDown()) {
                if (previous == null) return InteractionResult.CONSUME;
                locks.set(pos, null);
                BlockState natural = Block.updateFromNeighbourShapes(state, level, pos);
                if (natural != state && !level.setBlockAndUpdate(pos, natural)) {
                    locks.set(pos, previous);
                    return InteractionResult.CONSUME;
                }
            } else {
                BlockState target = nextShape(state);
                locks.set(pos, target.getValue(StairBlock.SHAPE));
                if (!level.setBlockAndUpdate(pos, target)) {
                    locks.set(pos, previous);
                    return InteractionResult.CONSUME;
                }
            }
            server.playSound(null, pos, ModSounds.SCREWDRIVER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult cycleConnections(UseOnContext context, BlockState state) {
        var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos();
        ConnectionCuts cuts = ConnectionCuts.get(server);
        Direction[] sides = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        var available = new ArrayList<Direction>(4);
        int current = 0;
        for (Direction side : sides) {
            BlockPos otherPos = pos.relative(side);
            if (!level.hasChunkAt(otherPos)) continue;
            boolean cut = cuts.has(pos, side) || cuts.has(otherPos, side.getOpposite());
            if (!cut && !ConnectionCuts.connected(state, side)) continue;
            if (ConnectionCuts.supports(level.getBlockState(otherPos)) && (!level.mayInteract(player, otherPos)
                    || !player.mayUseItemAt(otherPos, side.getOpposite(), context.getItemInHand()))) continue;
            if (cut) current |= 1 << available.size();
            available.add(side);
        }

        int next = player.isShiftKeyDown() ? 0 : (current + 1) % (1 << available.size());
        int changed = current ^ next;
        if (changed == 0) return InteractionResult.CONSUME;

        var neighbors = new ArrayList<BlockPos>(4);
        for (int i = 0; i < available.size(); i++) {
            int bit = 1 << i;
            if ((changed & bit) == 0) continue;
            Direction side = available.get(i);
            BlockPos otherPos = pos.relative(side);
            boolean cut = (next & bit) != 0;
            cuts.set(pos, side, cut);
            if (!cut) cuts.set(otherPos, side.getOpposite(), false);
            neighbors.add(otherPos);
        }

        refreshConnection(server, cuts, pos);
        for (BlockPos otherPos : neighbors) refreshConnection(server, cuts, otherPos);
        server.playSound(null, pos, ModSounds.SCREWDRIVER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }


    private static void refreshConnection(ServerLevel level, ConnectionCuts cuts, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!ConnectionCuts.supports(state)) return;
        BlockState updated = cuts.apply(Block.updateFromNeighbourShapes(state, level, pos), pos);
        if (updated != state) level.setBlockAndUpdate(pos, updated);
    }
}
