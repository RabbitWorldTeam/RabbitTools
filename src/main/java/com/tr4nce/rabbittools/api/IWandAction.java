package com.tr4nce.rabbittools.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import com.tr4nce.rabbittools.basics.option.WandOptions;
import com.tr4nce.rabbittools.wand.undo.ISnapshot;

import javax.annotation.Nonnull;
import java.util.List;

public interface IWandAction
{
    int getLimit(ItemStack wand);

    @Nonnull
    List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
                                 ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);

    @Nonnull
    List<ISnapshot> getSnapshotsFromAir(Level world, Player player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);


    default boolean handleShiftClick(Level world, Player player, BlockHitResult rayTraceResult,
                                     ItemStack wand, WandOptions options) {
        return false;
    }
}