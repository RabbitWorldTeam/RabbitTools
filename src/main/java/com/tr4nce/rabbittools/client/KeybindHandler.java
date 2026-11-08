package com.tr4nce.rabbittools.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.items.containeritems.ItemVoidSack;
import com.tr4nce.rabbittools.network.PacketExchangeSelect;
import com.tr4nce.rabbittools.network.PacketToggleVoidSackActive;

public class KeybindHandler {

    public static final KeyMapping KEY_VOID_SACK_TOGGLE = new KeyMapping(
            getKey("void_sack_toggle"),
            GLFW.GLFW_KEY_M,
            getCategory("rabbittools")
    );

    public static final KeyMapping KEY_WAND_UNDO = new KeyMapping(
            getKey("wand_undo"),
            GLFW.GLFW_KEY_K,
            getCategory("rabbittools")
    );

    /**
     * Selecciona el bloque apuntado como reemplazo del Exchange core.
     * Numpad 7 por defecto (evita el conflicto de Shift+Click con el GUI de la vara).
     */
    public static final KeyMapping KEY_EXCHANGE_SELECT = new KeyMapping(
            getKey("exchange_select"),
            GLFW.GLFW_KEY_KP_7,
            getCategory("rabbittools")
    );

    private static String getKey(String name) {
        return String.join(".", "key", RabbitTools.MODID, name);
    }

    private static String getCategory(String modid) {
        return "key.categories." + modid;
    }

    @SubscribeEvent
    public void KeyEvent(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // Toggle Void Sack con tecla M
        if (KEY_VOID_SACK_TOGGLE.consumeClick()) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof ItemVoidSack) {
                    PacketToggleVoidSackActive.send(hand);
                    break;
                }
            }
        }

        // Undo de varita con tecla K
        if (KEY_WAND_UNDO.consumeClick()) {
            if (com.tr4nce.rabbittools.basics.WandUtil.holdingWand(player) != null) {
                com.tr4nce.rabbittools.network.ModMessages.sendToServer(
                        new com.tr4nce.rabbittools.network.PacketWandUndo());
            }
        }

        // Seleccionar bloque de reemplazo del Exchange core con Numpad 7
        if (KEY_EXCHANGE_SELECT.consumeClick()) {
            if (com.tr4nce.rabbittools.basics.WandUtil.holdingWand(player) != null) {
                com.tr4nce.rabbittools.network.ModMessages.sendToServer(
                        new PacketExchangeSelect());
            }
        }
    }
}