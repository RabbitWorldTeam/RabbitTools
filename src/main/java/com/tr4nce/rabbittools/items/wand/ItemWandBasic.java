package com.tr4nce.rabbittools.items.wand;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import com.tr4nce.rabbittools.basics.ConfigServer;
import com.tr4nce.rabbittools.basics.option.WandOptions;
import com.tr4nce.rabbittools.items.ModItems;
import com.tr4nce.rabbittools.items.core.ItemCoreExchange;

import javax.annotation.Nonnull;

public class ItemWandBasic extends ItemWand
{
    private final Tier tier;

    public ItemWandBasic(Properties properties, Tier tier) {
        super(properties.durability(tier.getUses()));
        this.tier = tier;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        // Special case: Gold Construction Wand + Exchange Core must have exactly
        // half the durability of the Diamond Construction Wand + Exchange Core.
        // Diamond's own durability isn't affected by which core is installed, so this
        // simply reads Diamond's configured durability and halves it, staying in sync
        // if the server admin changes Diamond's config value.
        if(this.tier == Tiers.GOLD && new WandOptions(stack).cores.get() instanceof ItemCoreExchange) {
            int diamondDurability = ConfigServer.getWandProperties(ModItems.WAND_DIAMOND.get()).getDurability();
            return diamondDurability / 2;
        }
        return ConfigServer.getWandProperties(this).getDurability();
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    @Override
    public boolean isValidRepairItem(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
        return this.tier.getRepairIngredient().test(repair);
    }
}