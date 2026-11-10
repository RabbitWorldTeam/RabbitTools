package com.tr4nce.rabbittools.items.wand;

import com.tr4nce.rabbittools.data.ICustomItemModel;
import com.tr4nce.rabbittools.data.ItemModelGenerator;
import com.tr4nce.rabbittools.items.ModItems;
import com.tr4nce.rabbittools.world.TravelStaffSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.Nullable;

public final class TravelStaffItem extends Item implements ICustomItemModel {
    private static final int COOLDOWN_TICKS = 60;
    private static final double SEARCH_STEP = 0.5;
    private static final double MIN_SEARCH_DISTANCE = 2.0;
    private static final int MAX_LANDING_DROP = 2;
    private static final double COLLISION_EPSILON = 1.0E-6;
    private final int range;

    public TravelStaffItem(Properties properties, int range) {
        super(properties);
        this.range = range;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return use(context.getLevel(), player, context.getHand()).getResult();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || player.isSpectator()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!TravelStaffSettings.get(serverLevel.getServer()).isEnabled()) {
            player.displayClientMessage(Component.translatable("item.rabbittools.travel_staff.disabled"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!player.isAlive() || player.isPassenger() || player.isVehicle() || player.isSleeping()
                || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        Vec3 destination = findDestination(serverLevel, serverPlayer);
        if (destination == null) {
            return invalidTarget(player, stack);
        }
        EntityTeleportEvent teleport = NeoForge.EVENT_BUS.post(new EntityTeleportEvent(
                serverPlayer, destination.x, destination.y, destination.z));
        if (teleport.isCanceled()) {
            return InteractionResultHolder.fail(stack);
        }
        destination = teleport.getTarget();
        if (serverPlayer.level() != serverLevel || !isValidDestination(serverLevel, serverPlayer, destination)) {
            return invalidTarget(player, stack);
        }

        serverPlayer.teleportTo(destination.x, destination.y, destination.z);
        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.hurtMarked = true;
        serverPlayer.fallDistance = 0.0F;
        stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.getCooldowns().addCooldown(ModItems.DIAMOND_TRAVEL_STAFF.get(), COOLDOWN_TICKS);
        player.getCooldowns().addCooldown(ModItems.NETHERITE_TRAVEL_STAFF.get(), COOLDOWN_TICKS);
        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.75F, 1.0F);
        return InteractionResultHolder.consume(stack);
    }

    private static InteractionResultHolder<ItemStack> invalidTarget(Player player, ItemStack stack) {
        player.displayClientMessage(Component.translatable("item.rabbittools.travel_staff.invalid_target"), true);
        return InteractionResultHolder.fail(stack);
    }

    @Nullable
    private Vec3 findDestination(ServerLevel level, ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        CollisionContext collision = CollisionContext.of(player);
        BlockPos lastSample = null;
        // Search along the aim from far to near. A wall between the two positions is allowed.
        for (double distance = range; distance >= MIN_SEARCH_DISTANCE; distance -= SEARCH_STEP) {
            BlockPos sample = BlockPos.containing(eye.add(look.scale(distance)));
            if (sample.equals(lastSample)) {
                continue;
            }
            lastSample = sample;
            for (int drop = 0; drop <= MAX_LANDING_DROP; drop++) {
                BlockPos support = sample.below(drop + 1);
                if (!level.isInWorldBounds(support) || !level.isLoaded(support)) {
                    continue;
                }
                double top = Double.NEGATIVE_INFINITY;
                BlockState state = level.getBlockState(support);
                for (AABB box : state.getCollisionShape(level, support, collision).toAabbs()) {
                    if (box.minX <= 0.5 && box.maxX >= 0.5 && box.minZ <= 0.5 && box.maxZ >= 0.5) {
                        top = Math.max(top, box.maxY);
                    }
                }
                if (top == Double.NEGATIVE_INFINITY) {
                    continue;
                }
                Vec3 destination = new Vec3(support.getX() + 0.5, support.getY() + top, support.getZ() + 0.5);
                if (isValidDestination(level, player, destination)) {
                    return destination;
                }
            }
        }
        return null;
    }

    private boolean isValidDestination(ServerLevel level, ServerPlayer player, Vec3 destination) {
        double distanceSquared = player.position().distanceToSqr(destination);
        if (!Double.isFinite(distanceSquared) || distanceSquared <= COLLISION_EPSILON
                || distanceSquared > (double) range * range) {
            return false;
        }

        AABB landing = player.getDimensions(Pose.STANDING).makeBoundingBox(destination);
        AABB footing = landing.move(0.0, -0.01, 0.0);
        AABB occupied = landing.minmax(footing).deflate(COLLISION_EPSILON);
        BlockPos min = BlockPos.containing(occupied.minX, occupied.minY, occupied.minZ);
        BlockPos max = BlockPos.containing(occupied.maxX, occupied.maxY, occupied.maxZ);
        if (landing.minY < level.getMinBuildHeight() || landing.maxY > level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(landing)
                || !level.hasChunksAt(min, max)
                || !level.mayInteract(player, BlockPos.containing(destination))
                || !level.noCollision(player, landing)
                || !level.getBlockCollisions(player, footing).iterator().hasNext()) {
            return false;
        }

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty() || state.is(BlockTags.FIRE) || state.is(BlockTags.CAMPFIRES)
                    || state.is(Blocks.CACTUS) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.POWDER_SNOW)
                    || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE) || state.is(Blocks.POINTED_DRIPSTONE)
                    || state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void generateCustomItemModel(ItemModelGenerator generator, String name) {
        generator.withExistingParent(name, "item/handheld")
                .texture("layer0", generator.modLoc("item/" + name));
    }
}
