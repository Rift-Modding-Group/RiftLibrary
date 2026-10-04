package anightdazingzoroark.riftlib.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Describes one model-face section and exposes its subsection of the stitched block texture.
 * */
@SideOnly(Side.CLIENT)
public class AnimatedBlockParticleTexture extends TextureAtlasSprite {
    private final float minU;
    private final float maxU;
    private final float minV;
    private final float maxV;
    private final float modelX;
    private final float modelY;
    private final float modelZ;
    private final float normalX;
    private final float normalY;
    private final float normalZ;
    private final float particleScale;

    public AnimatedBlockParticleTexture(
            TextureAtlasSprite texture,
            float minU, float maxU, float minV, float maxV,
            float modelX, float modelY, float modelZ,
            float normalX, float normalY, float normalZ,
            float particleScale
    ) {
        super(texture.getIconName());
        this.minU = texture.getInterpolatedU(minU * 16f);
        this.maxU = texture.getInterpolatedU(maxU * 16f);
        this.minV = texture.getInterpolatedV(minV * 16f);
        this.maxV = texture.getInterpolatedV(maxV * 16f);
        this.modelX = modelX;
        this.modelY = modelY;
        this.modelZ = modelZ;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.particleScale = particleScale;
    }

    @Override
    public float getMinU() {
        return this.minU;
    }

    @Override
    public float getMaxU() {
        return this.maxU;
    }

    @Override
    public float getMinV() {
        return this.minV;
    }

    @Override
    public float getMaxV() {
        return this.maxV;
    }

    public float getModelX() {
        return this.modelX;
    }

    public float getModelY() {
        return this.modelY;
    }

    public float getModelZ() {
        return this.modelZ;
    }

    public float getNormalX() {
        return this.normalX;
    }

    public float getNormalY() {
        return this.normalY;
    }

    public float getNormalZ() {
        return this.normalZ;
    }

    public float getParticleScale() {
        return this.particleScale;
    }
}
