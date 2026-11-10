package com.tr4nce.rabbittools.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class TravelStaffSettings extends SavedData {
    private static final Factory<TravelStaffSettings> FACTORY =
            new Factory<>(TravelStaffSettings::new, TravelStaffSettings::load);

    private boolean enabled = true;

    public static TravelStaffSettings get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, "rabbittools_travel_staff");
    }

    private static TravelStaffSettings load(CompoundTag tag, HolderLookup.Provider registries) {
        TravelStaffSettings settings = new TravelStaffSettings();
        if (tag.contains("enabled")) {
            settings.enabled = tag.getBoolean("enabled");
        }
        return settings;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("enabled", enabled);
        return tag;
    }
}
