package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Variable;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RiftLibParticle {
    private final World world;
    private final MolangParser molangParser;
    public double x, y, z;
    public double prevX, prevY, prevZ;
    public double velX, velY, velZ;
    public double accelX, accelY, accelZ;
    public float uvXMin, uvYMin, uvXMax, uvYMax;
    public IValue[] size;
    private boolean isDead;
    public boolean particleUseLocalLighting;

    public ParticleCameraMode cameraMode;

    //flipbook data (in pixels)
    public boolean flipbook;
    public boolean flipbookStretchToLifetime;
    public boolean flipbookLoop;
    public int texWidth, texHeight;
    public float baseUVX, baseUVY;
    public float sizeUVX, sizeUVY;
    public float stepUVX, stepUVY;
    public int maxFrame;
    public float fps;

    //molang particle variables
    private Variable varParticleAge;
    private Variable varParticleLifetime;
    private Variable varParticleRandomOne;
    private Variable varParticleRandomTwo;
    private Variable varParticleRandomThree;
    private Variable varParticleRandomFour;

    //molang emitter variables, which the emitter shares with its particles
    public Variable varEmitterAge;
    public Variable varEmitterLifetime;
    public Variable varEmitterRandomOne;
    public Variable varEmitterRandomTwo;
    public Variable varEmitterRandomThree;
    public Variable varEmitterRandomFour;

    //IValue lifetime info
    public IValue lifetimeExpression;
    public IValue expirationExpression;

    //additional molang variables
    public List<Variable> additionalVariables = new ArrayList<>();

    //runtime data, are parsed molang variables
    public int lifetime, age; //REMEMBER THAT THESE ARE IN TICKS
    public float randomOne, randomTwo, randomThree, randomFour;

    //for color
    public IValue[] colorArray = new IValue[]{MolangParser.ONE, MolangParser.ONE, MolangParser.ONE};
    public IValue colorAlpha = MolangParser.ONE;

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

    public void update() {
        //set the lifetime from expression
        this.lifetime = (int) (this.lifetimeExpression.get() * 20);

        //dynamically set molang variables
        if (this.varParticleAge != null) this.varParticleAge.set(this.age / 20D);
        if (this.varParticleLifetime != null) this.varParticleLifetime.set(this.lifetime / 20D);
        if (this.varParticleRandomOne != null) this.varParticleRandomOne.set(this.randomOne);
        if (this.varParticleRandomTwo != null) this.varParticleRandomTwo.set(this.randomTwo);
        if (this.varParticleRandomThree != null) this.varParticleRandomThree.set(this.randomThree);
        if (this.varParticleRandomFour != null) this.varParticleRandomFour.set(this.randomFour);

        //update life based on age and expiration
        if (!this.isDead) {
            if (this.age < this.lifetime) this.age++;
            if (this.age >= this.lifetime || this.expirationExpression.get() != 0) this.isDead = true;
        }

        //update flipbook
        if (this.flipbook) this.updateFlipbookUV();

        //move based on velocity
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;

        this.x += this.velX;
        this.y += this.velY;
        this.z += this.velZ;

        //change velocity based on acceleration
        this.velX += this.accelX;
        this.velY += this.accelY;
        this.velZ += this.accelZ;
    }

    public void renderParticle(BufferBuilder buffer, Entity camera, float partialTicks) {
        //camera position (lerped)
        double camX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double camY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
        double camZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;

        //particle position (lerped) in camera space
        double px = this.prevX + (this.x - this.prevX) * partialTicks;
        double py = this.prevY + (this.y - this.prevY) * partialTicks;
        double pz = this.prevZ + (this.z - this.prevZ) * partialTicks;

        //origin
        Vec3d pointOrigin = new Vec3d(px - camX, py - camY, pz - camZ);

        //half sizes
        float halfScaleX = (float) (this.size[0].get()) * 0.5f;
        float halfScaleY = (float) (this.size[1].get()) * 0.5f;

        //camera look direction
        Vec3d look = camera.getLook(partialTicks);

        //everything from here on out will depend on the camera mode
        //rotate xyz emulates vanilla particle rendering
        if (this.cameraMode == ParticleCameraMode.ROTATE_XYZ) {
            float rotationX = ActiveRenderInfo.getRotationX();
            float rotationZ = ActiveRenderInfo.getRotationZ();
            float rotationYZ = ActiveRenderInfo.getRotationYZ();
            float rotationXY = ActiveRenderInfo.getRotationXY();
            float rotationXZ = ActiveRenderInfo.getRotationXZ();

            //compute 4 corners (in camera space)
            Vec3d pointOne = new Vec3d(
                    -rotationX * halfScaleX - rotationYZ * halfScaleX,
                    -rotationXZ * halfScaleY,
                    -rotationZ * halfScaleX - rotationXY * halfScaleX
            );
            Vec3d pointTwo = new Vec3d(
                    -rotationX * halfScaleX + rotationYZ * halfScaleX,
                    rotationXZ * halfScaleY,
                    -rotationZ * halfScaleX + rotationXY * halfScaleX
            );
            Vec3d pointThree = new Vec3d(
                    rotationX * halfScaleX + rotationYZ * halfScaleX,
                    rotationXZ * halfScaleY,
                    rotationZ * halfScaleX + rotationXY * halfScaleX
            );
            Vec3d pointFour = new Vec3d(
                    rotationX * halfScaleX - rotationYZ * halfScaleX,
                    -rotationXZ * halfScaleY,
                    rotationZ * halfScaleX - rotationXY * halfScaleX
            );

            this.emitQuad(buffer, pointOrigin, pointOne, pointTwo, pointThree, pointFour, partialTicks);
        }
        //some camera modes that share common code go here
        else {
            //common vectors
            Vec3d upWorld = new Vec3d(0, 1, 0);
            Vec3d horizontalVec = Vec3d.ZERO;
            Vec3d verticalVec = Vec3d.ZERO;

            //rotate only around world Y (billboard stands upright)
            if (this.cameraMode == ParticleCameraMode.ROTATE_Y) {
                float yaw = (float) Math.toRadians(camera.prevRotationYaw + (camera.rotationYaw - camera.prevRotationYaw) * partialTicks);

                float cos = MathHelper.cos(yaw);
                float sin = MathHelper.sin(yaw);

                horizontalVec = new Vec3d(-cos, 0, -sin);
                verticalVec = new Vec3d(0,  1,  0);
            }
            //face camera only in XZ plane, keep Y upright
            else if (this.cameraMode == ParticleCameraMode.LOOKAT_Y) {
                horizontalVec = look.crossProduct(upWorld).normalize();
                verticalVec = new Vec3d(0, 1, 0);
            }
            //face camera in XYZ plane, biased towards world y up
            else if (this.cameraMode == ParticleCameraMode.LOOKAT_XYZ) {
                horizontalVec = look.crossProduct(upWorld);

                //mainly to make the particle be visible from above
                double rightLenSq = horizontalVec.lengthSquared();
                if (rightLenSq < 1e-6) {
                    upWorld = new Vec3d(0, 0, 1);
                    horizontalVec = look.crossProduct(upWorld);
                    rightLenSq = horizontalVec.lengthSquared();
                    if (rightLenSq < 1e-6) {
                        horizontalVec = new Vec3d(1, 0, 0);
                    }
                }

                horizontalVec = horizontalVec.normalize();
                verticalVec = horizontalVec.crossProduct(look).normalize();
            }

            //compute 4 corners (in camera space)
            Vec3d pointOne = new Vec3d(
                    -horizontalVec.x * halfScaleX + verticalVec.x * halfScaleY,
                    -horizontalVec.y * halfScaleX + verticalVec.y * halfScaleY,
                    -horizontalVec.z * halfScaleX + verticalVec.z * halfScaleY
            );
            Vec3d pointTwo = new Vec3d(
                    -horizontalVec.x * halfScaleX - verticalVec.x * halfScaleY,
                    -horizontalVec.y * halfScaleX - verticalVec.y * halfScaleY,
                    -horizontalVec.z * halfScaleX - verticalVec.z * halfScaleY
            );
            Vec3d pointThree = new Vec3d(
                    horizontalVec.x * halfScaleX - verticalVec.x * halfScaleY,
                    horizontalVec.y * halfScaleX - verticalVec.y * halfScaleY,
                    horizontalVec.z * halfScaleX - verticalVec.z * halfScaleY
            );
            Vec3d pointFour = new Vec3d(
                    horizontalVec.x * halfScaleX + verticalVec.x * halfScaleY,
                    horizontalVec.y * halfScaleX + verticalVec.y * halfScaleY,
                    horizontalVec.z * halfScaleX + verticalVec.z * halfScaleY
            );

            this.emitQuad(buffer, pointOrigin, pointOne, pointTwo, pointThree, pointFour, partialTicks);
        }
    }

    private void emitQuad(BufferBuilder buffer, Vec3d pointOrigin, Vec3d pointOne, Vec3d pointTwo, Vec3d pointThree, Vec3d pointFour, float partialTicks) {
        //lighting
        int light = this.getBrightnessForRender(partialTicks);
        int j = (light >> 16) & 0xFFFF;
        int k = light & 0xFFFF;

        //colors
        float red = (float) this.colorArray[0].get();
        float green = (float) this.colorArray[1].get();
        float blue = (float) this.colorArray[2].get();
        float alpha = (float) this.colorAlpha.get();

        //emit vertices based on camera mode too
        if (this.cameraMode == ParticleCameraMode.ROTATE_XYZ) {
            buffer.pos(pointOrigin.x + pointOne.x, pointOrigin.y + pointOne.y, pointOrigin.z + pointOne.z)
                    .tex(this.uvXMax, this.uvYMax)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointTwo.x, pointOrigin.y + pointTwo.y, pointOrigin.z + pointTwo.z)
                    .tex(this.uvXMax, this.uvYMin)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointThree.x, pointOrigin.y + pointThree.y, pointOrigin.z + pointThree.z)
                    .tex(this.uvXMin, this.uvYMin)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointFour.x, pointOrigin.y + pointFour.y, pointOrigin.z + pointFour.z)
                    .tex(this.uvXMin, this.uvYMax)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();
        }
        else {
            buffer.pos(pointOrigin.x + pointOne.x, pointOrigin.y + pointOne.y, pointOrigin.z + pointOne.z)
                    .tex(this.uvXMax, this.uvYMin)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointTwo.x, pointOrigin.y + pointTwo.y, pointOrigin.z + pointTwo.z)
                    .tex(this.uvXMax, this.uvYMax)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointThree.x, pointOrigin.y + pointThree.y, pointOrigin.z + pointThree.z)
                    .tex(this.uvXMin, this.uvYMax)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointFour.x, pointOrigin.y + pointFour.y, pointOrigin.z + pointFour.z)
                    .tex(this.uvXMin, this.uvYMin)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();
        }
    }

    private int getBrightnessForRender(float partialTicks) {
        //fullbright when lighting component is NOT present
        if (!this.particleUseLocalLighting || this.world == null) return 0xF000F0;

        double x = this.prevX + (this.x - this.prevX) * partialTicks;
        double y = this.prevY + (this.y - this.prevY) * partialTicks;
        double z = this.prevZ + (this.z - this.prevZ) * partialTicks;

        BlockPos blockpos = new BlockPos(x, y, z);
        return this.world.isBlockLoaded(blockpos) ? this.world.getCombinedLight(blockpos, 0) : 0;
    }

    public void updateFlipbookUV() {
        if (!this.flipbook || this.maxFrame <= 0 || this.texWidth <= 0 || this.texHeight <= 0) return;

        //compute frame
        float timePercentage = this.age / (float) this.lifetime;
        float ageSeconds = this.age / 20.0f;
        float frame = this.flipbookStretchToLifetime && this.lifetime > 0 ? timePercentage * this.maxFrame : ageSeconds * this.fps;

        //wrap around
        if (this.flipbookLoop) {
            frame = frame % this.maxFrame;
            if (frame < 0) frame += this.maxFrame;
        }
        //clamp
        else {
            if (frame >= this.maxFrame) frame = this.maxFrame - 1;
            if (frame < 0) frame = 0;
        }

        float uPixels = this.baseUVX + this.stepUVX * (float) Math.floor(frame);
        float vPixels = this.baseUVY + this.stepUVY * (float) Math.floor(frame);

        this.uvXMin = uPixels / this.texWidth;
        this.uvYMin = vPixels / this.texHeight;
        this.uvXMax = (uPixels + this.sizeUVX) / this.texWidth;
        this.uvYMax = (vPixels + this.sizeUVY) / this.texHeight;
    }

    public boolean isDead() {
        return this.isDead;
    }
}
