package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.ClientProxy;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Variable;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterRate.RiftLibEmitterRateComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.EmitterShapeCustomComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.RiftLibEmitterShapeComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.lifetime.emiter.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.lifetime.emiter.RiftLibEmitterLifetimeComponent;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

//emitters are what spawn particles
@SideOnly(Side.CLIENT)
public class RiftLibParticleEmitter {
    private final List<RiftLibParticle> particles = new ArrayList<>();
    private final World world;
    private final int emitterId;
    private final ResourceLocation textureLocation;
    private final ParticleMaterial material;
    private final MolangParser molangParser;
    private final Random random = new Random();
    private final double x, y, z;
    private boolean isDead;
    private int particleCount;
    public RiftLibEmitterShapeComponent emitterShape;
    public RiftLibEmitterRateComponent emitterRate;

    //particle motion
    public IValue[] particleInitialSpeed = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};
    public IValue[] particleLinearAcceleration = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};

    //molang emitter variables
    private Variable varEmitterAge;
    private Variable varEmitterLifetime;
    private Variable varEmitterRandomOne;
    private Variable varEmitterRandomTwo;
    private Variable varEmitterRandomThree;
    private Variable varEmitterRandomFour;

    //runtime data, are parsed molang variables
    private int age, lifetime;
    private float emitterRandomOne, emitterRandomTwo, emitterRandomThree, emitterRandomFour;

    //this only matters if the EmitterRate is an instance of InstantEmitterRate
    public IValue maxParticleCount;
    private Integer defMaxParticleCount;

    //particle appearance stuff
    public IValue[] particleSize;
    public ParticleCameraMode cameraMode;
    public int particleTextureWidth, particleTextureHeight;
    public IValue[] particleUV;
    public IValue[] particleUVSize;
    //these only matter if the particle is flipbook mode
    public boolean particleFlipbook;
    public IValue[] particleUVStepSize;
    public float particleFPS;
    public IValue particleMaxFrame;
    public boolean particleFlipbookStretchToLifetime;
    public boolean particleFlipbookLoop;
    public boolean particleUseLocalLighting; //for if the particle must be tinted by local lighting conditions in-game

    //for lifetime stuff
    public RiftLibEmitterLifetimeComponent emitterLifetime;
    public IValue particleExpiration = MolangParser.ZERO;
    public IValue particleMaxLifetime = new Constant(Integer.MAX_VALUE);

    //for color
    public IValue[] colorArray = new IValue[]{MolangParser.ONE, MolangParser.ONE, MolangParser.ONE};
    public IValue colorAlpha = MolangParser.ONE;

    public RiftLibParticleEmitter(ParticleBuilder particleBuilder, World world, double x, double y, double z) {
        this.textureLocation = particleBuilder.texture;
        this.material = particleBuilder.material;
        this.molangParser = particleBuilder.molangParser;
        this.emitterId = ClientProxy.EMITTER_ID++;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;

        //init molang stuff
        this.setupMolangVariables();
        this.initEmitterRandoms();

        //apply components from components in the builder
        for (RiftLibParticleComponent component : particleBuilder.particleComponents) {
            component.applyComponent(this);
        }
    }

    //all molang variables are created here
    private void setupMolangVariables() {
        this.varEmitterAge = this.getOrCreateVar("variable.emitter_age");
        this.varEmitterLifetime = this.getOrCreateVar("variable.emitter_lifetime");
        this.varEmitterRandomOne = this.getOrCreateVar("variable.emitter_random_1");
        this.varEmitterRandomTwo = this.getOrCreateVar("variable.emitter_random_2");
        this.varEmitterRandomThree = this.getOrCreateVar("variable.emitter_random_3");
        this.varEmitterRandomFour = this.getOrCreateVar("variable.emitter_random_4");
    }

    private Variable getOrCreateVar(String name) {
        Variable v = this.molangParser.variables.get(name);
        if (v == null) {
            v = new Variable(name, 0.0);
            this.molangParser.register(v);
        }
        return v;
    }

    private void initEmitterRandoms() {
        this.emitterRandomOne = (float) Math.random();
        this.emitterRandomTwo = (float) Math.random();
        this.emitterRandomThree = (float) Math.random();
        this.emitterRandomFour = (float) Math.random();
    }

    //emitter is updated here, particles r created here too
    public void update() {
        if (this.isDead) return;

        //dynamically set molang variables
        if (this.varEmitterAge != null) this.varEmitterAge.set(this.age / 20D);
        if (this.varEmitterLifetime != null) this.varEmitterLifetime.set(this.lifetime / 20D);
        if (this.varEmitterRandomOne != null) this.varEmitterRandomOne.set(this.emitterRandomOne);
        if (this.varEmitterRandomTwo != null) this.varEmitterRandomTwo.set(this.emitterRandomTwo);
        if (this.varEmitterRandomThree != null) this.varEmitterRandomThree.set(this.emitterRandomThree);
        if (this.varEmitterRandomFour != null) this.varEmitterRandomFour.set(this.emitterRandomFour);

        //update emitter age
        this.age++;

        //set death based on expiry and if theres no particles left
        this.isDead = this.canExpire() && this.particles.isEmpty();

        //create particles based on rate and ability to create them
        if (this.canCreateParticles()) {
            if (this.emitterRate instanceof EmitterInstantComponent) {
                //get max particle count first
                if (this.defMaxParticleCount != null) {
                    if (this.particles.size() < this.defMaxParticleCount) {
                        for (int i = 0; i < this.defMaxParticleCount; i++) {
                            this.particles.add(this.createParticle());
                        }
                    }
                }
                else this.defMaxParticleCount = (int) this.maxParticleCount.get();
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

    private RiftLibParticle createParticle() {
        RiftLibParticle toReturn = new RiftLibParticle(this.world, this.molangParser);

        //set particle init position
        double[] offset = this.findParticleOffset();
        toReturn.x = this.x + offset[0];
        toReturn.y = this.y + offset[1];
        toReturn.z = this.z + offset[2];
        toReturn.prevX = toReturn.x;
        toReturn.prevY = toReturn.y;
        toReturn.prevZ = toReturn.z;

        //set particle camera mode
        toReturn.cameraMode = this.cameraMode;

        //set particle velocity
        double[] velocityFromShape = this.particleVelocityFromShape(new double[]{toReturn.x, toReturn.y, toReturn.z});
        double[] velocity = new double[]{
                this.particleInitialSpeed[0].get(),
                this.particleInitialSpeed[1].get(),
                this.particleInitialSpeed[2].get()
        };
        //divide all by 20 to turn them from blocks per second into blocks per tick
        toReturn.velX = (velocityFromShape[0] + velocity[0]) / 20D;
        toReturn.velY = (velocityFromShape[1] + velocity[1]) / 20D;
        toReturn.velZ = (velocityFromShape[2] + velocity[2]) / 20D;

        //set particle acceleration
        //divide all by 400 to turn them from blocks per second sq into blocks per tick sq
        toReturn.accelX = this.particleLinearAcceleration[0].get() / 400D;
        toReturn.accelY = this.particleLinearAcceleration[1].get() / 400D;
        toReturn.accelZ = this.particleLinearAcceleration[2].get() / 400D;

        //set particle lifetime info
        toReturn.lifetimeExpression = this.particleMaxLifetime;
        toReturn.expirationExpression = this.particleExpiration;

        //set particle scale
        toReturn.size = this.particleSize;

        //set particle colors
        toReturn.colorArray = this.colorArray;
        toReturn.colorAlpha = this.colorAlpha;

        //set particle lighting
        toReturn.particleUseLocalLighting = this.particleUseLocalLighting;

        //flipbook vs static UVs
        toReturn.texWidth  = this.particleTextureWidth;
        toReturn.texHeight = this.particleTextureHeight;

        //flipbook UV behaviour
        if (this.particleFlipbook) {
            toReturn.flipbook = true;
            toReturn.flipbookStretchToLifetime = this.particleFlipbookStretchToLifetime;
            toReturn.flipbookLoop = this.particleFlipbookLoop;
            toReturn.fps = this.particleFPS;
            toReturn.maxFrame = (int) this.particleMaxFrame.get();

            //store UV info in pixels
            toReturn.baseUVX = (float) this.particleUV[0].get();
            toReturn.baseUVY = (float) this.particleUV[1].get();
            toReturn.sizeUVX = (float) this.particleUVSize[0].get();
            toReturn.sizeUVY = (float) this.particleUVSize[1].get();
            toReturn.stepUVX = (float) this.particleUVStepSize[0].get();
            toReturn.stepUVY = (float) this.particleUVStepSize[1].get();

            //initialise UVs for frame 0
            toReturn.updateFlipbookUV();
        }
        //static UV behaviour
        else {
            float texW = this.particleTextureWidth;
            float texH = this.particleTextureHeight;
            float uvX = (float) this.particleUV[0].get();
            float uvY = (float) this.particleUV[1].get();
            float uvW = (float) this.particleUVSize[0].get();
            float uvH = (float) this.particleUVSize[1].get();

            toReturn.uvXMin = uvX / texW;
            toReturn.uvYMin = uvY / texH;
            toReturn.uvXMax = (uvX + uvW) / texW;
            toReturn.uvYMax = (uvY + uvH) / texH;
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
        else if (this.emitterShape instanceof EmitterShapeCustomComponent) {
            EmitterShapeCustomComponent customEmitterShape = (EmitterShapeCustomComponent) this.emitterShape;
            return new double[]{
                    customEmitterShape.customParticleDirection[0].get(),
                    customEmitterShape.customParticleDirection[1].get(),
                    customEmitterShape.customParticleDirection[2].get()
            };
        }
        else return new double[]{0, 0, 0};
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
