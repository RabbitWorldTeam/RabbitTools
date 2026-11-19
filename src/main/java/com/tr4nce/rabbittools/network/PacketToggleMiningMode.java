package com.tr4nce.rabbittools.network;

import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.component.ModDataComponents;
import com.tr4nce.rabbittools.item.HammerItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleMiningMode() implements CustomPacketPayload {
    public static final Type<PacketToggleMiningMode> TYPE = new Type<>(RabbitTools.loc("toggle_mining_mode"));
    public static final StreamCodec<FriendlyByteBuf, PacketToggleMiningMode> CODEC =
            StreamCodec.unit(new PacketToggleMiningMode());

    @Override
    public Type<PacketToggleMiningMode> type() {
        return TYPE;
    }

    public static void handle(PacketToggleMiningMode packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()
                    || !player.isShiftKeyDown()) return;

            ItemStack tool = player.getMainHandItem();
            if (!tool.is(HammerItems.NEUTRONIUM_MINING_HAMMER.get())
                    || player.pick(player.blockInteractionRange(), 1.0F, false).getType() != HitResult.Type.MISS) return;

            var mode = ModDataComponents.WIDE_MINING.get();
            boolean wide = !tool.getOrDefault(mode, false);
            if (wide) tool.set(mode, true);
            else tool.remove(mode);

            player.displayClientMessage(Component.translatable("message.rabbittools.mining_hammer.mode",
                    wide ? "5×5" : "3×3"), true);
        });
    }
}
