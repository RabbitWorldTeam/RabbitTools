package com.tr4nce.rabbittools.client.renderer;

import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.items.tool.MeasuringTapeItem;
import com.tr4nce.rabbittools.items.tool.PaintbrushItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = RabbitTools.MODID, value = Dist.CLIENT)
public final class BuilderToolOverlay {
    private BuilderToolOverlay() {
    }

    @SubscribeEvent
    public static void drawHud(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.screen != null || client.options.hideGui || client.player.isSpectator()) {
            return;
        }
        ItemStack stack = heldTool(client);
        if (stack.isEmpty()) {
            return;
        }

        BlockPos hit = target(client);
        Component label;
        Component centerLabel = null;
        if (stack.getItem() instanceof PaintbrushItem) {
            int cost = hit == null ? 0 : PaintbrushItem.cost(client.level.getBlockState(hit));
            if (cost == 0) {
                return;
            }
            var color = PaintbrushItem.selected(stack);
            label = color == null
                    ? Component.translatable("hud.rabbittools.paintbrush.no_color")
                    : Component.translatable("hud.rabbittools.paintbrush.cost", DyeItem.byColor(color).getDescription(),
                            PaintbrushItem.points(stack, color), cost);
        } else {
            BlockPos start = MeasuringTapeItem.start(stack, client.level);
            BlockPos end = MeasuringTapeItem.end(stack, client.level);
            if (start == null) {
                label = Component.translatable("hud.rabbittools.measurement.start");
            } else if (end != null || hit != null) {
                BlockPos other = end == null ? hit : end;
                label = MeasuringTapeItem.dimensions(start, other);
                var center = MeasuringTapeItem.center(start, other);
                centerLabel = Component.translatable("hud.rabbittools.measurement.center",
                        coordinate(center.x), coordinate(center.y), coordinate(center.z));
            } else {
                label = Component.translatable("hud.rabbittools.measurement.end");
            }
        }
        var graphics = event.getGuiGraphics();
        int x = graphics.guiWidth() / 2;
        int lineStep = client.font.lineHeight + 2;
        int y = graphics.guiHeight() / 2 + 14;
        if (stack.getItem() instanceof MeasuringTapeItem) {
            // Vanilla starts the selected-item name at 59 px from the bottom and
            // moves it up with taller status bars. Leave room for the action bar above it.
            int hotbarHud = Math.max(59, Math.max(client.gui.leftHeight, client.gui.rightHeight));
            int bottomLine = graphics.guiHeight() - hotbarHud - 13 - lineStep;
            y = centerLabel == null ? bottomLine : bottomLine - lineStep;
        }
        graphics.drawCenteredString(client.font, label, x, y, 0xFFF2E8D6);
        if (centerLabel != null) {
            graphics.drawCenteredString(client.font, centerLabel, x, y + lineStep, 0xFFF0BC64);
        }
    }

    private static String coordinate(double value) {
        int whole = (int) value;
        return value == whole ? Integer.toString(whole) : Double.toString(value);
    }

    @SubscribeEvent
    public static void drawMeasurement(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.screen != null || client.options.hideGui
                || client.player.isSpectator()) {
            return;
        }
        ItemStack stack = heldTool(client);
        if (!(stack.getItem() instanceof MeasuringTapeItem)) {
            return;
        }
        BlockPos start = MeasuringTapeItem.start(stack, client.level);
        if (start == null) {
            return;
        }
        BlockPos end = MeasuringTapeItem.end(stack, client.level);
        if (end == null) {
            end = target(client);
        }
        if (end == null) {
            drawMarker(event, new AABB(start), 0.54F, 0.84F, 0.67F);
        } else {
            BlockPos lower = MeasuringTapeItem.centerBlock(start, end, false);
            BlockPos upper = MeasuringTapeItem.centerBlock(start, end, true);
            // Each even-length axis contributes two blocks, for at most eight in total.
            AABB middle = new AABB(lower).minmax(new AABB(upper));
            if (!middle.contains(start.getCenter())) {
                drawMarker(event, new AABB(start), 0.54F, 0.84F, 0.67F);
            }
            if (!middle.contains(end.getCenter())) {
                drawMarker(event, new AABB(end), 0.47F, 0.76F, 0.92F);
            }
            for (BlockPos pos : BlockPos.betweenClosed(lower, upper)) {
                drawMarker(event, new AABB(pos), 0.94F, 0.74F, 0.39F);
            }
            var center = MeasuringTapeItem.center(start, end);
            drawMarker(event, new AABB(center, center).inflate(0.1), 1.0F, 0.84F, 0.53F);
        }
        client.renderBuffers().bufferSource().endBatch(RenderType.lines());
    }

    private static void drawMarker(RenderLevelStageEvent event, AABB bounds, float red, float green, float blue) {
        var camera = event.getCamera().getPosition();
        var box = bounds.inflate(0.006).move(-camera.x, -camera.y, -camera.z);
        LevelRenderer.renderLineBox(event.getPoseStack(),
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines()),
                box, red, green, blue, 0.9F);
    }

    private static ItemStack heldTool(Minecraft client) {
        ItemStack main = client.player.getMainHandItem();
        if (main.getItem() instanceof MeasuringTapeItem || main.getItem() instanceof PaintbrushItem) {
            return main;
        }
        ItemStack offhand = client.player.getOffhandItem();
        return offhand.getItem() instanceof MeasuringTapeItem || offhand.getItem() instanceof PaintbrushItem
                ? offhand : ItemStack.EMPTY;
    }

    private static BlockPos target(Minecraft client) {
        return client.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                ? hit.getBlockPos() : null;
    }
}
