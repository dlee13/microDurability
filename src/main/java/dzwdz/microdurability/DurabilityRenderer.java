package dzwdz.microdurability;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class DurabilityRenderer extends DrawableHelper implements HudRenderCallback {

    private static final Identifier TEX = new Identifier("microdurability", "textures/gui/icons.png");
    private final MinecraftClient client;
    private final ItemRenderer itemRenderer;

    private float time = 0;

    public DurabilityRenderer() {
        client = MinecraftClient.getInstance();
        itemRenderer = client.getItemRenderer();
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
            for (ItemStack stack : client.player.getHandItems()) {
                if (EntryPoint.shouldWarn(stack)) {
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
            for (ItemStack stack : client.player.getArmorItems())
                if (time < EntryPoint.CONFIG.blinkTime * 20f && EntryPoint.shouldWarn(stack))
                    return;

        for (ItemStack stack : client.player.getArmorItems()) {
            itemRenderer.renderGuiItemOverlay(client.textRenderer, stack, x, y -= 3);
        }
    }
}
