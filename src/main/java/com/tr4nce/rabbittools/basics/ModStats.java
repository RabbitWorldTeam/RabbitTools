package com.tr4nce.rabbittools.basics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.tr4nce.rabbittools.RabbitTools;

import java.util.function.Supplier;

public class ModStats
{
    public static final DeferredRegister<ResourceLocation> CUSTOM_STATS = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT, RabbitTools.MODID);

    public static final ResourceLocation USE_WAND = ResourceLocation.fromNamespaceAndPath(RabbitTools.MODID, "use_wand");
    public static final Supplier<ResourceLocation> USE_WAND_STAT = CUSTOM_STATS.register("use_wand", () -> USE_WAND);
}
