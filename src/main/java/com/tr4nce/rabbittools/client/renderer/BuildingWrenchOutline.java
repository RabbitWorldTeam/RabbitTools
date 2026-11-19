package com.tr4nce.rabbittools.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.items.ModItems;
import com.tr4nce.rabbittools.items.tool.BuildingWrenchItem;
import com.tr4nce.rabbittools.items.tool.ScrewdriverItem;
import com.tr4nce.rabbittools.items.tool.WrenchGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = RabbitTools.MODID, value = Dist.CLIENT)
public final class BuildingWrenchOutline {
    private BuildingWrenchOutline() {
    }

    @SubscribeEvent
    public static void renderGrid(RenderHighlightEvent.Block event) {
        var client = Minecraft.getInstance();
        var player = client.player;
        var level = client.level;
        if (player == null || level == null || player.isSpectator() || event.getCamera().getEntity() != player) return;

        ItemStack wrench = player.getMainHandItem().is(ModItems.BUILDING_WRENCH.get())
                ? player.getMainHandItem() : player.getOffhandItem();
        boolean holdingWrench = wrench.is(ModItems.BUILDING_WRENCH.get());
        boolean holdingScrewdriver = !holdingWrench && (player.getMainHandItem().is(ModItems.SCREWDRIVER.get())
                || player.getOffhandItem().is(ModItems.SCREWDRIVER.get()));
        if (!holdingWrench && !holdingScrewdriver) return;

        var hit = event.getTarget();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        boolean flipping = holdingWrench && player.isShiftKeyDown();
        BlockState preview = state;
        if (holdingScrewdriver) {
            if (player.isShiftKeyDown() || !(state.getBlock() instanceof StairBlock)) return;
            preview = ScrewdriverItem.nextShape(state);
        } else if (flipping) {
            preview = BuildingWrenchItem.previewFlip(state, level, pos);
            if (preview == state) return;
        } else if (!BuildingWrenchItem.supportsFacing(state)) {
            return;
        }

        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        PoseStack poses = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poses.pushPose();
        poses.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        PoseStack.Pose pose = poses.last();

        if (flipping || holdingScrewdriver) {
            preview.getShape(level, pos, CollisionContext.of(player)).forAllEdges((x1, y1, z1, x2, y2, z2) ->
                    line(lines, pose, new Vec3(x1, y1, z1), new Vec3(x2, y2, z2), 0xFFFFD45C));
        } else {
            WrenchGrid grid = WrenchGrid.at(pos, hit.getDirection(), hit.getLocation());
            boolean canChange = BuildingWrenchItem.previewFacing(state, level, pos, grid.selectedDirection()) != state;
            drawGrid(lines, pose, grid, canChange);
        }
        poses.popPose();
    }

    private static void drawGrid(VertexConsumer lines, PoseStack.Pose pose, WrenchGrid grid, boolean canChange) {
        for (int division = 0; division <= 3; division++) {
            line(lines, pose, grid.point(division, 0), grid.point(division, 3), 0xDDEFEFEF);
            line(lines, pose, grid.point(0, division), grid.point(3, division), 0xDDEFEFEF);
        }
        int col = grid.column();
        int row = grid.row();
        int color = canChange ? 0xFFFFD45C : 0xFF909090;
        Vec3 a = grid.point(col, row);
        Vec3 b = grid.point(col + 1, row);
        Vec3 c = grid.point(col + 1, row + 1);
        Vec3 d = grid.point(col, row + 1);
        line(lines, pose, a, b, color);
        line(lines, pose, b, c, color);
        line(lines, pose, c, d, color);
        line(lines, pose, d, a, color);
    }

    private static void line(VertexConsumer lines, PoseStack.Pose pose, Vec3 start, Vec3 end, int color) {
        Vec3 normal = end.subtract(start).normalize();
        lines.addVertex(pose, (float) start.x, (float) start.y, (float) start.z).setColor(color)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        lines.addVertex(pose, (float) end.x, (float) end.y, (float) end.z).setColor(color)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }
}
