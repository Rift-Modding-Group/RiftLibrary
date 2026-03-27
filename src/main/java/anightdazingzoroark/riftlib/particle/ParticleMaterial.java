package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.exceptions.InvalidMaterialException;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public enum ParticleMaterial {
    //Enables color blending and transparency in colored pixels, uses an additive blend mode
    ADD(() -> {
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE
        );
        GlStateManager.depthMask(false);
    }),
    //Pixels with an colorAlpha of less than 255 will be fully transparent, colored pixels will always be opaque
    ALPHA(() -> {
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.99f);
        GlStateManager.disableCull();
    }),
    //Enables color blending and transparency in colored pixels, uses a normal blend mode
    BLEND(() -> {
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569f);
        GlStateManager.depthMask(true);
    }),
    //No transparency whatsoever in all colors, including uncolored areas
    OPAQUE(() -> {
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0f);
        GlStateManager.disableCull();
        GlStateManager.depthMask(true);
    });

    private final Runnable beginDraw;

    ParticleMaterial(Runnable beginDraw) {
        this.beginDraw = beginDraw;
    }

    public void beginDraw() {
        this.beginDraw.run();
    }

    public void endDraw() {
        //reset default global state
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();

        //restore default alpha test and blend func
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
    }

    public static ParticleMaterial getMaterialFromString(String value) {
        return switch (value) {
            case "particles_add" -> ADD;
            case "particles_alpha" -> ALPHA;
            case "particles_blend" -> BLEND;
            case "particles_opaque" -> OPAQUE;
            default -> throw new InvalidMaterialException(value, "Invalid particle material");
        };
    }
}
