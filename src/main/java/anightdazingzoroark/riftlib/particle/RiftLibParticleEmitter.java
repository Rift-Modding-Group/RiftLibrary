package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.ClientProxy;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.molang.expressions.MolangAssignment;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Variable;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.RiftLibEmitterRateComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeCustomComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.RiftLibEmitterShapeComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeLoopingComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.RiftLibEmitterLifetimeComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;

//emitters are what spawn particles
@SideOnly(Side.CLIENT)
public class RiftLibParticleEmitter {
    private final List<RiftLibParticle> particles = new ArrayList<>();
    private double particleCount;
    private final World world;
    private final int emitterId; //this is mostly for debugging
    private int particleId; //this too is for debugging, mainly of individual particles, increment this after assignment
    private final ResourceLocation textureLocation;
    private final ParticleMaterial material;
    private final MolangParser molangParser;
    private final Random random = new Random();
    private final double x, y, z;
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
    private boolean initRan = false;

    //runtime data, are parsed molang variables
    private int age, lifetime;
    private float emitterRandomOne, emitterRandomTwo, emitterRandomThree, emitterRandomFour;

    //for lifetime stuff
    public RiftLibEmitterLifetimeComponent emitterLifetime;

    public RiftLibParticleEmitter(ParticleBuilder particleBuilder, World world, double x, double y, double z) {
        this.textureLocation = particleBuilder.texture;
        this.material = particleBuilder.material;
        this.molangParser = particleBuilder.molangParser;
        this.rawParticleComponents = particleBuilder.rawParticleComponents;
        this.emitterId = ClientProxy.EMITTER_ID++;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;

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
        this.initRan = true;
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
        if (this.isDead) return;

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
        this.isDead = this.canExpire() && this.particles.isEmpty();

        //create particles based on rate and ability to create them
        if (this.canCreateParticles()) {
            if (this.emitterRate instanceof EmitterInstantComponent) {
                EmitterInstantComponent emitterInstant = (EmitterInstantComponent) this.emitterRate;
                double particleCount = emitterInstant.particleCount.get();
                while (this.particleCount < particleCount) {
                    this.particles.add(this.createParticle());
                    this.particleCount++;
                }
            }
            else if (this.emitterRate instanceof EmitterSteadyComponent) {
                EmitterSteadyComponent emitterSteady = (EmitterSteadyComponent) this.emitterRate;
                int maxParticleCount = (int) emitterSteady.maxParticleCount.get();
                //turn particles per second into particles per tick
                double particleRate = emitterSteady.spawnRate.get() / 20D;

                this.particleCount += particleRate;
                while (this.particleCount >= 1 && this.particles.size() < maxParticleCount) {
                    this.particles.add(this.createParticle());
                    this.particleCount -= 1;
                }

                if (this.particles.size() >= maxParticleCount) this.particleCount = Math.min(this.particleCount, 1.0);
            }
        }

        //update existing particles
        Iterator<RiftLibParticle> it = this.particles.iterator();
        while (it.hasNext()) {
            RiftLibParticle particle = it.next();
            particle.update();
            if (particle.isDead()) it.remove();
        }
    }

    private RiftLibParticle createParticle() throws MolangException {
        RiftLibParticle toReturn = new RiftLibParticle(this.world, this.molangParser, this.emitterScope);

        //debug info
        toReturn.emitterId = this.emitterId;
        toReturn.particleId = this.particleId++;

        //parse init particle components, these mostly apply info that apply every tick
        for (Map.Entry<String, RawParticleComponent> rawParticleComponent : this.rawParticleComponents) {
            //init and parse the component
            RiftLibParticleComponent component = RiftLibParticleComponentRegistry.createParticleComponent(rawParticleComponent.getKey());
            if (component != null) {
                component.parseRawComponent(rawParticleComponent, toReturn.molangParser);
                component.applyComponent(toReturn);
            }
        }

        //molang side operations to pass to the particle go here
        double[] offset = new double[3];
        double[] velFromShape = new double[3];
        this.molangParser.withScope(this.emitterScope, () -> {
            double[] o = this.findParticleOffset();
            offset[0] = o[0];
            offset[1] = o[1];
            offset[2] = o[2];

            double[] v = this.particleVelocityFromShape(new double[]{this.x + offset[0], this.y + offset[1], this.z + offset[2]});
            velFromShape[0] = v[0];
            velFromShape[1] = v[1];
            velFromShape[2] = v[2];
        });

        //set particle init position
        //position is only evaluated when the moment a particle is created, it should be ok to define it here
        toReturn.x = this.x + offset[0];
        toReturn.y = this.y + offset[1];
        toReturn.z = this.z + offset[2];
        toReturn.prevX = toReturn.x;
        toReturn.prevY = toReturn.y;
        toReturn.prevZ = toReturn.z;

        //set particle velocity
        //velocity is only evaluated when the moment a particle is created, it should be ok to define it here
        toReturn.initializeVelocity(velFromShape);

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

        return toReturn;
    }

    public void render(float partialTicks) {
        if (this.world == null || this.textureLocation == null || this.particles.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        Entity camera = mc.getRenderViewEntity();
        if (camera == null) return;

        //bind particle texture directly
        mc.getTextureManager().bindTexture(this.textureLocation);

        //render particle, start by using info from material to change how it renders
        this.beginMaterialDraw();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        for (RiftLibParticle particle : this.particles) {
            if (particle == null) continue;
            particle.renderParticle(buffer, camera, partialTicks);
        }

        tess.draw();
        this.finishMaterialDraw();
    }

    //this creates a position based on the emitter shape and provided offset
    private double[] findParticleOffset() {
        if (this.emitterShape instanceof EmitterShapeSphereComponent) {
            EmitterShapeSphereComponent sphereEmitterShape = (EmitterShapeSphereComponent) this.emitterShape;
            //sphere formula is x² + y² + z² = r²,
            //this offset creator uses the radius to generate the x, y, and z positions of particles to create
            double radius = sphereEmitterShape.surfaceOnly ? sphereEmitterShape.radius.get() : (2 * this.random.nextDouble() - 1) * sphereEmitterShape.radius.get();
            double offsetY = (2 * this.random.nextDouble() - 1) * radius;
            double radiusAtY = Math.sqrt(radius * radius - offsetY * offsetY);
            double theta = 2 * Math.PI * this.random.nextDouble();
            double offsetX = radiusAtY * Math.cos(theta);
            double offsetZ = radiusAtY * Math.sin(theta);
            return new double[]{
                    offsetX + sphereEmitterShape.offset[0].get(),
                    offsetY + sphereEmitterShape.offset[1].get(),
                    offsetZ + sphereEmitterShape.offset[2].get()
            };
        }
        else if (this.emitterShape instanceof EmitterShapePointComponent) {
            EmitterShapePointComponent customEmitterShape = (EmitterShapePointComponent) this.emitterShape;
            return new double[]{
                    customEmitterShape.offset[0].get(),
                    customEmitterShape.offset[1].get(),
                    customEmitterShape.offset[2].get()
            };
        }
        else if (this.emitterShape instanceof EmitterShapeCustomComponent) {
            EmitterShapeCustomComponent customEmitterShape = (EmitterShapeCustomComponent) this.emitterShape;
            return new double[]{
                    customEmitterShape.offset[0].get(),
                    customEmitterShape.offset[1].get(),
                    customEmitterShape.offset[2].get()
            };
        }
        else return new double[]{0, 0, 0};
    }

    //this creates the velocity to go to based on the emitter shape and initial pos
    //for now the velocity will be 1
    private double[] particleVelocityFromShape(double[] particleEmissionPos) {
        if (this.emitterShape instanceof EmitterShapeSphereComponent) {
            EmitterShapeSphereComponent sphereEmitterShape = (EmitterShapeSphereComponent) this.emitterShape;

            //if it has custom particle direction, just return it instead
            if (sphereEmitterShape.customParticleDirection != null) {
                return new double[]{
                        sphereEmitterShape.customParticleDirection[0].get(),
                        sphereEmitterShape.customParticleDirection[1].get(),
                        sphereEmitterShape.customParticleDirection[2].get()
                };
            }
            else {
                //generally the direction to go towards should be the same as its xyz offset from the center of sphere emitter
                int pointer = sphereEmitterShape.particleDirection.equals("outwards") ? 1 : sphereEmitterShape.particleDirection.equals("inwards") ? -1 : 0;

                double xDirection = pointer * (particleEmissionPos[0] - this.x);
                double yDirection = pointer * (particleEmissionPos[1] - this.y);
                double zDirection = pointer * (particleEmissionPos[2] - this.z);

                //normalize
                double magnitude = Math.sqrt(xDirection * xDirection + yDirection * yDirection + zDirection * zDirection);
                double xDirectionNormalized = xDirection / magnitude;
                double yDirectionNormalized = yDirection / magnitude;
                double zDirectionNormalized = zDirection / magnitude;

                return new double[]{xDirectionNormalized, yDirectionNormalized, zDirectionNormalized};
            }
        }
        else if (this.emitterShape instanceof EmitterShapePointComponent) {
            EmitterShapePointComponent customEmitterShape = (EmitterShapePointComponent) this.emitterShape;
            return new double[]{
                    customEmitterShape.particleDirection[0].get(),
                    customEmitterShape.particleDirection[1].get(),
                    customEmitterShape.particleDirection[2].get()
            };
        }
        else if (this.emitterShape instanceof EmitterShapeCustomComponent) {
            EmitterShapeCustomComponent customEmitterShape = (EmitterShapeCustomComponent) this.emitterShape;
            return new double[]{
                    customEmitterShape.customParticleDirection[0].get(),
                    customEmitterShape.customParticleDirection[1].get(),
                    customEmitterShape.customParticleDirection[2].get()
            };
        }
        return new double[]{0, 0, 0};
    }

    private void beginMaterialDraw() {
        if (this.material == ParticleMaterial.ALPHA) {
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.99f);
            GlStateManager.disableCull();
        }
        else if (this.material == ParticleMaterial.ADD) {
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE
            );
            GlStateManager.depthMask(false);
        }
        else if (this.material == ParticleMaterial.BLEND) {
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569f);
            GlStateManager.depthMask(true);
        }
        else if (this.material == ParticleMaterial.OPAQUE) {
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0f);
            GlStateManager.disableCull();
            GlStateManager.depthMask(true);
        }
    }

    private void finishMaterialDraw() {
        //reset default global state
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();

        //restore default alpha test and blend func
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
    }

    //this utilizes the emitter lifetime component to return whether or not particles can be made
    private boolean canCreateParticles() {
        if (this.emitterLifetime instanceof EmitterLifetimeExpressionComponent) {
            EmitterLifetimeExpressionComponent lifetimeExpression = (EmitterLifetimeExpressionComponent) this.emitterLifetime;
            IValue canMakeParticles = lifetimeExpression.emitterActivationValue;
            return canMakeParticles.get() != 0;
        }
        else if (this.emitterLifetime instanceof EmitterLifetimeLoopingComponent) {
            EmitterLifetimeLoopingComponent lifetimeLooping = (EmitterLifetimeLoopingComponent) this.emitterLifetime;
            IValue activeTime = lifetimeLooping.emitterActiveTime;
            double activeTimeValue = activeTime.get();
            IValue sleepTime = lifetimeLooping.emitterSleepTime;
            double sleepTimeValue = sleepTime.get();

            double totalTimeValue = activeTimeValue + sleepTimeValue;
            double sleepTimePercent = activeTimeValue / totalTimeValue;
            double currentTimePercent = (this.age % totalTimeValue) / totalTimeValue;

            return currentTimePercent <= sleepTimePercent;
        }
        return false;
    }

    //this utilizes the emitter lifetime component to return whether or not the emitter can
    //continue existing
    private boolean canExpire() {
        if (this.emitterLifetime instanceof EmitterLifetimeExpressionComponent) {
            EmitterLifetimeExpressionComponent lifetimeExpression = (EmitterLifetimeExpressionComponent) this.emitterLifetime;
            IValue canExpire = lifetimeExpression.emitterExpirationValue;
            return canExpire.get() != 0;
        }
        return false;
    }

    public boolean isDead() {
        return this.isDead;
    }
}
