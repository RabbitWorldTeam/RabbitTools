package com.tr4nce.rabbittools.items.tool;

import com.tr4nce.rabbittools.registry.ModMenuTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class PaintbrushMenu extends AbstractContainerMenu {
    public static final int LOAD_ONE = 16;
    public static final int LOAD_ALL = 17;
    private static final int SELECTED_DATA = 16;
    private static final int OFFHAND_SWAP = 40;

    private final SimpleContainer input = new SimpleContainer(1);
    private final ContainerData palette = new SimpleContainerData(17);
    private final InteractionHand hand;
    private final ItemStack brush;
    private final int heldSlot;

    public PaintbrushMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ModMenuTypes.PAINTBRUSH.get(), id);
        this.hand = hand;
        this.brush = inventory.player.getItemInHand(hand);
        this.heldSlot = hand == InteractionHand.MAIN_HAND ? inventory.selected : OFFHAND_SWAP;

        addSlot(new Slot(input, 0, 156, 30) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DyeItem;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, 9 + column + row * 9, 28 + column * 18, 133 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            boolean locked = column == heldSlot;
            addSlot(new Slot(inventory, column, 28 + column * 18, 191) {
                @Override
                public boolean mayPickup(Player player) {
                    return !locked;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !locked;
                }
            });
        }
        readPalette();
        addDataSlots(palette);
    }

    public ItemStack inputStack() {
        return input.getItem(0);
    }

    public int points(DyeColor color) {
        return palette.get(color.getId());
    }

    @Nullable
    public DyeColor selected() {
        int id = palette.get(SELECTED_DATA);
        return id < 0 ? null : DyeColor.byId(id);
    }

    public int fillableDyes() {
        ItemStack stack = inputStack();
        return stack.getItem() instanceof DyeItem dye
                ? Math.min(stack.getCount(), (PaintbrushItem.CAPACITY - points(dye.getDyeColor())) / PaintbrushItem.POINTS_PER_DYE)
                : 0;
    }

    private void readPalette() {
        for (DyeColor color : DyeColor.values()) {
            palette.set(color.getId(), PaintbrushItem.points(brush, color));
        }
        DyeColor selected = PaintbrushItem.selected(brush);
        palette.set(SELECTED_DATA, selected == null ? -1 : selected.getId());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.level().isClientSide || !stillValid(player)) {
            return false;
        }
        if (id >= 0 && id < DyeColor.values().length) {
            DyeColor color = DyeColor.byId(id);
            if (PaintbrushItem.points(brush, color) == 0) {
                return false;
            }
            PaintbrushItem.select(brush, color);
        } else if ((id == LOAD_ONE || id == LOAD_ALL) && inputStack().getItem() instanceof DyeItem dye) {
            DyeColor color = dye.getDyeColor();
            int current = PaintbrushItem.points(brush, color);
            int count = Math.min(id == LOAD_ONE ? 1 : inputStack().getCount(),
                    (PaintbrushItem.CAPACITY - current) / PaintbrushItem.POINTS_PER_DYE);
            if (count <= 0) {
                return false;
            }
            input.removeItem(0, count);
            PaintbrushItem.setPoints(brush, color, current + count * PaintbrushItem.POINTS_PER_DYE);
            if (PaintbrushItem.selected(brush) == null) {
                PaintbrushItem.select(brush, color);
            }
        } else {
            return false;
        }
        readPalette();
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive() && !player.isSpectator() && player.getItemInHand(hand) == brush
                && brush.getItem() instanceof PaintbrushItem;
    }

    @Override
    public void clicked(int index, int button, ClickType type, Player player) {
        if ((!player.level().isClientSide && !stillValid(player))
                || (type == ClickType.SWAP && button == heldSlot)
                || (heldSlot < OFFHAND_SWAP && index == 28 + heldSlot)) {
            return;
        }
        super.clicked(index, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!(stack.getItem() instanceof DyeItem) || !moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            clearContainer(player, input);
        }
    }
}
