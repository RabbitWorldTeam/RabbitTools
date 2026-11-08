package com.tr4nce.rabbittools.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.tr4nce.rabbittools.containers.ContainerTrace;

public interface IContainerHandler
{
    boolean matches(Player player, ItemStack itemStack, ItemStack inventoryStack);

    default int getSignature(Player player, ItemStack inventoryStack) {
        return System.identityHashCode(inventoryStack);
    }

    int countItems(Player player, ContainerTrace trace, ItemStack itemStack, ItemStack inventoryStack);

    int useItems(Player player, ContainerTrace trace, ItemStack itemStack, ItemStack inventoryStack, int count);
}