package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Variable;
import anightdazingzoroark.riftlib.util.MutableAxisAlignedBB;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RiftLibParticle {
    private final World world;
    public final MolangParser molangParser;
    public final MolangScope particleScope;
    public double x, y, z;
    public double prevX, prevY, prevZ;
    public double velX, velY, velZ;
    public double rotation;
    public double velRotation;
    public double accelRotation;
    public float uvXMin, uvYMin, uvXMax, uvYMax;
    public IValue[] size;
    private boolean isDead;
    public boolean useLocalLighting;
    public ParticleCameraMode cameraMode;

    //debug info
    public int emitterId; //this is mostly for debugging
    public int particleId; //this too is for debugging, mainly of individual particles

    //expire/not expire within certain blocks of said strings
    public List<ParticleBlockRule> blocksExpireIfNotIn = new ArrayList<>();
    public List<ParticleBlockRule> blocksExpireIfIn = new ArrayList<>();
    private final BlockPos.MutableBlockPos tempPos = new BlockPos.MutableBlockPos();

    //other collision related stuff
    private final MutableAxisAlignedBB tempAABB = new MutableAxisAlignedBB();
    public IValue collisionEnabled = MolangParser.ZERO;
    public float collisionDrag;
    public float coeffOfRestitution;
    public Float collisionRadius;
    public boolean expireOnContact;

    //speed
    public IValue initialSpeed = MolangParser.ZERO;
    public IValue[] linearAcceleration = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};
    public IValue linearDragCoefficient = MolangParser.ZERO;

    //rotation
    public IValue initialRotation = MolangParser.ZERO;
    public IValue rotationRate = MolangParser.ZERO;
    public IValue rotationAcceleration = MolangParser.ZERO;
    public IValue rotationDragCoefficient = MolangParser.ZERO;

    //flipbook data
    public boolean flipbook;
    public boolean flipbookStretchToLifetime;
    public boolean flipbookLoop;
    public int textureWidth, textureHeight;
    public IValue[] particleUV;
    public IValue[] particleUVSize;
    //only matters if the particle is flipbook mode
    public IValue[] particleUVStepSize;

    //parsed flipbook data
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

    //IValue lifetime info
    public IValue lifetimeExpression;
    public IValue expirationExpression;

    //runtime data, are parsed molang variables
    public int lifetime, age; //REMEMBER THAT THESE ARE IN TICKS
    public float randomOne, randomTwo, randomThree, randomFour;

    //for color
    public IValue[] colorArray = new IValue[]{MolangParser.ONE, MolangParser.ONE, MolangParser.ONE};
    public IValue colorAlpha = MolangParser.ONE;

    public RiftLibParticle(World world, MolangParser parser, MolangScope emitterScope) {
        this.world = world;
        this.molangParser = parser;
        this.particleScope = new MolangScope(emitterScope);

        //init molang stuff
        this.setupMolangVariables();
        this.initParticleRandoms();
    }

    //all molang variables are created here
    private void setupMolangVariables() {
        this.molangParser.withScope(this.particleScope, () -> {
            this.varParticleAge = this.molangParser.getVariable("variable.particle_age");
            this.varParticleLifetime = this.molangParser.getVariable("variable.particle_lifetime");
            this.varParticleRandomOne = this.molangParser.getVariable("variable.particle_random_1");
            this.varParticleRandomTwo = this.molangParser.getVariable("variable.particle_random_2");
            this.varParticleRandomThree = this.molangParser.getVariable("variable.particle_random_3");
            this.varParticleRandomFour = this.molangParser.getVariable("variable.particle_random_4");
        });
    }

    private void initParticleRandoms() {
        this.randomOne = (float) Math.random();
        this.randomTwo = (float) Math.random();
        this.randomThree = (float) Math.random();
        this.randomFour = (float) Math.random();
    }

    public void initializeVelocity(Vec3d direction) {
        AtomicReference<Vec3d> toFinalVelocity = new AtomicReference<>(Vec3d.ZERO);
        this.molangParser.withScope(this.particleScope, () -> {
            toFinalVelocity.set(direction.scale(this.initialSpeed.get()));
        });

        Vec3d finalVelocity = toFinalVelocity.get();

        //divide all by 20 to turn them from blocks/second into blocks/tick
        this.velX = finalVelocity.x / 20D;
        this.velY = finalVelocity.y / 20D;
        this.velZ = finalVelocity.z / 20D;
    }

    public void initializeRotation() {
        this.molangParser.withScope(this.particleScope, () -> {
            this.rotation = this.initialRotation.get();
            this.velRotation = this.rotationRate.get() / 20D;
        });
    }

    public void update() {
        this.molangParser.withScope(this.particleScope, () -> {
            //set the lifetime from expression
            this.lifetime = (int) (this.lifetimeExpression.get() * 20);

            //dynamically set molang variables
            if (this.varParticleAge != null) this.varParticleAge.set(this.age / 20D);
            if (this.varParticleLifetime != null) this.varParticleLifetime.set(this.lifetime / 20D);
            if (this.varParticleRandomOne != null) this.varParticleRandomOne.set(this.randomOne);
            if (this.varParticleRandomTwo != null) this.varParticleRandomTwo.set(this.randomTwo);
            if (this.varParticleRandomThree != null) this.varParticleRandomThree.set(this.randomThree);
            if (this.varParticleRandomFour != null) this.varParticleRandomFour.set(this.randomFour);

            //update temp pos
            this.tempPos.setPos(this.x, this.y, this.z);

            //update life based on age and expiration
            if (!this.isDead) {
                if (this.age < this.lifetime) this.age++;
                if (this.age >= this.lifetime
                        || this.expirationExpression.get() != 0
                        || !this.isWithinValidBlock()
                ) this.isDead = true;
            }

            //update flipbook
            if (this.flipbook) this.updateFlipbookUV();

            //-----rotation modification-----
            if (this.velRotation != 0) {
                this.rotation += this.velRotation;

                //clamp between -180 and 180 degrees
                if (this.rotation > 180) this.rotation -= 360;
                if (this.rotation < -180) this.rotation += 360;
            }

            //get rotation acceleration
            //turn degrees per sec^2 into degrees per second^2
            this.accelRotation = this.rotationAcceleration.get() / 400;

            //apply to rotation velocity
            if (this.accelRotation != 0) this.velRotation += this.accelRotation;

            //apply rotational linearDrag
            double rotationalDrag = this.rotationDragCoefficient.get();
            if (rotationalDrag > 0) {
                double factor = Math.max(0, 1 - (rotationalDrag / 20));
                this.velRotation *= factor;
            }

            //-----position modification-----
            //move based on velocity (w collision)
            this.prevX = this.x;
            this.prevY = this.y;
            this.prevZ = this.z;

            double nextX = this.x + this.velX;
            double nextY = this.y + this.velY;
            double nextZ = this.z + this.velZ;

            boolean collided = false;
            if (this.collisionEnabled.get() != 0 && this.collisionRadius != null && this.collisionRadius > 0) {
                collided = this.resolveCollision(nextX, nextY, nextZ);
            }
            else {
                this.x = nextX;
                this.y = nextY;
                this.z = nextZ;
            }

            //expire on contact should be driven by actual collision response
            if (this.expireOnContact && collided) {
                this.isDead = true;
            }

            //change velocity based on acceleration
            //the division by 400 is to convert from blocks/sec^2 to blocks/tick^2
            this.velX += this.linearAcceleration[0].get() / 400D;
            this.velY += this.linearAcceleration[1].get() / 400D;
            this.velZ += this.linearAcceleration[2].get() / 400D;

            //apply linear drag
            double linearDrag = this.linearDragCoefficient.get();
            if (linearDrag > 0) {
                double factor = Math.max(0, 1 - (linearDrag / 20));
                this.velX *= factor;
                this.velY *= factor;
                this.velZ *= factor;
            }
        });
    }

    public void renderParticle(BufferBuilder buffer, Entity cameraEntity, float partialTicks) {
        this.molangParser.withScope(this.particleScope, () -> {
            //sizes
            float scaleX = (float) this.size[0].get();
            float scaleY = (float) this.size[1].get();

            //camera position (lerped)
            double camX = cameraEntity.lastTickPosX + (cameraEntity.posX - cameraEntity.lastTickPosX) * partialTicks;
            double camY = cameraEntity.lastTickPosY + (cameraEntity.posY - cameraEntity.lastTickPosY) * partialTicks;
            double camZ = cameraEntity.lastTickPosZ + (cameraEntity.posZ - cameraEntity.lastTickPosZ) * partialTicks;

            //particle position (lerped) in camera space
            double px = this.prevX + (this.x - this.prevX) * partialTicks;
            double py = this.prevY + (this.y - this.prevY) * partialTicks;
            double pz = this.prevZ + (this.z - this.prevZ) * partialTicks;

            //origin point
            Vec3d pointOrigin = new Vec3d(px - camX, py - camY, pz - camZ);

            //get and emit vector quad
            List<Vec3d> vecQuad = this.cameraMode.getPoints(scaleX, scaleY, partialTicks, this.rotation);
            this.emitQuad(buffer, pointOrigin, vecQuad.getFirst(), vecQuad.get(1), vecQuad.get(2), vecQuad.getLast(), partialTicks);
        });
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
        if (!this.useLocalLighting || this.world == null) return 0xF000F0;

        double x = this.prevX + (this.x - this.prevX) * partialTicks;
        double y = this.prevY + (this.y - this.prevY) * partialTicks;
        double z = this.prevZ + (this.z - this.prevZ) * partialTicks;

        BlockPos blockpos = new BlockPos(x, y, z);
        return this.world.isBlockLoaded(blockpos) ? this.world.getCombinedLight(blockpos, 0) : 0;
    }

    public void updateFlipbookUV() {
        if (!this.flipbook || this.maxFrame <= 0 || this.textureWidth <= 0 || this.textureHeight <= 0) return;

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

        float uvXPixels = this.baseUVX + this.stepUVX * (float) Math.floor(frame);
        float uvYPixels = this.baseUVY + this.stepUVY * (float) Math.floor(frame);

        this.uvXMin = uvXPixels / this.textureWidth;
        this.uvYMin = uvYPixels / this.textureHeight;
        this.uvXMax = (uvXPixels + this.sizeUVX) / this.textureWidth;
        this.uvYMax = (uvYPixels + this.sizeUVY) / this.textureHeight;
    }

    public boolean isDead() {
        return this.isDead;
    }

    private boolean isWithinValidBlock() {
        //if blocksExpireIfNotIn and blocksExpireIfIn are empty, skip
        if (this.blocksExpireIfNotIn.isEmpty() && this.blocksExpireIfIn.isEmpty()) return true;
        IBlockState blockState = this.world.getBlockState(this.tempPos);

        for (ParticleBlockRule blockRule : this.blocksExpireIfNotIn) {
            if (!blockRule.matches(blockState)) return false;
        }
        for (ParticleBlockRule blockRule : this.blocksExpireIfIn) {
            if (blockRule.matches(blockState)) return false;
        }

        return true;
    }

    private boolean resolveCollision(double nextX, double nextY, double nextZ) {
        boolean collided = false;

        //x axis
        double xTry = nextX;
        if (this.isCollided(xTry, this.y, this.z)) {
            collided = true;
            xTry = this.x;

            this.velX = -this.velX * this.coeffOfRestitution;
            this.applyCollisionDrag(false, true, true);
        }
        this.x = xTry;

        //y axis
        double yTry = nextY;
        if (this.isCollided(this.x, yTry, this.z)) {
            collided = true;
            yTry = this.y;

            this.velY = -this.velY * this.coeffOfRestitution;
            this.applyCollisionDrag(true, false, true);
        }
        this.y = yTry;

        //z axis
        double zTry = nextZ;
        if (this.isCollided(this.x, this.y, zTry)) {
            collided = true;
            zTry = this.z;

            this.velZ = -this.velZ * this.coeffOfRestitution;
            this.applyCollisionDrag(true, true, false);
        }
        this.z = zTry;

        return collided;
    }

    private boolean isCollided(double xTest, double yTest, double zTest) {
        if (this.collisionRadius == null || this.collisionEnabled.get() == 0) return false;

        this.tempAABB.set(
                xTest - this.collisionRadius, yTest - this.collisionRadius, zTest - this.collisionRadius,
                xTest + this.collisionRadius, yTest + this.collisionRadius, zTest + this.collisionRadius
        );

        int minX = MathHelper.floor(this.tempAABB.getMinX());
        int minY = MathHelper.floor(this.tempAABB.getMinY());
        int minZ = MathHelper.floor(this.tempAABB.getMinZ());
        int maxX = MathHelper.floor(this.tempAABB.getMaxX());
        int maxY = MathHelper.floor(this.tempAABB.getMaxY());
        int maxZ = MathHelper.floor(this.tempAABB.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    this.tempPos.setPos(x, y, z);
                    if (!this.world.isBlockLoaded(this.tempPos)) continue;

                    IBlockState state = this.world.getBlockState(this.tempPos);
                    if (state.getMaterial().isReplaceable()) continue;

                    AxisAlignedBB blockBox = state.getCollisionBoundingBox(this.world, this.tempPos);
                    if (blockBox == Block.NULL_AABB) continue;

                    if (this.tempAABB.intersects(
                            blockBox.minX + x, blockBox.minY + y, blockBox.minZ + z,
                            blockBox.maxX + x, blockBox.maxY + y, blockBox.maxZ + z
                    )) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyCollisionDrag(boolean dragX, boolean dragY, boolean dragZ) {
        if (this.collisionDrag <= 0f) return;

        double dragPerTick = this.collisionDrag / 20.0;

        if (dragX) this.velX = this.approachZero(this.velX, dragPerTick);
        if (dragY) this.velY = this.approachZero(this.velY, dragPerTick);
        if (dragZ) this.velZ = this.approachZero(this.velZ, dragPerTick);
    }

    private double approachZero(double v, double amount) {
        if (v > 0) return Math.max(0, v - amount);
        if (v < 0) return Math.min(0, v + amount);
        return 0;
    }
}
