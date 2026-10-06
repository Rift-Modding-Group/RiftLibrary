package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangObject;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.RiftLibEmitterLifetimeComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.RiftLibEmitterRateComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.RiftLibEmitterShapeComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;

@SideOnly(Side.CLIENT)
public class RiftLibParticleEmitter implements MolangObject {
    @NotNull
    private final List<RiftLibParticle> particles = new ArrayList<>();
    @NotNull
    private final String particleIdentifier;
    @Nullable
    private final AnimatedLocator locator;
    @Nullable
    private final World world;
    private final int emitterId; //this is mostly for debugging
    @NotNull
    private final ResourceLocation textureLocation;
    @NotNull
    private final ParticleMaterial material;
    @NotNull
    private final MolangParser molangParser;
    @NotNull
    private final RandomGenerator random = RandomGenerator.getDefault();
    @NotNull
    private final MolangScope emitterScope = new MolangScope(null, this);
    @NotNull
    private final List<Map.Entry<String, RawParticleComponent>> rawParticleComponents;
    @NotNull
    private List<MolangExpression> initialOperations = List.of();
    @NotNull
    private List<MolangExpression> repeatingOperations = List.of();
    @NotNull
    private Quaternion rotationQuaternion = new Quaternion();
    @Nullable
    private RiftLibEmitterShapeComponent emitterShape;
    @Nullable
    private RiftLibEmitterRateComponent emitterRate;
    @Nullable
    private RiftLibEmitterLifetimeComponent emitterLifetime;
    @Nullable
    private String stateParticleStateName;
    private double particleCount;
    private double posX;
    private double posY;
    private double posZ;
    private boolean hasExpired;
    private int particleId;
    private int stateParticleControllerId = -1;
    private int stateParticleIndex = -1;
    private int age;
    private int lifetime;

    public RiftLibParticleEmitter(@NotNull ParticleBuilder particleBuilder, @Nullable World world, @NotNull AnimatedLocator locator, @NotNull String... variables) {
        this(particleBuilder, world, 0, 0, 0, locator, variables);
    }

    public RiftLibParticleEmitter(
            @NotNull ParticleBuilder particleBuilder, @Nullable World world,
            double x, double y, double z, double rotationX, double rotationY, @NotNull String... variables
    ) {
        this(particleBuilder, world, x, y, z, variables);
        this.rotationQuaternion = QuaternionUtils.createXYZQuaternion(rotationX, rotationY, 0);
    }

    public RiftLibParticleEmitter(@NotNull ParticleBuilder particleBuilder, @Nullable World world, double x, double y, double z, @NotNull String... variables) {
        this(particleBuilder, world, x, y, z, null, variables);
    }

    private RiftLibParticleEmitter(
            @NotNull ParticleBuilder particleBuilder, @Nullable World world,
            double x, double y, double z, @Nullable AnimatedLocator locator,
            @NotNull String... variables
    ) {
        this.textureLocation = Objects.requireNonNull(particleBuilder.texture, "Particle texture cannot be null");
        this.particleIdentifier = Objects.requireNonNull(particleBuilder.identifier, "Particle identifier cannot be null");
        this.material = Objects.requireNonNull(particleBuilder.material, "Particle material cannot be null");
        this.molangParser = Objects.requireNonNull(particleBuilder.molangParser, "Particle Molang parser cannot be null");
        this.rawParticleComponents = List.copyOf(particleBuilder.rawParticleComponents);
        this.emitterId = ParticleTicker.EMITTER_ID++;
        this.world = world;
        this.locator = locator;
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        for (RiftLibEmitterComponent component : particleBuilder.emitterComponents) {
            component.applyComponent(this);
        }

        //deal with custom variables
        if (variables.length % 2 != 0) {
            throw new IllegalArgumentException("Emitter variables must be supplied as name-expression pairs");
        }

        List<ImmutablePair<String, MolangExpression>> parsedVariables = new ArrayList<>(variables.length / 2);
        for (int index = 0; index < variables.length; index += 2) {
            String variableName = Objects.requireNonNull(variables[index], "Emitter variable name cannot be null");
            String variableExpression = Objects.requireNonNull(variables[index + 1], "Emitter variable expression cannot be null");
            try {
                parsedVariables.add(new ImmutablePair<>(variableName, this.molangParser.parseExpression(variableExpression)));
            }
            catch (MolangException exception) {
                throw new IllegalArgumentException("Could not parse expression for emitter variable '" + variableName + "'", exception);
            }
        }

        //start molang stuff
        this.molangParser.withScope(this.emitterScope, () -> {
            this.molangParser.setVariable("variable.emitter_age", 0D);
            this.molangParser.setVariable("variable.emitter_lifetime", 0D);
            this.molangParser.setVariable("variable.emitter_random_1", this.random.nextDouble());
            this.molangParser.setVariable("variable.emitter_random_2", this.random.nextDouble());
            this.molangParser.setVariable("variable.emitter_random_3", this.random.nextDouble());
            this.molangParser.setVariable("variable.emitter_random_4", this.random.nextDouble());

            for (ImmutablePair<String, MolangExpression> variable : parsedVariables) {
                this.molangParser.setVariable(variable.getKey(), variable.getValue().get());
            }

            for (MolangExpression expression : this.initialOperations) expression.get();
        });
    }

    public void update() {
        if (this.isDead()) return;

        RiftLibEmitterLifetimeComponent lifetimeComponent = this.emitterLifetime;
        RiftLibEmitterRateComponent rateComponent = this.emitterRate;
        if (lifetimeComponent == null) {
            throw new IllegalStateException("No emitter lifetime component has been parsed!");
        }
        if (rateComponent == null) {
            throw new IllegalStateException("No emitter rate component has been parsed!");
        }

        this.molangParser.withScope(this.emitterScope, () -> {
            this.molangParser.setVariable("variable.emitter_age", this.age / 20D);
            this.molangParser.setVariable("variable.emitter_lifetime", this.lifetime / 20D);
            for (MolangExpression expression : this.repeatingOperations) expression.get();
        });

        this.age++;

        if (lifetimeComponent.canExpire(this) && this.particles.isEmpty()) this.killEmitter();

        if (this.locator != null && !this.locator.isValid()) this.killEmitter();

        if (this.locator != null) {
            Vec3d locatorPos = this.locator.getWorldSpacePosition();
            this.posX = locatorPos.x;
            this.posY = locatorPos.y;
            this.posZ = locatorPos.z;

            this.rotationQuaternion = this.locator.getWorldSpaceYXZQuaternion();
        }

        if (lifetimeComponent.canCreateParticles(this) && !this.hasExpired
                && (this.locator == null || this.locator.isUpdated())) {
            rateComponent.createParticles(this);
        }

        Iterator<RiftLibParticle> it = this.particles.iterator();
        while (it.hasNext()) {
            RiftLibParticle particle = it.next();
            particle.update();
            if (particle.isDead()) it.remove();
        }
    }

    @NotNull
    public RiftLibParticle createParticle() {
        RiftLibParticle particle = new RiftLibParticle(this.world, this.molangParser, this.emitterScope);
        particle.setDebugIds(this.emitterId, this.particleId++);

        for (Map.Entry<String, RawParticleComponent> rawParticleComponent : this.rawParticleComponents) {
            try {
                RiftLibParticleComponent component = RiftLibParticleComponentRegistry.createParticleComponent(rawParticleComponent.getKey());
                if (component != null) {
                    component.parseRawComponent(rawParticleComponent, this.molangParser);
                    component.applyComponent(particle);
                }
            }
            catch (Exception exception) {
                RiftLib.LOGGER.error("Could not apply particle component '{}' to '{}'.",
                        rawParticleComponent.getKey(), this.particleIdentifier, exception);
            }
        }

        RiftLibEmitterShapeComponent shapeComponent = this.emitterShape;
        if (shapeComponent == null) throw new IllegalStateException("No emitter shape component has been parsed!");

        Vec3d[] shapeVectors = Objects.requireNonNull(this.molangParser.withScope(this.emitterScope, () -> {
            Vec3d offset = shapeComponent.defineParticleOffset(this);
            Vec3d direction = shapeComponent.defineDirection(
                    this,
                    this.posX + offset.x,
                    this.posY + offset.y,
                    this.posZ + offset.z
            );
            return new Vec3d[]{
                    VectorUtils.rotateVectorWithQuaternion(offset, this.rotationQuaternion),
                    VectorUtils.rotateVectorWithQuaternion(direction, this.rotationQuaternion).normalize()
            };
        }));

        Vec3d offset = shapeVectors[0];
        particle.setPosition(this.posX + offset.x, this.posY + offset.y, this.posZ + offset.z);
        particle.initializeVelocity(shapeVectors[1]);
        particle.initializeRotation();
        return particle;
    }

    public void render(float partialTicks) {
        if (this.world == null || this.particles.isEmpty()) return;

        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) return;

        Minecraft.getMinecraft().getTextureManager().bindTexture(this.textureLocation);
        this.material.beginDraw();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        for (RiftLibParticle particle : this.particles) {
            particle.renderParticle(buffer, camera, partialTicks);
        }

        tess.draw();
        this.material.endDraw();
    }

    public void killEmitter() {
        this.hasExpired = true;
    }

    public boolean isDead() {
        return this.hasExpired && this.particles.isEmpty();
    }

    public void setStateParticleOwner(int controllerId, @NotNull String stateName, int particleIndex) {
        this.stateParticleControllerId = controllerId;
        this.stateParticleStateName = stateName;
        this.stateParticleIndex = particleIndex;
    }

    public boolean isStateParticleEmitter(int controllerId, @NotNull String stateName, int particleIndex) {
        return this.stateParticleControllerId == controllerId
                && (particleIndex < 0 || this.stateParticleIndex == particleIndex)
                && Objects.equals(this.stateParticleStateName, stateName);
    }

    @Nullable
    public AnimatedLocator getLocator() {
        return this.locator;
    }

    @Override
    @NotNull
    public MolangScope getMolangScope() {
        return this.emitterScope;
    }

    @Override
    @Nullable
    public Object getMolangActorOwner() {
        return this.locator == null ? null : this.locator.getAnimationData().getAnimatable();
    }

    public void setEmitterShape(@NotNull RiftLibEmitterShapeComponent emitterShape) {
        this.emitterShape = emitterShape;
    }

    public void setEmitterRate(@NotNull RiftLibEmitterRateComponent emitterRate) {
        this.emitterRate = emitterRate;
    }

    public void setEmitterLifetime(@NotNull RiftLibEmitterLifetimeComponent emitterLifetime) {
        this.emitterLifetime = emitterLifetime;
    }

    public void setInitializationOperations(@NotNull List<MolangExpression> initialOperations, @NotNull List<MolangExpression> repeatingOperations) {
        this.initialOperations = List.copyOf(initialOperations);
        this.repeatingOperations = List.copyOf(repeatingOperations);
    }

    public double nextRandomDouble() {
        return this.random.nextDouble();
    }

    public int nextRandomInt(int bound) {
        return this.random.nextInt(bound);
    }

    public double getParticleCount() {
        return this.particleCount;
    }

    public void setParticleCount(double value) {
        this.particleCount = value;
    }

    public int getAge() {
        return this.age;
    }

    public double getX() {
        return this.posX;
    }

    public double getY() {
        return this.posY;
    }

    public double getZ() {
        return this.posZ;
    }

    public int getParticleAmount() {
        return this.particles.size();
    }

    public void addParticle(@NotNull RiftLibParticle particle) {
        this.particles.add(particle);
    }

    @NotNull
    public List<RiftLibParticle> getParticles() {
        return Collections.unmodifiableList(this.particles);
    }
}
