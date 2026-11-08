package com.tr4nce.rabbittools.items.core;

import net.minecraft.resources.ResourceLocation;
import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.api.IWandAction;
import com.tr4nce.rabbittools.wand.action.ActionDestruction;

public class ItemCoreDestruction extends ItemCore
{
    public ItemCoreDestruction(Properties properties) {
        super(properties);
    }

    @Override
    public int getColor() {
        return 0xFF0000;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionDestruction();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RabbitTools.loc("core_destruction");
    }
}
