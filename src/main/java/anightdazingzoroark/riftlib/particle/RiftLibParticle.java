package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangObject;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.util.MutableAxisAlignedBB;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class RiftLibParticle implements MolangObject {
    @Nullable
    private final World world;
    @Nullable
    private final RiftLibParticleEmitter emitter;
    @NotNull
    private final MolangParser molangParser;
    @NotNull
    private final MolangScope particleScope;
    @NotNull
    private final BlockPos.MutableBlockPos tempPos = new BlockPos.MutableBlockPos();
    @NotNull
    private final MutableAxisAlignedBB tempAABB = new MutableAxisAlignedBB();
    @NotNull
    private List<ParticleBlockRule> blocksExpireIfNotIn = List.of();
    @NotNull
    private List<ParticleBlockRule> blocksExpireIfIn = List.of();
    @NotNull
    private IValue collisionEnabled = MolangParser.ZERO;
    @NotNull
    private IValue initialSpeed = MolangParser.ZERO;
    @NotNull
    private IValue[] linearAcceleration = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};
    @NotNull
    private IValue linearDragCoefficient = MolangParser.ZERO;
    @NotNull
    private IValue initialRotation = MolangParser.ZERO;
    @NotNull
    private IValue rotationRate = MolangParser.ZERO;
    @NotNull
    private IValue rotationAcceleration = MolangParser.ZERO;
    @NotNull
    private IValue rotationDragCoefficient = MolangParser.ZERO;
    @Nullable
    private AppearanceBillboardComponent particleAppearance;
    @Nullable
    private IValue lifetimeExpression;
    @Nullable
    private IValue expirationExpression;
    @NotNull
    private IValue[] colorArray = new IValue[]{MolangParser.ONE, MolangParser.ONE, MolangParser.ONE};
    @NotNull
    private IValue colorAlpha = MolangParser.ONE;
    @Nullable
    private Float collisionRadius;
    private double x;
    private double y;
    private double z;
    private double prevX;
    private double prevY;
    private double prevZ;
    private double velX;
    private double velY;
    private double velZ;
    private double rotation;
    private double velRotation;
    private float collisionDrag;
    private float coeffOfRestitution;
    private boolean isDead;
    private boolean useLocalLighting;
    private boolean expireOnContact;
    private int emitterId;
    private int particleId;
    private int lifetime;
    private int age;

    public RiftLibParticle(@Nullable World world, @NotNull MolangParser parser, @NotNull MolangScope emitterScope) {
        this.world = world;
        this.emitter = emitterScope.getOwner() instanceof RiftLibParticleEmitter particleEmitter ? particleEmitter : null;
        this.molangParser = parser;
        this.particleScope = new MolangScope(emitterScope, this);
        this.molangParser.withScope(this.particleScope, () -> {
            this.molangParser.setVariable("variable.particle_age", 0);
            this.molangParser.setVariable("variable.particle_lifetime", 0);
            this.molangParser.setVariable("variable.particle_random_1", ThreadLocalRandom.current().nextDouble());
            this.molangParser.setVariable("variable.particle_random_2", ThreadLocalRandom.current().nextDouble());
            this.molangParser.setVariable("variable.particle_random_3", ThreadLocalRandom.current().nextDouble());
            this.molangParser.setVariable("variable.particle_random_4", ThreadLocalRandom.current().nextDouble());
        });
    }

    @Override
    @NotNull
    public MolangScope getMolangScope() {
        return this.particleScope;
    }

    @Override
    @Nullable
    public Object getMolangActorOwner() {
        return this.emitter;
    }

    public void initializeVelocity(@NotNull Vec3d direction) {
        Vec3d finalVelocity = Objects.requireNonNull(
                this.molangParser.withScope(this.particleScope, () -> direction.scale(this.initialSpeed.get()))
        );

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
        IValue lifetimeValue = this.lifetimeExpression;
        IValue expirationValue = this.expirationExpression;
        AppearanceBillboardComponent appearance = this.particleAppearance;
        if (lifetimeValue == null) {
            throw new IllegalStateException("No minecraft:particle_lifetime_expression component has been parsed!");
        }
        if (expirationValue == null) {
            throw new IllegalStateException("No particle expiration expression has been parsed!");
        }
        if (appearance == null) {
            throw new IllegalStateException("No minecraft:particle_appearance_billboard component has been parsed!");
        }

        this.molangParser.withScope(this.particleScope, () -> {
            this.lifetime = (int) (lifetimeValue.get() * 20D);
            this.tempPos.setPos(this.x, this.y, this.z);

            if (!this.isDead) {
                if (this.age < this.lifetime) this.age++;
                if (this.age >= this.lifetime
                        || expirationValue.get() != 0D
                        || !this.isWithinValidBlock()
                ) this.isDead = true;
            }

            this.molangParser.setVariable("variable.particle_age", this.age / 20D);
            this.molangParser.setVariable("variable.particle_lifetime", this.lifetime / 20D);
            appearance.updateAppearance(this);

            if (this.velRotation != 0D) {
                this.rotation += this.velRotation;
                if (this.rotation > 180D) this.rotation -= 360D;
                if (this.rotation < -180D) this.rotation += 360D;
            }

            double rotationAcceleration = this.rotationAcceleration.get() / 400D;
            if (rotationAcceleration != 0D) this.velRotation += rotationAcceleration;

            double rotationalDrag = this.rotationDragCoefficient.get();
            if (rotationalDrag > 0D) {
                double factor = Math.max(0D, 1D - rotationalDrag / 20D);
                this.velRotation *= factor;
            }

            this.prevX = this.x;
            this.prevY = this.y;
            this.prevZ = this.z;

            double nextX = this.x + this.velX;
            double nextY = this.y + this.velY;
            double nextZ = this.z + this.velZ;

            boolean collided = false;
            if (this.collisionEnabled.get() != 0D && this.collisionRadius != null && this.collisionRadius > 0F) {
                collided = this.resolveCollision(nextX, nextY, nextZ);
            }
            else {
                this.x = nextX;
                this.y = nextY;
                this.z = nextZ;
            }

            if (this.expireOnContact && collided) this.isDead = true;

            this.velX += this.linearAcceleration[0].get() / 400D;
            this.velY += this.linearAcceleration[1].get() / 400D;
            this.velZ += this.linearAcceleration[2].get() / 400D;

            double linearDrag = this.linearDragCoefficient.get();
            if (linearDrag > 0D) {
                double factor = Math.max(0D, 1D - linearDrag / 20D);
                this.velX *= factor;
                this.velY *= factor;
                this.velZ *= factor;
            }
        });
    }

    public void renderParticle(@NotNull BufferBuilder buffer, @NotNull Entity cameraEntity, float partialTicks) {
        AppearanceBillboardComponent appearance = this.particleAppearance;
        if (appearance == null) throw new IllegalStateException("No minecraft:particle_appearance_billboard component has been parsed!");

        this.molangParser.withScope(this.particleScope, () -> {
            float scaleX = (float) appearance.getSize()[0];
            float scaleY = (float) appearance.getSize()[1];

            //camera position (lerped)
            double camX = Interpolations.lerp(cameraEntity.lastTickPosX, cameraEntity.posX, partialTicks);
            double camY = Interpolations.lerp(cameraEntity.lastTickPosY, cameraEntity.posY, partialTicks);
            double camZ = Interpolations.lerp(cameraEntity.lastTickPosZ, cameraEntity.posZ, partialTicks);

            //particle position (lerped) in camera space
            double particleX = Interpolations.lerp(this.prevX, this.x, partialTicks);
            double particleY = Interpolations.lerp(this.prevY, this.y, partialTicks);
            double particleZ = Interpolations.lerp(this.prevZ, this.z, partialTicks);

            //origin point
            Vec3d pointOrigin = new Vec3d(particleX - camX, particleY - camY, particleZ - camZ);

            List<Vec3d> vecQuad = appearance.getCameraMode().getPoints(scaleX, scaleY, partialTicks, this.rotation);
            this.emitQuad(appearance, buffer, pointOrigin, vecQuad.getFirst(), vecQuad.get(1), vecQuad.get(2), vecQuad.getLast(), partialTicks);
        });
    }

    private void emitQuad(@NotNull AppearanceBillboardComponent appearance, @NotNull BufferBuilder buffer,
                          @NotNull Vec3d pointOrigin, @NotNull Vec3d pointOne, @NotNull Vec3d pointTwo,
                          @NotNull Vec3d pointThree, @NotNull Vec3d pointFour, float partialTicks) {
        int light = this.getBrightnessForRender(partialTicks);
        int j = (light >> 16) & 0xFFFF;
        int k = light & 0xFFFF;

        //colors
        float red = (float) this.colorArray[0].get();
        float green = (float) this.colorArray[1].get();
        float blue = (float) this.colorArray[2].get();
        float alpha = (float) this.colorAlpha.get();

        float[] uvs = appearance.getUVs();
        float uvXMin = uvs[0];
        float uvYMin = uvs[1];
        float uvXMax = uvs[2];
        float uvYMax = uvs[3];
        if (appearance.getCameraMode() == ParticleCameraMode.ROTATE_XYZ) {
            buffer.pos(pointOrigin.x + pointOne.x, pointOrigin.y + pointOne.y, pointOrigin.z + pointOne.z)
                    .tex(uvXMax, uvYMax)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointTwo.x, pointOrigin.y + pointTwo.y, pointOrigin.z + pointTwo.z)
                    .tex(uvXMax, uvYMin)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointThree.x, pointOrigin.y + pointThree.y, pointOrigin.z + pointThree.z)
                    .tex(uvXMin, uvYMin)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointFour.x, pointOrigin.y + pointFour.y, pointOrigin.z + pointFour.z)
                    .tex(uvXMin, uvYMax)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();
        }
        else {
            buffer.pos(pointOrigin.x + pointOne.x, pointOrigin.y + pointOne.y, pointOrigin.z + pointOne.z)
                    .tex(uvXMax, uvYMin)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointTwo.x, pointOrigin.y + pointTwo.y, pointOrigin.z + pointTwo.z)
                    .tex(uvXMax, uvYMax)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointThree.x, pointOrigin.y + pointThree.y, pointOrigin.z + pointThree.z)
                    .tex(uvXMin, uvYMax)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();

            buffer.pos(pointOrigin.x + pointFour.x, pointOrigin.y + pointFour.y, pointOrigin.z + pointFour.z)
                    .tex(uvXMin, uvYMin)
                    .lightmap(j, k)
                    .color(red, green, blue, alpha)
                    .endVertex();
        }
    }

    private int getBrightnessForRender(float partialTicks) {
        World currentWorld = this.world;
        if (!this.useLocalLighting || currentWorld == null) return 0xF000F0;

        double x = this.prevX + (this.x - this.prevX) * partialTicks;
        double y = this.prevY + (this.y - this.prevY) * partialTicks;
        double z = this.prevZ + (this.z - this.prevZ) * partialTicks;

        BlockPos blockpos = new BlockPos(x, y, z);
        return currentWorld.isBlockLoaded(blockpos) ? currentWorld.getCombinedLight(blockpos, 0) : 0;
    }

    public boolean isDead() {
        return this.isDead;
    }

    private boolean isWithinValidBlock() {
        if (this.blocksExpireIfNotIn.isEmpty() && this.blocksExpireIfIn.isEmpty()) return true;
        World currentWorld = this.world;
        if (currentWorld == null || !currentWorld.isBlockLoaded(this.tempPos)) return true;
        IBlockState blockState = currentWorld.getBlockState(this.tempPos);

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
        World currentWorld = this.world;
        Float radius = this.collisionRadius;
        if (currentWorld == null || radius == null || this.collisionEnabled.get() == 0D) return false;

        this.tempAABB.set(
                xTest - radius, yTest - radius, zTest - radius,
                xTest + radius, yTest + radius, zTest + radius
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
                    if (!currentWorld.isBlockLoaded(this.tempPos)) continue;

                    IBlockState state = currentWorld.getBlockState(this.tempPos);
                    if (state.getMaterial().isReplaceable()) continue;

                    AxisAlignedBB blockBox = state.getCollisionBoundingBox(currentWorld, this.tempPos);
                    if (blockBox == null) continue;

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
        if (this.collisionDrag <= 0F) return;

        double dragPerTick = this.collisionDrag / 20D;

        if (dragX) this.velX = this.approachZero(this.velX, dragPerTick);
        if (dragY) this.velY = this.approachZero(this.velY, dragPerTick);
        if (dragZ) this.velZ = this.approachZero(this.velZ, dragPerTick);
    }

    private double approachZero(double v, double amount) {
        if (v > 0D) return Math.max(0D, v - amount);
        if (v < 0D) return Math.min(0D, v + amount);
        return 0D;
    }

    public void setDebugIds(int emitterId, int particleId) {
        this.emitterId = emitterId;
        this.particleId = particleId;
    }

    public void setPosition(double x, double y, double z) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
    }

    public void setAppearance(@NotNull AppearanceBillboardComponent appearance) {
        this.particleAppearance = appearance;
    }

    public void enableLocalLighting() {
        this.useLocalLighting = true;
    }

    public void setColor(@NotNull IValue[] color, @NotNull IValue alpha) {
        if (color.length != 3) throw new IllegalArgumentException("Particle colors require exactly three RGB expressions!");
        this.colorArray = color.clone();
        this.colorAlpha = alpha;
    }

    public void setInitialSpeed(@NotNull IValue initialSpeed) {
        this.initialSpeed = initialSpeed;
    }

    public void setInitialSpin(@NotNull IValue initialRotation, @NotNull IValue rotationRate) {
        this.initialRotation = initialRotation;
        this.rotationRate = rotationRate;
    }

    public void setLifetimeExpressions(@NotNull IValue lifetimeExpression, @NotNull IValue expirationExpression) {
        this.lifetimeExpression = lifetimeExpression;
        this.expirationExpression = expirationExpression;
    }

    public void setExpireInBlocks(@NotNull List<ParticleBlockRule> rules) {
        this.blocksExpireIfIn = List.copyOf(rules);
    }

    public void setExpireNotInBlocks(@NotNull List<ParticleBlockRule> rules) {
        this.blocksExpireIfNotIn = List.copyOf(rules);
    }

    public void setDynamicMotion(@NotNull IValue[] linearAcceleration, @NotNull IValue linearDragCoefficient,
                                 @NotNull IValue rotationAcceleration, @NotNull IValue rotationDragCoefficient) {
        if (linearAcceleration.length != 3) {
            throw new IllegalArgumentException("Particle acceleration requires exactly three expressions!");
        }
        this.linearAcceleration = linearAcceleration.clone();
        this.linearDragCoefficient = linearDragCoefficient;
        this.rotationAcceleration = rotationAcceleration;
        this.rotationDragCoefficient = rotationDragCoefficient;
    }

    public void setCollision(@NotNull IValue enabled, float collisionDrag, float coefficientOfRestitution,
                             @Nullable Float collisionRadius, boolean expireOnContact) {
        this.collisionEnabled = enabled;
        this.collisionDrag = collisionDrag;
        this.coeffOfRestitution = coefficientOfRestitution;
        this.collisionRadius = collisionRadius;
        this.expireOnContact = expireOnContact;
    }

    public int getAge() {
        return this.age;
    }

    public int getLifetime() {
        return this.lifetime;
    }
}
