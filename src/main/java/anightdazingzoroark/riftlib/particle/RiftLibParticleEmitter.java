package anightdazingzoroark.riftlib.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

//emitters are what spawn particles
@SideOnly(Side.CLIENT)
public class RiftLibParticleEmitter {
    private final ParticleBuilder particleBuilder;
    private final Random random = new Random();
    private final double x, y, z;
    private boolean isDead;
    private int particleCount;
    private int age;

    //this only matters if the EmitterRate is an instance of InstantEmitterRate
    private int maxParticleCount;

    public RiftLibParticleEmitter(ParticleBuilder particleBuilder, double x, double y, double z) {
        this.particleBuilder = particleBuilder;
        this.x = x;
        this.y = y;
        this.z = z;

        if (this.particleBuilder.emitterRate instanceof ParticleBuilder.InstantEmitterRate) this.maxParticleCount = this.defineMaxParticleCount();
    }

    //emitter is updated here, particles r created here too
    public void update() {
        if (this.isDead) return;
        this.age++;

        for (int i = 0; i < this.maxParticleCount; i++) {
            this.spawnOneParticle();
        }
    }

    private void spawnOneParticle() {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        if (world == null || mc.effectRenderer == null) return;

        // 1) Where it is emitted
        double[] offset = this.findParticleOffset();
        double px = this.x + offset[0];
        double py = this.y + offset[1];
        double pz = this.z + offset[2];

        // 2) Direction
        double[] dir = this.particleVelocity(new double[]{px, py, pz});

        // 3) Speed (if you parsed minecraft:particle_initial_speed into builder)
        double speed = 1;

        double vx = dir[0] * speed;
        double vy = dir[1] * speed;
        double vz = dir[2] * speed;

        // 4) Create particle instance
        RiftLibParticle particle = new RiftLibParticle(
                this.particleBuilder,
                world,
                px, py, pz,
                vx, vy, vz
        );

        mc.effectRenderer.addEffect(particle);
    }

    //this creates a position based on the emitter shape and provided offset
    private double[] findParticleOffset() {
        if (this.particleBuilder.emitterShape instanceof ParticleBuilder.SphereEmitterShape) {
            ParticleBuilder.SphereEmitterShape sphereEmitterShape = (ParticleBuilder.SphereEmitterShape) this.particleBuilder.emitterShape;
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
        if (this.particleBuilder.emitterShape instanceof ParticleBuilder.SphereEmitterShape) {
            ParticleBuilder.SphereEmitterShape sphereEmitterShape = (ParticleBuilder.SphereEmitterShape) this.particleBuilder.emitterShape;

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

    //this only matters if the EmitterRate is an instance of InstantEmitterRate
    private int defineMaxParticleCount() {
        if (this.particleBuilder.emitterRate instanceof ParticleBuilder.InstantEmitterRate) {
            ParticleBuilder.InstantEmitterRate instantEmitterRate = (ParticleBuilder.InstantEmitterRate) this.particleBuilder.emitterRate;
            return (int) instantEmitterRate.particleCount.get();
        }
        return 0;
    }

    public boolean isDead() {
        return this.isDead;
    }
}
