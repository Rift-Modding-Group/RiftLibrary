package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Variable;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeOnceComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.*;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.RiftLibEmitterRateComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeLoopingComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.RiftLibEmitterLifetimeComponent;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

//emitters are what spawn particles
@SideOnly(Side.CLIENT)
public class RiftLibParticleEmitter {
    private final List<RiftLibParticle> particles = new ArrayList<>();
    public final String particleIdentifier;
    private AnimatedLocator locator;
    private double particleCount;
    private final World world;
    private final int emitterId; //this is mostly for debugging
    private int particleId; //this too is for debugging, mainly of individual particles, increment this after assignment
    private final ResourceLocation textureLocation;
    private final ParticleMaterial material;
    private final MolangParser molangParser;
    public final Random random = new Random();
    public double posX, posY, posZ;
    public Quaternion rotationQuaternion = new Quaternion(); //assumed to use yxz rotation when created from animations and xyz when created from player orientation
    private boolean isDead;
    public RiftLibEmitterShapeComponent emitterShape;
    public RiftLibEmitterRateComponent emitterRate;

    public final MolangScope emitterScope = new MolangScope();

    //unparsed particle components
    public final List<Map.Entry<String, RawParticleComponent>> rawParticleComponents;

    //molang emitter variables
    private Variable varEmitterAge;
    private Variable varEmitterLifetime;
    private Variable varEmitterRandomOne;
    private Variable varEmitterRandomTwo;
    private Variable varEmitterRandomThree;
    private Variable varEmitterRandomFour;

    //additional molang operations and variables
    private final List<Variable> additionalVariables = new ArrayList<>();
    public List<MolangExpression> initialOperations = new ArrayList<>();
    public List<MolangExpression> repeatingOperations = new ArrayList<>();

    //runtime data, are parsed molang variables
    private int age, lifetime;
    private float emitterRandomOne, emitterRandomTwo, emitterRandomThree, emitterRandomFour;

    //for lifetime stuff
    public RiftLibEmitterLifetimeComponent emitterLifetime;

    public RiftLibParticleEmitter(ParticleBuilder particleBuilder, World world, AnimatedLocator locator) {
        this(particleBuilder, world, 0, 0, 0);
        this.locator = locator;
    }

    public RiftLibParticleEmitter(ParticleBuilder particleBuilder, World world, double x, double y, double z, double rotationX, double rotationY) {
        this(particleBuilder, world, x, y, z);
        //the reason for using xyz here is because as far as i was able to see
        //thats basically the one look related operations use
        this.rotationQuaternion = QuaternionUtils.createXYZQuaternion(rotationX, rotationY, 0);
    }

    public RiftLibParticleEmitter(ParticleBuilder particleBuilder, World world, double x, double y, double z) {
        this.textureLocation = particleBuilder.texture;
        this.particleIdentifier = particleBuilder.identifier;
        this.material = particleBuilder.material;
        this.molangParser = particleBuilder.molangParser;
        this.rawParticleComponents = particleBuilder.rawParticleComponents;
        this.emitterId = ParticleTicker.EMITTER_ID++;
        this.world = world;
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        //init molang stuff
        this.setupMolangVariables();
        this.initEmitterRandoms();

        //apply components from components in the builder
        for (RiftLibEmitterComponent component : particleBuilder.emitterComponents) {
            component.applyComponent(this);
        }

        //execute initial operations
        this.molangParser.withScope(this.emitterScope, () -> {
            for (MolangExpression expression : this.initialOperations) expression.get();
        });
    }

    //all molang variables are created here
    private void setupMolangVariables() {
        this.molangParser.withScope(this.emitterScope, () -> {
            this.varEmitterAge = this.molangParser.getVariable("variable.emitter_age");
            this.varEmitterLifetime = this.molangParser.getVariable("variable.emitter_lifetime");
            this.varEmitterRandomOne = this.molangParser.getVariable("variable.emitter_random_1");
            this.varEmitterRandomTwo = this.molangParser.getVariable("variable.emitter_random_2");
            this.varEmitterRandomThree = this.molangParser.getVariable("variable.emitter_random_3");
            this.varEmitterRandomFour = this.molangParser.getVariable("variable.emitter_random_4");
        });
    }

    private void initEmitterRandoms() {
        this.emitterRandomOne = (float) Math.random();
        this.emitterRandomTwo = (float) Math.random();
        this.emitterRandomThree = (float) Math.random();
        this.emitterRandomFour = (float) Math.random();
    }

    //emitter is updated here, particles r created here too
    public void update() throws MolangException {
        if (this.isDead()/* || !this.locatorIsUpdated()*/) return;

        this.molangParser.withScope(this.emitterScope, () -> {
            //dynamically set molang variables
            if (this.varEmitterAge != null) this.varEmitterAge.set(this.age / 20D);
            if (this.varEmitterLifetime != null) this.varEmitterLifetime.set(this.lifetime / 20D);
            if (this.varEmitterRandomOne != null) this.varEmitterRandomOne.set(this.emitterRandomOne);
            if (this.varEmitterRandomTwo != null) this.varEmitterRandomTwo.set(this.emitterRandomTwo);
            if (this.varEmitterRandomThree != null) this.varEmitterRandomThree.set(this.emitterRandomThree);
            if (this.varEmitterRandomFour != null) this.varEmitterRandomFour.set(this.emitterRandomFour);

            //apply repeating operations
            for (MolangExpression expression : this.repeatingOperations) expression.get();
        });

        //update emitter age
        this.age++;

        //set death based on expiry and if theres no particles left
        if (this.emitterLifetime != null && this.emitterLifetime.canExpire(this) && this.particles.isEmpty()) {
            this.killEmitter();
        }

        //set death based on if it has an animated locator and if said animatedlocator is dead
        if (this.locator != null && !this.locator.isValid()) this.killEmitter();

        //set position and quaternion based on animated locator
        if (this.locator != null) {
            Vec3d locatorPos = this.locator.getWorldSpacePosition();
            this.posX = locatorPos.x;
            this.posY = locatorPos.y;
            this.posZ = locatorPos.z;

            this.rotationQuaternion = this.locator.getWorldSpaceYXZQuaternion();
        }

        //create particles based on rate and ability to create them
        if (this.emitterLifetime != null && this.emitterLifetime.canCreateParticles(this)
                && this.emitterRate != null
                && !this.isDead && this.locatorIsUpdated()
        ) {
            this.emitterRate.createParticles(this);
        }

        //update existing particles
        Iterator<RiftLibParticle> it = this.particles.iterator();
        while (it.hasNext()) {
            RiftLibParticle particle = it.next();
            particle.update();
            if (particle.isDead()) it.remove();
        }
    }

    public RiftLibParticle createParticle() {
        RiftLibParticle toReturn = new RiftLibParticle(this.world, this.molangParser, this.emitterScope);

        //debug info
        toReturn.emitterId = this.emitterId;
        toReturn.particleId = this.particleId++;

        //parse init particle components, these mostly apply info that apply every tick
        for (Map.Entry<String, RawParticleComponent> rawParticleComponent : this.rawParticleComponents) {
            //init and parse the component
            try {
                RiftLibParticleComponent component = RiftLibParticleComponentRegistry.createParticleComponent(rawParticleComponent.getKey());
                if (component != null) {
                    component.parseRawComponent(rawParticleComponent, toReturn.molangParser);
                    component.applyComponent(toReturn);
                }
            }
            catch (Exception e) {}
        }

        //molang side operations to pass to the particle go here
        AtomicReference<Vec3d> offset = new AtomicReference<>(Vec3d.ZERO);
        AtomicReference<Vec3d> directionFromShape = new AtomicReference<>(Vec3d.ZERO);
        this.molangParser.withScope(this.emitterScope, () -> {
            Vec3d obtainedOffset = this.emitterShape != null ? this.emitterShape.defineParticleOffset(this) : Vec3d.ZERO;
            offset.set(obtainedOffset);
            directionFromShape.set(this.particleDirection(
                    this.posX + obtainedOffset.x,
                    this.posY + obtainedOffset.y,
                    this.posZ + obtainedOffset.z
            ));
        });

        //set particle init position
        //position is only evaluated when the moment a particle is created, it should be ok to define it here
        Vec3d finalOffset = offset.get();
        toReturn.x = toReturn.prevX = this.posX + finalOffset.x;
        toReturn.y = toReturn.prevY = this.posY + finalOffset.y;
        toReturn.z = toReturn.prevZ = this.posZ + finalOffset.z;

        //set particle velocity
        //velocity is only evaluated when the moment a particle is created, it should be ok to define it here
        toReturn.initializeVelocity(directionFromShape.get());

        //init particle rotation
        toReturn.initializeRotation();

        //particle texturing and uv application is only evaluated when the moment a particle is created
        //it should be ok to define it here, though i need to figure out how to make them within scope of
        //the emitterScope
        //flipbook UV behaviour
        if (toReturn.flipbook) {
            //store UV info in pixels
            toReturn.baseUVX = (float) toReturn.particleUV[0].get();
            toReturn.baseUVY = (float) toReturn.particleUV[1].get();
            toReturn.sizeUVX = (float) toReturn.particleUVSize[0].get();
            toReturn.sizeUVY = (float) toReturn.particleUVSize[1].get();
            toReturn.stepUVX = (float) toReturn.particleUVStepSize[0].get();
            toReturn.stepUVY = (float) toReturn.particleUVStepSize[1].get();

            //initialise UVs for frame 0
            toReturn.updateFlipbookUV();
        }
        //static UV behaviour
        else {
            toReturn.uvXMin = (float) (toReturn.particleUV[0].get() / toReturn.textureWidth);
            toReturn.uvYMin = (float) (toReturn.particleUV[1].get() / toReturn.textureHeight);
            toReturn.uvXMax = (float) ((toReturn.particleUV[0].get() + toReturn.particleUVSize[0].get()) / toReturn.textureWidth);
            toReturn.uvYMax = (float) ((toReturn.particleUV[1].get() + toReturn.particleUVSize[1].get()) / toReturn.textureHeight);
        }

        //return value
        return toReturn;
    }

    public void render(float partialTicks) {
        if (this.world == null || this.textureLocation == null || this.particles.isEmpty()) return;

        //get camera
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) return;

        //bind particle texture directly
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.textureLocation);

        //render particle, start by using info from material to change how it renders
        this.material.beginDraw();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        for (RiftLibParticle particle : this.particles) {
            if (particle == null) continue;
            particle.renderParticle(buffer, camera, partialTicks);
        }

        tess.draw();
        this.material.endDraw();
    }

    private Vec3d particleDirection(double emissionX, double emissionY, double emissionZ) {
        //get from shape first
        Vec3d directionFromShape = this.emitterShape != null ? this.emitterShape.defineDirection(this, emissionX, emissionY, emissionZ) : Vec3d.ZERO;

        //rotate using quaternion and return
        return VectorUtils.rotateVectorWithQuaternion(directionFromShape, this.rotationQuaternion).normalize();
    }

    public void killEmitter() {
        this.isDead = true;
    }

    public boolean isDead() {
        return this.isDead && this.particles.isEmpty();
    }

    public AnimatedLocator getLocator() {
        return this.locator;
    }

    private boolean locatorIsUpdated() {
        return this.locator == null || this.locator.isUpdated();
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

    public List<RiftLibParticle> getParticles() {
        return this.particles;
    }
}
