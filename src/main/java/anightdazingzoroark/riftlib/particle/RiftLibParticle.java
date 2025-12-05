package anightdazingzoroark.riftlib.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RiftLibParticle {
    private final World world;
    public double x, y, z;
    public double prevX, prevY, prevZ;
    public double velX, velY, velZ;
    public float uvXMin, uvYMin, uvXMax, uvYMax;
    public int lifetime, age;
    public float[] size;
    private boolean isDead;

    public RiftLibParticle(World world) {
        this.world = world;
    }

    public void update(RiftLibParticleEmitter emitter) {
        if (this.age < this.lifetime && !this.isDead) this.age++;
        else this.isDead = true;
    }

    //note: this is stricly when "facing_camera_mode" in "minecraft:particle_appearance_billboard" is "lookat_xyz"
    //must be edited to take into account all camera facing modes
    public void renderParticle(BufferBuilder buffer, Entity camera, float partialTicks, float red, float green, float blue, float alpha) {
        //camera position (lerped)
        double camX = camera.prevPosX + (camera.posX - camera.prevPosX) * partialTicks;
        double camY = camera.prevPosY + (camera.posY - camera.prevPosY) * partialTicks;
        double camZ = camera.prevPosZ + (camera.posZ - camera.prevPosZ) * partialTicks;

        //particle position (lerped) in camera space
        double px = this.prevX + (this.x - this.prevX) * partialTicks;
        double py = this.prevY + (this.y - this.prevY) * partialTicks;
        double pz = this.prevZ + (this.z - this.prevZ) * partialTicks;

        double x = px - camX;
        double y = py - camY;
        double z = pz - camZ;

        //half sizes
        float halfX = this.size[0] * 0.5f;
        float halfY = this.size[1] * 0.5f;

        //build billboard basis from camera look direction (rotation)
        Vec3d look = camera.getLook(partialTicks); // camera rotation in world space

        //world up
        Vec3d upWorld = new Vec3d(0.0, 1.0, 0.0);

        //right = forward x upWorld
        Vec3d rightVec = look.crossProduct(upWorld);
        double rightLenSq = rightVec.lengthSquared();

        if (rightLenSq < 1.0e-6) {
            //camera is looking almost straight up/down → use a different up basis
            upWorld = new Vec3d(0.0, 0.0, 1.0);
            rightVec = look.crossProduct(upWorld);
            rightLenSq = rightVec.lengthSquared();
            if (rightLenSq < 1.0e-6) {
                // super-degenerate fallback: just use X axis
                rightVec = new Vec3d(1.0, 0.0, 0.0);
            }
        }

        rightVec = rightVec.normalize();

        //up = right x forward (orthonormal basis)
        Vec3d upVec = rightVec.crossProduct(look).normalize();

        float rightX = (float) rightVec.x;
        float rightY = (float) rightVec.y;
        float rightZ = (float) rightVec.z;

        float upX = (float) upVec.x;
        float upY = (float) upVec.y;
        float upZ = (float) upVec.z;

        //compute 4 corners (in camera space)
        float xOne = -rightX * halfX + upX * halfY;
        float yOne = -rightY * halfX + upY * halfY;
        float zOne = -rightZ * halfX + upZ * halfY;

        float xTwo = -rightX * halfX - upX * halfY;
        float yTwo = -rightY * halfX - upY * halfY;
        float zTwo = -rightZ * halfX - upZ * halfY;

        float xThree =  rightX * halfX - upX * halfY;
        float yThree =  rightY * halfX - upY * halfY;
        float zThree =  rightZ * halfX - upZ * halfY;

        float xFour =  rightX * halfX + upX * halfY;
        float yFour =  rightY * halfX + upY * halfY;
        float zFour =  rightZ * halfX + upZ * halfY;

        //lighting
        int light = this.getBrightnessForRender();
        int j = (light >> 16) & 0xFFFF;
        int k = light & 0xFFFF;

        //emit vertices
        buffer.pos(x + xOne, y + yOne, z + zOne)
                .tex(this.uvXMax, this.uvYMin)
                .lightmap(j, k)
                .color(red, green, blue, alpha)
                .endVertex();

        buffer.pos(x + xTwo, y + yTwo, z + zTwo)
                .tex(this.uvXMax, this.uvYMax)
                .lightmap(j, k)
                .color(red, green, blue, alpha)
                .endVertex();

        buffer.pos(x + xThree, y + yThree, z + zThree)
                .tex(this.uvXMin, this.uvYMax)
                .lightmap(j, k)
                .color(red, green, blue, alpha)
                .endVertex();

        buffer.pos(x + xFour, y + yFour, z + zFour)
                .tex(this.uvXMin, this.uvYMin)
                .lightmap(j, k)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private int getBrightnessForRender() {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        return this.world.isBlockLoaded(blockpos) ? this.world.getCombinedLight(blockpos, 0) : 0;
    }

    public boolean isDead() {
        return this.isDead;
    }
}
