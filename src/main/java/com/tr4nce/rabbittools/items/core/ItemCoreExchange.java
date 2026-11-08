package com.tr4nce.rabbittools.items.core;

import net.minecraft.resources.ResourceLocation;
import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.api.IWandAction;
import com.tr4nce.rabbittools.wand.action.ActionExchange;

public class ItemCoreExchange extends ItemCore
{
    public ItemCoreExchange(Properties properties) {
        super(properties);
    }

    @Override
    public int getColor() {
        return 0x2ECC71;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionExchange();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RabbitTools.loc("core_exchange");
    }
}