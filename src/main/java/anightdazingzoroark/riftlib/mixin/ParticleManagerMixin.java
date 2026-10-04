package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import anightdazingzoroark.riftlib.block.AnimatedBlockParticle;
import anightdazingzoroark.riftlib.block.AnimatedBlockParticleFace;
import anightdazingzoroark.riftlib.block.AnimatedBlockParticleTexture;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates animated block particles from the texture sections of their rendered model faces.
 * */
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Shadow
    protected World world;

    @Shadow
    public abstract void addEffect(Particle effect);

    @Inject(
            method = "addBlockDestroyEffects(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void riftlib$addAnimatedBlockDestroyEffects(BlockPos pos, IBlockState state, CallbackInfo callback) {
        GeoBlockRenderer<?> renderer = AnimatedBlockRegistry.INSTANCE.getRenderer(state.getBlock());
        if (renderer == null) return;

        ParticleManager thisParticleManager = (ParticleManager) (Object) this;
        if (!state.getBlock().isAir(state, this.world, pos) && !state.getBlock().addDestroyEffects(this.world, pos, thisParticleManager)) {
            IBlockState actualState = state.getActualState(this.world, pos);
            List<AnimatedBlockParticleFace> faces = renderer.createParticleFaces(this.world, pos, actualState);
            int sectionCount = 0;
            for (AnimatedBlockParticleFace face : faces) sectionCount += face.getSectionCount();
            int particleCount = Math.min(64, sectionCount);
            Set<Integer> selectedSections = new LinkedHashSet<>();
            while (selectedSections.size() < particleCount) selectedSections.add(this.world.rand.nextInt(sectionCount));
            for (int selectedSection : selectedSections) {
                AnimatedBlockParticleFace selectedFace = null;
                int faceSection = selectedSection;
                for (AnimatedBlockParticleFace face : faces) {
                    if (faceSection < face.getSectionCount()) {
                        selectedFace = face;
                        break;
                    }
                    faceSection -= face.getSectionCount();
                }
                if (selectedFace == null) continue;
                AnimatedBlockParticleTexture texture = selectedFace.createTexture(faceSection);
                this.addEffect(new AnimatedBlockParticle(
                        this.world,
                        pos.getX() + 0.5D + texture.getModelX(),
                        pos.getY() + 0.01D + texture.getModelY(),
                        pos.getZ() + 0.5D + texture.getModelZ(),
                        texture.getModelX(),
                        texture.getModelY(),
                        texture.getModelZ(),
                        texture,
                        actualState.getBlock().blockParticleGravity,
                        0.7f
                ));
            }
        }
        callback.cancel();
    }

    @Inject(
            method = "addBlockHitEffects(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void riftlib$addAnimatedBlockHitEffects(BlockPos pos, EnumFacing side, CallbackInfo callback) {
        IBlockState state = this.world.getBlockState(pos);
        GeoBlockRenderer<?> renderer = AnimatedBlockRegistry.INSTANCE.getRenderer(state.getBlock());
        if (renderer == null) return;
        IBlockState actualState = state.getActualState(this.world, pos);
        List<AnimatedBlockParticleFace> faces = renderer.createParticleFaces(this.world, pos, actualState);
        if (faces.isEmpty()) {
            callback.cancel();
            return;
        }
        Vec3i sideNormal = side.getDirectionVec();
        List<AnimatedBlockParticleFace> facingFaces = new ArrayList<>();
        for (AnimatedBlockParticleFace face : faces) {
            double alignment = face.getNormalX() * sideNormal.getX()
                    + face.getNormalY() * sideNormal.getY()
                    + face.getNormalZ() * sideNormal.getZ();
            if (alignment > 0.5D) facingFaces.add(face);
        }
        List<AnimatedBlockParticleFace> choices = facingFaces.isEmpty() ? faces : facingFaces;
        int sectionCount = 0;
        for (AnimatedBlockParticleFace face : choices) sectionCount += face.getSectionCount();
        int faceSection = this.world.rand.nextInt(sectionCount);
        AnimatedBlockParticleFace selectedFace = choices.getFirst();
        for (AnimatedBlockParticleFace face : choices) {
            if (faceSection < face.getSectionCount()) {
                selectedFace = face;
                break;
            }
            faceSection -= face.getSectionCount();
        }
        AnimatedBlockParticleTexture texture = selectedFace.createTexture(faceSection);
        this.addEffect(new AnimatedBlockParticle(
                this.world,
                pos.getX() + 0.5D + texture.getModelX(),
                pos.getY() + 0.01D + texture.getModelY(),
                pos.getZ() + 0.5D + texture.getModelZ(),
                0D, 0D, 0D,
                texture, actualState.getBlock().blockParticleGravity, 0.6f
        ).multiplyVelocity(0.2f));
        callback.cancel();
    }
}
