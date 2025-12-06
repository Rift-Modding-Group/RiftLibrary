package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Variable;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterRate.RiftLibEmitterRateComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.RiftLibEmitterShapeComponent;
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
    private final ResourceLocation textureLocation;
    private final MolangParser molangParser;
    private final Random random = new Random();
    private final double x, y, z;
    private boolean isDead;
    private int particleCount;
    public RiftLibEmitterShapeComponent emitterShape;
    public RiftLibEmitterRateComponent emitterRate;

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
    public int particleTextureWidth, particleTextureHeight;
    public IValue[] particleUV;
    public IValue[] particleUVSize;

    //for lifetime stuff
    public IValue emitterActivation;
    public IValue emitterExpiration;
    public IValue particleExpiration;
    public IValue particleMaxLifetime;

    public RiftLibParticleEmitter(ParticleBuilder particleBuilder, World world, double x, double y, double z) {
        this.textureLocation = particleBuilder.texture;
        this.molangParser = particleBuilder.molangParser;
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
            System.out.println("create new "+name);
            v = new Variable(name, 0.0);
            this.molangParser.register(v);
        }
        else System.out.println(name+" already exists");
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

        //create particles based on rate
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

        //update existing particles
        Iterator<RiftLibParticle> it = this.particles.iterator();
        while (it.hasNext()) {
            RiftLibParticle particle = it.next();
            particle.update(this);
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

        //set particle velocity
        double[] vel = this.particleVelocity(new double[]{toReturn.x, toReturn.y, toReturn.z});
        toReturn.velX = vel[0];
        toReturn.velY = vel[1];
        toReturn.velZ = vel[2];

        //set particle lifetime
        float lifetimeSeconds = (float) this.particleMaxLifetime.get();
        toReturn.lifetime = (int) (lifetimeSeconds * 20);

        //set particle scale
        toReturn.size = this.particleSize;

        //set particle uvs
        float textureWidth = this.particleTextureWidth;
        float textureHeight = this.particleTextureHeight;
        float uvX = (float) this.particleUV[0].get();
        float uvY = (float) this.particleUV[1].get();
        float uvWidth  = (float) this.particleUVSize[0].get();
        float uvHeight  = (float) this.particleUVSize[1].get();
        toReturn.uvXMin = uvX / textureWidth;
        toReturn.uvYMin = uvY / textureHeight;
        toReturn.uvXMax = (uvX + uvWidth) / textureWidth;
        toReturn.uvYMax = (uvY + uvHeight) / textureHeight;

        return toReturn;
    }

    public void render(float partialTicks) {
        if (this.world == null || this.textureLocation == null || this.particles.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        Entity camera = mc.getRenderViewEntity();
        if (camera == null) return;

        //bind particle texture directly
        mc.getTextureManager().bindTexture(this.textureLocation);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        for (RiftLibParticle particle : this.particles) particle.renderParticle(buffer, camera, partialTicks, 1f, 1f, 1f, 1f);

        tess.draw();

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
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
        else return new double[]{0, 0, 0};
    }

    //this creates the velocity to go to based on the emitter shape and initial pos
    //for now the velocity will be 1
    private double[] particleVelocity(double[] particleEmissionPos) {
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
        else return new double[]{0, 0, 0};
    }

    public boolean isDead() {
        return this.isDead;
    }
}
