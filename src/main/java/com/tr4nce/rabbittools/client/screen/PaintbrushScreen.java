package com.tr4nce.rabbittools.client.screen;

import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.items.tool.PaintbrushItem;
import com.tr4nce.rabbittools.items.tool.PaintbrushMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;

public final class PaintbrushScreen extends AbstractContainerScreen<PaintbrushMenu> {
    private static final ResourceLocation TEXTURE = RabbitTools.loc("textures/gui/paintbrush.png");
    private static final int TEXT = 0x3E3634;
    private final Button[] swatches = new Button[16];
    private Button loadOne;
    private Button loadAll;

    public PaintbrushScreen(PaintbrushMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 216;
        imageHeight = 217;
        inventoryLabelX = 28;
        inventoryLabelY = 121;
    }

    @Override
    protected void init() {
        super.init();
        for (DyeColor color : DyeColor.values()) {
            int index = color.getId();
            swatches[index] = addRenderableWidget(new Swatch(color,
                    leftPos + 11 + index % 4 * 24, topPos + 23 + index / 4 * 23));
        }
        loadOne = addRenderableWidget(Button.builder(Component.translatable("gui.rabbittools.paintbrush.load_one"),
                button -> sendButton(PaintbrushMenu.LOAD_ONE)).bounds(leftPos + 120, topPos + 73, 88, 19).build());
        loadAll = addRenderableWidget(Button.builder(Component.translatable("gui.rabbittools.paintbrush.load_all"),
                button -> sendButton(PaintbrushMenu.LOAD_ALL)).bounds(leftPos + 120, topPos + 94, 88, 19).build());
        refreshControls();
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void refreshControls() {
        boolean canFill = menu.fillableDyes() > 0;
        loadOne.active = canFill;
        loadAll.active = canFill;
        for (DyeColor color : DyeColor.values()) {
            Button button = swatches[color.getId()];
            int points = menu.points(color);
            button.active = points > 0;
            button.setMessage(Component.translatable("gui.rabbittools.paintbrush.color_points",
                    DyeItem.byColor(color).getDescription(), points, PaintbrushItem.CAPACITY));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshControls();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        for (Button swatch : swatches) {
            if (mouseX >= swatch.getX() && mouseX < swatch.getX() + swatch.getWidth()
                    && mouseY >= swatch.getY() && mouseY < swatch.getY() + swatch.getHeight()) {
                graphics.renderTooltip(font, swatch.getMessage(), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
        Component colorName = menu.inputStack().getItem() instanceof DyeItem dye
                ? dye.getDescription()
                : Component.translatable("gui.rabbittools.paintbrush.insert_dye");
        Component preview = Component.translatable("gui.rabbittools.paintbrush.preview",
                menu.fillableDyes(), menu.fillableDyes() * PaintbrushItem.POINTS_PER_DYE);
        graphics.drawString(font, colorName, 164 - font.width(colorName) / 2, 21, TEXT, false);
        if (menu.inputStack().getItem() instanceof DyeItem dye) {
            String balance = menu.points(dye.getDyeColor()) + "/" + PaintbrushItem.CAPACITY;
            graphics.drawString(font, balance, 164 - font.width(balance) / 2, 51, TEXT, false);
        }
        graphics.drawString(font, preview, 164 - font.width(preview) / 2, 62, TEXT, false);
    }

    private final class Swatch extends Button {
        private final DyeColor color;

        private Swatch(DyeColor color, int x, int y) {
            super(x, y, 22, 21, Component.empty(), button -> sendButton(color.getId()), DEFAULT_NARRATION);
            this.color = color;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            boolean selected = color == menu.selected();
            int rgb = color.getTextureDiffuseColor() | 0xFF000000;
            graphics.fill(x, y, x + 22, y + 21, selected ? 0xFFF3D292 : 0xFF534647);
            graphics.fill(x + 1, y + 1, x + 21, y + 20, 0xFF302A2E);
            graphics.fill(x + 2, y + 2, x + 20, y + 11, rgb);
            graphics.fill(x + 2, y + 2, x + 20, y + 3, 0x39FFF0DB);
            graphics.fill(x + 2, y + 10, x + 20, y + 11, 0x380D091C);
            if (selected) {
                graphics.fill(x + 14, y + 6, x + 16, y + 8, 0xFFF3D292);
                graphics.fill(x + 15, y + 7, x + 17, y + 9, 0xFFF3D292);
                graphics.fill(x + 16, y + 5, x + 18, y + 7, 0xFFF3D292);
                graphics.fill(x + 17, y + 4, x + 19, y + 6, 0xFFF3D292);
            }
            if (isHoveredOrFocused() && active) {
                graphics.renderOutline(x, y, 22, 21, 0xFFF0E3CC);
            }
            graphics.drawCenteredString(font, Integer.toString(menu.points(color)), x + 11, y + 12,
                    active ? 0xFFF1E7D9 : 0xFFB3A3A0);
        }
    }
}
