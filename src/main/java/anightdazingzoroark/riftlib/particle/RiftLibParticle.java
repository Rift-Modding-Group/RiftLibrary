package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Variable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RiftLibParticle {
    private final World world;
    private final MolangParser molangParser;
    public double x, y, z;
    public double prevX, prevY, prevZ;
    public double velX, velY, velZ;
    public float uvXMin, uvYMin, uvXMax, uvYMax;
    public IValue[] size;
    private boolean isDead;

    //molang particle variables
    private Variable varParticleAge;
    private Variable varParticleLifetime;
    private Variable varParticleRandomOne;
    private Variable varParticleRandomTwo;
    private Variable varParticleRandomThree;
    private Variable varParticleRandomFour;

    //runtime data, are parsed molang variables
    public int lifetime, age; //REMEMBER THAT THESE ARE IN TICKS
    public float randomOne, randomTwo, randomThree, randomFour;

    public RiftLibParticle(World world, MolangParser molangParser) {
        this.world = world;
        this.molangParser = molangParser;

        //init molang stuff
        this.setupMolangVariables();
        this.initParticleRandoms();
    }

    //all molang variables are created here
    private void setupMolangVariables() {
        this.varParticleAge = this.getOrCreateVar("variable.particle_age");
        this.varParticleLifetime = this.getOrCreateVar("variable.particle_lifetime");
        this.varParticleRandomOne = this.getOrCreateVar("variable.particle_random_1");
        this.varParticleRandomTwo = this.getOrCreateVar("variable.particle_random_2");
        this.varParticleRandomThree = this.getOrCreateVar("variable.particle_random_3");
        this.varParticleRandomFour = this.getOrCreateVar("variable.particle_random_4");
    }

    private void initParticleRandoms() {
        this.randomOne = (float) Math.random();
        this.randomTwo = (float) Math.random();
        this.randomThree = (float) Math.random();
        this.randomFour = (float) Math.random();
    }

    private Variable getOrCreateVar(String name) {
        Variable v = this.molangParser.variables.get(name);
        if (v == null) {
            v = new Variable(name, 0.0);
            this.molangParser.register(v);
        }
        return v;
    }

    public void update(RiftLibParticleEmitter emitter) {
        //dynamically set molang variables
        if (this.varParticleAge != null) this.varParticleAge.set(this.age / 20D);
        if (this.varParticleLifetime != null) this.varParticleLifetime.set(this.lifetime / 20D);
        if (this.varParticleRandomOne != null) this.varParticleRandomOne.set(this.randomOne);
        if (this.varParticleRandomTwo != null) this.varParticleRandomTwo.set(this.randomTwo);
        if (this.varParticleRandomThree != null) this.varParticleRandomThree.set(this.randomThree);
        if (this.varParticleRandomFour != null) this.varParticleRandomFour.set(this.randomFour);

        //update life
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
        float halfX = (float) (this.size[0].get()) * 0.5f;
        float halfY = (float) (this.size[1].get()) * 0.5f;

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
