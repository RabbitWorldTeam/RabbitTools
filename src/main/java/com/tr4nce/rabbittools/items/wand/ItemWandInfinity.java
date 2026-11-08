package com.tr4nce.rabbittools.items.wand;


public class ItemWandInfinity extends ItemWand
{
    public ItemWandInfinity(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }
}
