package com.tr4nce.rabbittools.items.core;

import net.minecraft.resources.ResourceLocation;
import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.api.IWandAction;
import com.tr4nce.rabbittools.api.IWandCore;
import com.tr4nce.rabbittools.wand.action.ActionConstruction;

public class CoreDefault implements IWandCore
{
    @Override
    public int getColor() {
        return -1;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionConstruction();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RabbitTools.loc("default");
    }
}
