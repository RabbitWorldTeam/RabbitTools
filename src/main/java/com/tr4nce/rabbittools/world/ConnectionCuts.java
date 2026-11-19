package com.tr4nce.rabbittools.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public final class ConnectionCuts extends SavedData {
    private static final Factory<ConnectionCuts> FACTORY = new Factory<>(ConnectionCuts::new, ConnectionCuts::load);
    private static final Map<Direction, BooleanProperty> CROSS_SIDES = Map.of(
            Direction.NORTH, BlockStateProperties.NORTH, Direction.EAST, BlockStateProperties.EAST,
            Direction.SOUTH, BlockStateProperties.SOUTH, Direction.WEST, BlockStateProperties.WEST);
    private static final Map<Direction, EnumProperty<WallSide>> WALL_SIDES = Map.of(
            Direction.NORTH, WallBlock.NORTH_WALL, Direction.EAST, WallBlock.EAST_WALL,
            Direction.SOUTH, WallBlock.SOUTH_WALL, Direction.WEST, WallBlock.WEST_WALL);

    private final Map<Long, Integer> cuts = new HashMap<>();

    public static ConnectionCuts get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, "rabbittools_connection_cuts");
    }

    public static boolean supports(BlockState state) {
        return state.getBlock() instanceof FenceBlock || state.getBlock() instanceof WallBlock
                || state.getBlock() instanceof IronBarsBlock;
    }

    public boolean has(BlockPos pos, Direction side) {
        return (cuts.getOrDefault(pos.asLong(), 0) & bit(side)) != 0;
    }

    public boolean set(BlockPos pos, Direction side, boolean cut) {
        long key = pos.asLong();
        int before = cuts.getOrDefault(key, 0);
        int after = cut ? before | bit(side) : before & ~bit(side);
        if (before == after) return false;
        if (after == 0) cuts.remove(key);
        else cuts.put(key, after);
        setDirty();
        return true;
    }

    public void clear(BlockPos pos) {
        if (cuts.remove(pos.asLong()) != null) setDirty();
    }

    private static int bit(Direction side) {
        return 1 << side.get2DDataValue();
    }

    public static boolean connected(BlockState state, Direction side) {
        if (!side.getAxis().isHorizontal() || !supports(state)) return false;
        if (state.getBlock() instanceof WallBlock) {
            return state.getValue(WALL_SIDES.get(side)) != WallSide.NONE;
        }
        return state.getValue(CROSS_SIDES.get(side));
    }

    public BlockState apply(BlockState state, BlockPos pos) {
        if (!supports(state)) return state;
        BlockState result = state;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if ((!has(pos, side) && !has(pos.relative(side), side.getOpposite())) || !connected(result, side)) continue;
            if (result.getBlock() instanceof WallBlock) {
                result = result.setValue(WALL_SIDES.get(side), WallSide.NONE);
            } else {
                result = result.setValue(CROSS_SIDES.get(side), false);
            }
        }
        if (result != state && result.getBlock() instanceof WallBlock) {
            boolean north = connected(result, Direction.NORTH);
            boolean east = connected(result, Direction.EAST);
            boolean south = connected(result, Direction.SOUTH);
            boolean west = connected(result, Direction.WEST);
            if (north != south || east != west || north == east) result = result.setValue(WallBlock.UP, true);
        }
        return result;
    }

    private static ConnectionCuts load(CompoundTag data, HolderLookup.Provider registries) {
        ConnectionCuts locks = new ConnectionCuts();
        CompoundTag positions = data.getCompound("Cuts");
        for (String key : positions.getAllKeys()) {
            locks.cuts.put(Long.parseLong(key), positions.getInt(key) & 15);
        }
        return locks;
    }

    @Override
    public CompoundTag save(CompoundTag data, HolderLookup.Provider registries) {
        CompoundTag positions = new CompoundTag();
        cuts.forEach((pos, sides) -> positions.putInt(Long.toString(pos), sides));
        data.put("Cuts", positions);
        return data;
    }
}
