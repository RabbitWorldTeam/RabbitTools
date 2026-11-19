package com.tr4nce.rabbittools.registry;

import com.tr4nce.rabbittools.RabbitTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, RabbitTools.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BUILDING_WRENCH =
            SOUND_EVENTS.register("building_wrench", SoundEvent::createVariableRangeEvent);

    public static final DeferredHolder<SoundEvent, SoundEvent> SCREWDRIVER =
            SOUND_EVENTS.register("screwdriver", SoundEvent::createVariableRangeEvent);

    private ModSounds() {
    }
}
