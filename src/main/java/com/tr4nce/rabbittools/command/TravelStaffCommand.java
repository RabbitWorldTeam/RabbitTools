package com.tr4nce.rabbittools.command;

import com.tr4nce.rabbittools.world.TravelStaffSettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class TravelStaffCommand {
    private TravelStaffCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("rabbittools")
                .then(Commands.literal("travelstaff")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("status").executes(context -> status(context.getSource(), false)))
                        .then(Commands.literal("enable").executes(context -> setEnabled(context.getSource(), true)))
                        .then(Commands.literal("disable").executes(context -> setEnabled(context.getSource(), false)))));
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        TravelStaffSettings.get(source.getServer()).setEnabled(enabled);
        status(source, true);
        return 1;
    }

    private static int status(CommandSourceStack source, boolean broadcast) {
        boolean enabled = TravelStaffSettings.get(source.getServer()).isEnabled();
        source.sendSuccess(() -> Component.translatable(enabled
                ? "commands.rabbittools.travelstaff.enabled"
                : "commands.rabbittools.travelstaff.disabled"), broadcast);
        return enabled ? 1 : 0;
    }
}
