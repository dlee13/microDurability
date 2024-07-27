package dzwdz.microdurability;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;

public class DurabilityRenderer implements HudRenderCallback {

    private static final Identifier TEX = Identifier.of("microdurability", "textures/gui/icons.png");
    private static final int MASK = Argb.getArgb(0xFF, 0x00, 0x00, 0x00);
    private final MinecraftClient client;

    private float time = 0;

    public DurabilityRenderer() {
        client = MinecraftClient.getInstance();
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (!client.interactionManager.hasStatusBars())
            return;

        final int scaledWidth = client.getWindow().getScaledWidth();
        final int scaledHeight = client.getWindow().getScaledHeight();
        time = (time + tickCounter.getTickDelta(false)) % (EntryPoint.CONFIG.blinkTime * 40f);

        // render the held item warning
        if (EntryPoint.CONFIG.toolWarning) {
            for (ItemStack stack : client.player.getHandItems()) {
                if (EntryPoint.shouldWarn(stack, client.world)) {
                    // todo: this doesn't align with the crosshair at some resolutions
                    drawContext.drawTexture(TEX, scaledWidth / 2 - 2, scaledHeight / 2 - 18, 0, 0, 3, 11);
                    break;
                }
            }
        }

        // render the armor durability
        if (EntryPoint.CONFIG.blinkTime > 0) {
            for (ItemStack stack : client.player.getArmorItems()) {
                if (time < EntryPoint.CONFIG.blinkTime * 20f && EntryPoint.shouldWarn(stack, client.world)) {
                    return;
                }
            }
        }

        int x = (scaledWidth / 2) - 7;
        int y = scaledHeight - (client.player.experienceLevel > 0 ? 36 : 30);

        for (ItemStack stack : client.player.getArmorItems()) {
            drawItemBar(drawContext, stack, x, y -= 3);
        }
    }

    private static void drawItemBar(DrawContext drawContext, ItemStack stack, int x, int y) {
        if (stack == null || !stack.isItemBarVisible()) {
            return;
        }
        final int width = stack.getItemBarStep();
        final int color = stack.getItemBarColor();
        drawContext.fill(RenderLayer.getGuiOverlay(), x, y, x + 13, y + 2, MASK);
        drawContext.fill(RenderLayer.getGuiOverlay(), x, y, x + width, y + 1, color | MASK);
    }
}
