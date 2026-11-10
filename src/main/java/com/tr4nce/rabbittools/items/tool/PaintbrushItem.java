package com.tr4nce.rabbittools.items.tool;

import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.component.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@EventBusSubscriber(modid = RabbitTools.MODID)
public final class PaintbrushItem extends Item {
    public static final int POINTS_PER_DYE = 8;
    public static final int CAPACITY = 64;
    public static final Component TITLE = Component.translatable("gui.rabbittools.paintbrush.title");
    private static final String SELECTED = "selected";
    private static final Map<String, Integer> COSTS = Map.of(
            "wool", 8, "carpet", 8, "terracotta", 1, "concrete", 1);

    public PaintbrushItem(Properties properties) {
        super(properties.component(ModDataComponents.PAINTBRUSH_DATA.get(), new CompoundTag()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || player.isSpectator()) {
            return InteractionResultHolder.pass(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new PaintbrushMenu(id, inventory, hand), TITLE),
                    buf -> buf.writeBoolean(hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown() || !player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        var pos = context.getClickedPos();
        BlockState original = level.getBlockState(pos);
        String family = family(original);
        if (family == null) {
            return InteractionResult.PASS;
        }

        ItemStack brush = context.getItemInHand();
        DyeColor color = selected(brush);
        if (color == null) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("hud.rabbittools.paintbrush.no_color"), true);
            }
            return InteractionResult.FAIL;
        }
        var target = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(color.getName() + "_" + family));
        int cost = COSTS.get(family);
        int points = points(brush, color);
        if (original.is(target)) {
            return InteractionResult.FAIL;
        }
        if (points < cost) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("hud.rabbittools.paintbrush.shortfall", cost - points), true);
            }
            return InteractionResult.FAIL;
        }
        if (!level.mayInteract(player, pos) || !level.getWorldBorder().isWithinBounds(pos)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            if (!level.setBlockAndUpdate(pos, target.defaultBlockState())) {
                return InteractionResult.FAIL;
            }
            setPoints(brush, color, points - cost);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void paintWithPlacementProtection(UseItemOnBlockEvent event) {
        if (event.getLevel().isClientSide || event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK
                || !(event.getItemStack().getItem() instanceof PaintbrushItem) || event.getPlayer() == null
                || event.getPlayer().isShiftKeyDown() || cost(event.getLevel().getBlockState(event.getPos())) == 0) {
            return;
        }

        // Block placement can roll back the world after useOn returns. Keep the dye debit
        // on a separate stack until NeoForge has accepted the placement.
        ItemStack brush = event.getItemStack();
        ItemStack pending = brush.copy();
        UseOnContext original = event.getUseOnContext();
        var hit = new BlockHitResult(original.getClickLocation(), original.getClickedFace(),
                original.getClickedPos(), original.isInside());
        var context = new UseOnContext(event.getLevel(), event.getPlayer(), event.getHand(), pending, hit);
        InteractionResult result = CommonHooks.onPlaceItemIntoWorld(context);
        if (result.consumesAction()) {
            var type = ModDataComponents.PAINTBRUSH_DATA.get();
            brush.set(type, pending.get(type));
            event.cancelWithResult(ItemInteractionResult.CONSUME);
        } else if (result == InteractionResult.FAIL) {
            event.cancelWithResult(ItemInteractionResult.FAIL);
        }
    }

    public static int cost(BlockState state) {
        String family = family(state);
        return family == null ? 0 : COSTS.get(family);
    }

    @Nullable
    private static String family(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!id.getNamespace().equals("minecraft")) {
            return null;
        }
        String path = id.getPath();
        if (path.equals("terracotta")) {
            return path;
        }
        for (DyeColor color : DyeColor.values()) {
            String prefix = color.getName() + "_";
            if (path.startsWith(prefix)) {
                String material = path.substring(prefix.length());
                if (COSTS.containsKey(material)) {
                    return material;
                }
            }
        }
        return null;
    }

    public static int points(ItemStack brush, DyeColor color) {
        CompoundTag data = brush.get(ModDataComponents.PAINTBRUSH_DATA.get());
        return data == null ? 0 : Mth.clamp(data.getInt(color.getName()), 0, CAPACITY);
    }

    @Nullable
    public static DyeColor selected(ItemStack brush) {
        CompoundTag data = brush.get(ModDataComponents.PAINTBRUSH_DATA.get());
        if (data == null || !data.contains(SELECTED, Tag.TAG_INT)) {
            return null;
        }
        int id = data.getInt(SELECTED);
        return id >= 0 && id < DyeColor.values().length ? DyeColor.byId(id) : null;
    }

    public static void select(ItemStack brush, DyeColor color) {
        var type = ModDataComponents.PAINTBRUSH_DATA.get();
        CompoundTag data = brush.getOrDefault(type, new CompoundTag()).copy();
        data.putInt(SELECTED, color.getId());
        brush.set(type, data);
    }

    public static void setPoints(ItemStack brush, DyeColor color, int points) {
        var type = ModDataComponents.PAINTBRUSH_DATA.get();
        CompoundTag data = brush.getOrDefault(type, new CompoundTag()).copy();
        data.putInt(color.getName(), points);
        brush.set(type, data);
    }
}
