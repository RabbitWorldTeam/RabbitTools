package com.tr4nce.rabbittools.items.tool;

import com.tr4nce.rabbittools.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.Property;

public class BuildingWrenchItem extends Item {
    public BuildingWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || player.isSpectator() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        BlockState target;
        if (player.isShiftKeyDown()) {
            target = previewFlip(state, level, pos);
            if (target == state) return InteractionResult.PASS;
        } else {
            if (!supportsFacing(state)) return InteractionResult.PASS;
            WrenchGrid grid = WrenchGrid.at(pos, context.getClickedFace(), context.getClickLocation());
            target = previewFacing(state, level, pos, grid.selectedDirection());
            if (target == state) return InteractionResult.CONSUME;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (apply(level, pos, target)) {
            level.playSound(null, pos, ModSounds.BUILDING_WRENCH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.CONSUME;
    }

    public static boolean supportsFacing(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BedBlock
                || state.is(Blocks.PISTON_HEAD) || state.is(Blocks.MOVING_PISTON)
                || state.getOptionalValue(BlockStateProperties.EXTENDED).orElse(false)
                || state.getOptionalValue(BlockStateProperties.CHEST_TYPE).orElse(ChestType.SINGLE) != ChestType.SINGLE
                || (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && !(block instanceof DoorBlock))) {
            return false;
        }
        return facingProperty(state) != null
                || findProperty(state, "axis", Direction.Axis.class) != null
                || state.hasProperty(BlockStateProperties.ORIENTATION)
                || state.hasProperty(BlockStateProperties.ROTATION_16);
    }

    public static BlockState previewFacing(BlockState state, Level level, BlockPos pos, Direction direction) {
        return validateChange(state, face(state, direction), level, pos);
    }

    public static BlockState previewFlip(BlockState state, Level level, BlockPos pos) {
        Block block = state.getBlock();
        BlockState target;
        if (block instanceof StairBlock || block instanceof TrapDoorBlock) {
            Half half = state.getValue(BlockStateProperties.HALF);
            target = state.setValue(BlockStateProperties.HALF, half == Half.TOP ? Half.BOTTOM : Half.TOP);
        } else if (block instanceof SlabBlock) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            if (type == SlabType.DOUBLE) return state;
            target = state.setValue(SlabBlock.TYPE, type == SlabType.TOP ? SlabType.BOTTOM : SlabType.TOP);
        } else if (block instanceof DoorBlock) {
            DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);
            target = state.setValue(DoorBlock.HINGE, hinge == DoorHingeSide.LEFT ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
        } else {
            return state;
        }

        return validateChange(state, target, level, pos);
    }

    private static BlockState validateChange(BlockState state, BlockState target, Level level, BlockPos pos) {
        if (target == state || target.getBlock() != state.getBlock() || !target.canSurvive(level, pos)) return state;
        if (target.getBlock() instanceof DoorBlock && otherDoorHalf(state, level, pos) == null) return state;
        if (target.getBlock() instanceof StairBlock) {
            target = Block.updateFromNeighbourShapes(target, level, pos);
            if (target.getBlock() != state.getBlock()) return state;
        }
        return target;
    }

    private static boolean apply(Level level, BlockPos pos, BlockState target) {
        if (!(target.getBlock() instanceof DoorBlock)) return level.setBlockAndUpdate(pos, target);

        BlockPos otherPos = otherDoorHalf(target, level, pos);
        if (otherPos == null) return false;
        BlockState other = level.getBlockState(otherPos)
                .setValue(DoorBlock.FACING, target.getValue(DoorBlock.FACING))
                .setValue(DoorBlock.HINGE, target.getValue(DoorBlock.HINGE));

        // Update both halves before door neighbor logic copies properties across them.
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        level.setBlock(pos, target, flags);
        level.setBlock(otherPos, other, flags);
        level.updateNeighborsAt(pos, target.getBlock());
        level.updateNeighborsAt(otherPos, other.getBlock());
        return true;
    }

    private static BlockPos otherDoorHalf(BlockState state, Level level, BlockPos pos) {
        DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState other = level.getBlockState(otherPos);
        return other.is(state.getBlock()) && other.getValue(DoorBlock.HALF) != half ? otherPos : null;
    }

    private static Property<Direction> facingProperty(BlockState state) {
        Property<Direction> property = findProperty(state, "facing", Direction.class);
        if (property == null) property = findProperty(state, "horizontal_facing", Direction.class);
        if (property == null) property = findProperty(state, "direction", Direction.class);
        return property;
    }

    private static BlockState face(BlockState state, Direction direction) {
        Property<Direction> facing = facingProperty(state);
        if (facing != null) return setAllowed(state, facing, direction);

        Property<Direction.Axis> axis = findProperty(state, "axis", Direction.Axis.class);
        if (axis != null) return setAllowed(state, axis, direction.getAxis());

        if (state.hasProperty(BlockStateProperties.ORIENTATION)) {
            FrontAndTop current = state.getValue(BlockStateProperties.ORIENTATION);
            if (current.front() == direction) return state;
            for (FrontAndTop orientation : BlockStateProperties.ORIENTATION.getPossibleValues()) {
                if (orientation.front() == direction && orientation.top() == current.top()) {
                    return state.setValue(BlockStateProperties.ORIENTATION, orientation);
                }
            }
            for (FrontAndTop orientation : BlockStateProperties.ORIENTATION.getPossibleValues()) {
                if (orientation.front() == direction) {
                    return state.setValue(BlockStateProperties.ORIENTATION, orientation);
                }
            }
        }

        if (state.hasProperty(BlockStateProperties.ROTATION_16) && direction.getAxis().isHorizontal()) {
            return state.setValue(BlockStateProperties.ROTATION_16, direction.get2DDataValue() * 4);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState setAllowed(BlockState state, Property<T> property, T value) {
        return property.getPossibleValues().contains(value) ? state.setValue(property, value) : state;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> Property<T> findProperty(BlockState state, String name, Class<T> type) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
        return property != null && property.getValueClass() == type ? (Property<T>) property : null;
    }
}
