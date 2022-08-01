package dzwdz.microdurability;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class DurabilityRenderer extends DrawableHelper implements HudRenderCallback {

    private static final Identifier TEX = new Identifier("microdurability", "textures/gui/icons.png");
    private final MinecraftClient client;

    private float time = 0;

    public DurabilityRenderer() {
        client = MinecraftClient.getInstance();
    }

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        if (!client.interactionManager.hasStatusBars())
            return;

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        time = (time + tickDelta) % (EntryPoint.CONFIG.blinkTime * 40f);

        // render the held item warning
        if (EntryPoint.CONFIG.toolWarning) {
            for (ItemStack s : client.player.getHandItems()) {
                if (EntryPoint.shouldWarn(s)) {
                    client.getTextureManager().bindTexture(TEX);
                    // todo: this doesn't align with the crosshair at some resolutions
                    drawTexture(matrixStack, scaledWidth / 2 - 2, scaledHeight / 2 - 18, 0, 0, 3, 11);
                    client.getTextureManager().bindTexture(GUI_ICONS_TEXTURE);
                    break;
                }
            }
        }

        // render the armor durability
        int x = scaledWidth / 2 - 7;
        int y = scaledHeight - 30;
        if (client.player.experienceLevel > 0)
            y -= 6;
        if (EntryPoint.CONFIG.blinkTime > 0)
            for (ItemStack s : client.player.getArmorItems())
                if (time < EntryPoint.CONFIG.blinkTime * 20f && EntryPoint.shouldWarn(s))
                    return;

        for (ItemStack s : client.player.getArmorItems())
            renderBar(s, x, y -= 3);
    }

    public static void renderBar(ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty())
            return;
        if (!EntryPoint.CONFIG.undamagedBars && !stack.isItemBarVisible())
            return;
        if (!stack.isDamageable())
            return;

        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        int width = stack.getItemBarStep();
        int color = stack.getItemBarColor();
        renderGuiQuad(bufferBuilder, x, y, 13, 2, 0, 0, 0, 255);
        renderGuiQuad(bufferBuilder, x, y, width, 1, color >> 16 & 255, color >> 8 & 255, color & 255, 255);

        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    private static void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue,
            int alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0D).color(red, green, blue, alpha).next();
        BufferRenderer.drawWithShader(buffer.end());
    }
}
