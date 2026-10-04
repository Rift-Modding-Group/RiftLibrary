package anightdazingzoroark.riftlib.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;

/**
 * Stores a transformed model face and creates texture-aligned particle sections from it on demand.
 * */
@SideOnly(Side.CLIENT)
public class AnimatedBlockParticleFace {
    private final TextureAtlasSprite texture;
    private final float minU;
    private final float maxU;
    private final float minV;
    private final float maxV;
    private final Vector3f topLeft;
    private final Vector3f topRight;
    private final Vector3f bottomLeft;
    private final Vector3f bottomRight;
    private final Vector3f normal;
    private final int columns;
    private final int rows;
    private final float particleScale;

    public AnimatedBlockParticleFace(
            TextureAtlasSprite texture,
            float minU, float maxU, float minV, float maxV,
            Vector3f topLeft, Vector3f topRight, Vector3f bottomLeft, Vector3f bottomRight,
            Vector3f normal, int columns, int rows, float particleScale
    ) {
        this.texture = texture;
        this.minU = minU;
        this.maxU = maxU;
        this.minV = minV;
        this.maxV = maxV;
        this.topLeft = new Vector3f(topLeft);
        this.topRight = new Vector3f(topRight);
        this.bottomLeft = new Vector3f(bottomLeft);
        this.bottomRight = new Vector3f(bottomRight);
        this.normal = new Vector3f(normal);
        this.columns = columns;
        this.rows = rows;
        this.particleScale = particleScale;
    }

    public AnimatedBlockParticleTexture createTexture(int sectionIndex) {
        int row = sectionIndex / this.columns;
        int column = sectionIndex % this.columns;
        float horizontalFactor = (column + 0.5f) / this.columns;
        float verticalFactor = (row + 0.5f) / this.rows;
        float inverseHorizontal = 1f - horizontalFactor;
        float inverseVertical = 1f - verticalFactor;
        float modelX = this.topLeft.getX() * inverseHorizontal * inverseVertical
                + this.topRight.getX() * horizontalFactor * inverseVertical
                + this.bottomLeft.getX() * inverseHorizontal * verticalFactor
                + this.bottomRight.getX() * horizontalFactor * verticalFactor;
        float modelY = this.topLeft.getY() * inverseHorizontal * inverseVertical
                + this.topRight.getY() * horizontalFactor * inverseVertical
                + this.bottomLeft.getY() * inverseHorizontal * verticalFactor
                + this.bottomRight.getY() * horizontalFactor * verticalFactor;
        float modelZ = this.topLeft.getZ() * inverseHorizontal * inverseVertical
                + this.topRight.getZ() * horizontalFactor * inverseVertical
                + this.bottomLeft.getZ() * inverseHorizontal * verticalFactor
                + this.bottomRight.getZ() * horizontalFactor * verticalFactor;
        float sectionMinU = this.minU + (this.maxU - this.minU) * column / this.columns;
        float sectionMaxU = this.minU + (this.maxU - this.minU) * (column + 1) / this.columns;
        float sectionMinV = this.minV + (this.maxV - this.minV) * row / this.rows;
        float sectionMaxV = this.minV + (this.maxV - this.minV) * (row + 1) / this.rows;
        return new AnimatedBlockParticleTexture(
                this.texture,
                sectionMinU, sectionMaxU, sectionMinV, sectionMaxV,
                modelX + this.normal.getX() * 0.01f,
                modelY + this.normal.getY() * 0.01f,
                modelZ + this.normal.getZ() * 0.01f,
                this.normal.getX(), this.normal.getY(), this.normal.getZ(),
                this.particleScale
        );
    }

    public int getSectionCount() {
        return this.columns * this.rows;
    }

    public float getNormalX() {
        return this.normal.getX();
    }

    public float getNormalY() {
        return this.normal.getY();
    }

    public float getNormalZ() {
        return this.normal.getZ();
    }
}
