package anightdazingzoroark.riftlib.block;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders a textured fragment taken from one section of an animated block model face.
 * */
@SideOnly(Side.CLIENT)
public class AnimatedBlockParticle extends Particle {
    public AnimatedBlockParticle(
            World world, double x, double y, double z,
            double velocityX, double velocityY,double velocityZ,
            AnimatedBlockParticleTexture texture,
            float gravity, float scale
    ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.particleGravity = gravity;
        this.particleScale = texture.getParticleScale() * scale * (0.85f + this.rand.nextFloat() * 0.3f);
        float brightness = 0.7f + this.rand.nextFloat() * 0.2f;
        this.particleRed = brightness;
        this.particleGreen = brightness;
        this.particleBlue = brightness;
        this.setParticleTexture(texture);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
