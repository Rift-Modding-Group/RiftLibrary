package anightdazingzoroark.riftlib.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class RiftLibParticle extends Particle {
    private final TextureAtlasSprite texture;
    private final int textureWidth, textureHeight;
    private final int u, v, uvWidth, uvHeight;
    private final ParticleBuilder builder;
    private final double expirationValue;

    public RiftLibParticle(ParticleBuilder builder, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
        this(builder, worldIn, xCoordIn, yCoordIn, zCoordIn, 0, 0, 0);
    }

    public RiftLibParticle(ParticleBuilder builder, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.setSize((float) builder.size[0].get(), (float) builder.size[1].get());
        this.setMaxAge((int) builder.particleExpirationValue.get());
        this.texture = ParticleTextureStitcher.SPRITES.get(builder.texture);
        this.textureWidth = builder.textureWidth;
        this.textureHeight = builder.textureHeight;
        this.u = (int) builder.uv[0].get();
        this.v = (int) builder.uv[1].get();
        this.uvWidth = (int) builder.uvSize[0].get();
        this.uvHeight = (int) builder.uvSize[1].get();
        this.expirationValue = builder.particleExpirationValue.get();
        this.builder = builder;
    }

    /*
    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.expirationValue > 0) this.setExpired();
    }
     */

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        // lerped position relative to camera
        double x = this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX;
        double y = this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY;
        double z = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ;

        float scale = this.particleScale; // or from builder/expressions

        // compute UVs from JSON+sprite
        float spriteMinU = this.texture.getMinU();
        float spriteMaxU = this.texture.getMaxU();
        float spriteMinV = this.texture.getMinV();
        float spriteMaxV = this.texture.getMaxV();

        float spriteWidthU  = spriteMaxU - spriteMinU;
        float spriteHeightV = spriteMaxV - spriteMinV;

        float u0 = spriteMinU + (this.u       / (float) this.textureWidth)  * spriteWidthU;
        float v0 = spriteMinV + (this.v       / (float) this.textureHeight) * spriteHeightV;
        float u1 = spriteMinU + ((this.u + this.uvWidth) / (float) this.textureWidth)  * spriteWidthU;
        float v1 = spriteMinV + ((this.v + this.uvHeight) / (float) this.textureHeight) * spriteHeightV;

        // standard billboard math from Particle#renderParticle
        float x1 = (float)(-rotationX * scale - rotationXY * scale);
        float y1 = (float)(-rotationZ * scale);
        float z1 = (float)(-rotationYZ * scale - rotationXZ * scale);
        float x2 = (float)(-rotationX * scale + rotationXY * scale);
        float y2 = (float)( rotationZ * scale);
        float z2 = (float)(-rotationYZ * scale + rotationXZ * scale);
        float x3 = (float)( rotationX * scale + rotationXY * scale);
        float y3 = (float)( rotationZ * scale);
        float z3 = (float)( rotationYZ * scale + rotationXZ * scale);
        float x4 = (float)( rotationX * scale - rotationXY * scale);
        float y4 = (float)(-rotationZ * scale);
        float z4 = (float)( rotationYZ * scale - rotationXZ * scale);

        int brightness = this.getBrightnessForRender(partialTicks);
        int j = brightness >> 16 & 65535;
        int k = brightness & 65535;

        buffer.pos(x + x1, y + y1, z + z1).tex(u1, v1)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                .lightmap(j, k).endVertex();
        buffer.pos(x + x2, y + y2, z + z2).tex(u1, v0)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                .lightmap(j, k).endVertex();
        buffer.pos(x + x3, y + y3, z + z3).tex(u0, v0)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                .lightmap(j, k).endVertex();
        buffer.pos(x + x4, y + y4, z + z4).tex(u0, v1)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                .lightmap(j, k).endVertex();
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
