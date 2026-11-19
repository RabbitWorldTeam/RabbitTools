package com.tr4nce.rabbittools.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class StairShapeLocks extends SavedData {
    private static final String NAME = "rabbittools_stair_shapes";
    private static final Factory<StairShapeLocks> FACTORY = new Factory<>(StairShapeLocks::new, StairShapeLocks::load);
    private final Map<Long, StairsShape> shapes = new HashMap<>();

    public static StairShapeLocks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    @Nullable
    public StairsShape get(BlockPos pos) {
        return shapes.get(pos.asLong());
    }

    public void set(BlockPos pos, @Nullable StairsShape shape) {
        long key = pos.asLong();
        StairsShape before = shape == null ? shapes.remove(key) : shapes.put(key, shape);
        if (before != shape) setDirty();
    }

    private static StairShapeLocks load(CompoundTag data, HolderLookup.Provider registries) {
        StairShapeLocks locks = new StairShapeLocks();
        for (Tag value : data.getList("Stairs", Tag.TAG_COMPOUND)) {
            if (value instanceof CompoundTag entry && entry.contains("Pos", Tag.TAG_LONG)) {
                for (StairsShape shape : StairsShape.values()) {
                    if (shape.getSerializedName().equals(entry.getString("Shape"))) {
                        locks.shapes.put(entry.getLong("Pos"), shape);
                        break;
                    }
                }
            }
        }
        return locks;
    }

    @Override
    public CompoundTag save(CompoundTag data, HolderLookup.Provider registries) {
        var stairs = new net.minecraft.nbt.ListTag();
        shapes.forEach((pos, shape) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", pos);
            entry.putString("Shape", shape.getSerializedName());
            stairs.add(entry);
        });
        data.put("Stairs", stairs);
        return data;
    }
}
