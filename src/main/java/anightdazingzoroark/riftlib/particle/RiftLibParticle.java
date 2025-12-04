package anightdazingzoroark.riftlib.particle;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftLibParticle {
    private final World world;
    private final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

    public double x, y, z;
    public double prevX, prevY, prevZ;
    public double velX, velY, velZ;
    public float uvXMin, uvYMin, uvXMax, uvYMax;
    public int lifetime;
    public float[] size;
    public boolean isDead;

    public RiftLibParticle(World world) {
        this.world = world;
    }

    public void update(RiftLibParticleEmitter emitter) {}

    public void renderParticle(BufferBuilder buffer, Entity camera, float partialTicks, float red, float green, float blue, float alpha) {
        //camera-relative position & rotations
        double camX = camera.prevPosX + (camera.posX - camera.prevPosX) * partialTicks;
        double camY = camera.prevPosY + (camera.posY - camera.prevPosY) * partialTicks;
        double camZ = camera.prevPosZ + (camera.posZ - camera.prevPosZ) * partialTicks;

        float rotX  = ActiveRenderInfo.getRotationX();
        float rotZ  = ActiveRenderInfo.getRotationZ();
        float rotYZ = ActiveRenderInfo.getRotationYZ();
        float rotXY = ActiveRenderInfo.getRotationXY();
        float rotXZ = ActiveRenderInfo.getRotationXZ();

        //lerp position
        double x = this.prevX + (this.x - this.prevX) * partialTicks - camX;
        double y = this.prevY + (this.y - this.prevY) * partialTicks - camY;
        double z = this.prevZ + (this.z - this.prevZ) * partialTicks - camZ;

        //scale
        float x1 = -rotX * this.size[0] - rotXY * this.size[0];
        float y1 = -rotZ * this.size[1];
        float z1 = -rotYZ * this.size[0] - rotXZ * this.size[0];
        float x2 = -rotX * this.size[0] + rotXY * this.size[0];
        float y2 = rotZ * this.size[1];
        float z2 = -rotYZ * this.size[0] + rotXZ * this.size[0];
        float x3 = rotX * this.size[0] + rotXY * this.size[0];
        float y3 = rotZ * this.size[1];
        float z3 = rotYZ * this.size[0] + rotXZ * this.size[0];
        float x4 = rotX * this.size[0] - rotXY * this.size[0];
        float y4 = -rotZ * this.size[1];
        float z4 = rotYZ * this.size[0] - rotXZ * this.size[0];

        int light = this.getBrightnessForRender(partialTicks);
        int j = (light >> 16) & 0xFFFF;
        int k = light & 0xFFFF;

        buffer.pos(x + x1, y + y1, z + z1)
                .tex(this.uvXMax, this.uvYMax)
                .lightmap(j, k)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(x + x2, y + y2, z + z2)
                .tex(this.uvXMax, this.uvYMax)
                .lightmap(j, k)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(x + x3, y + y3, z + z3)
                .tex(this.uvXMax, this.uvYMax)
                .lightmap(j, k)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(x + x4, y + y4, z + z4)
                .tex(this.uvXMax, this.uvYMax)
                .lightmap(j, k)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private int getBrightnessForRender(float partialTicks) {
        if (this.world == null) return 15728880;

        double x = this.prevX + (this.x - this.prevX) * partialTicks;
        double y = this.prevY + (this.y - this.prevY) * partialTicks;
        double z = this.prevZ + (this.z - this.prevZ) * partialTicks;

        this.mutableBlockPos.setPos(x, y, z);
        return this.world.isBlockLoaded(this.mutableBlockPos) ? this.world.getCombinedLight(this.mutableBlockPos, 0): 0;
    }
}
