package com.tr4nce.rabbittools.items.core;

import net.minecraft.resources.ResourceLocation;
import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.api.IWandAction;
import com.tr4nce.rabbittools.wand.action.ActionAngel;

public class ItemCoreAngel extends ItemCore
{
    public ItemCoreAngel(Properties properties) {
        super(properties);
    }

    @Override
    public int getColor() {
        return 0xE9B115;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionAngel();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RabbitTools.loc("core_angel");
    }
}
