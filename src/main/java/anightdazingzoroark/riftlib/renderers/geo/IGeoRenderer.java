package anightdazingzoroark.riftlib.renderers.geo;

import javax.vecmath.*;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.*;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.util.MatrixUtils;
import anightdazingzoroark.riftlib.util.ParticleUtils;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.util.MatrixStack;

import java.util.List;

public interface IGeoRenderer<T> {
	MatrixStack MATRIX_STACK = new MatrixStack();

	default void render(GeoModel model, T animatable, float partialTicks, boolean renderParticles, float red, float green, float blue, float alpha) {
		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();
        this.renderEarly(animatable, partialTicks, red, green, blue, alpha);

        this.renderLate(animatable, partialTicks, red, green, blue, alpha);

		BufferBuilder builder = Tessellator.getInstance().getBuffer();

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		// Render all top level bones
		for (GeoBone group : model.topLevelBones) {
			this.renderRecursively(builder, group, red, green, blue, alpha);
		}

		Tessellator.getInstance().draw();

		this.renderAfter(animatable, partialTicks, red, green, blue, alpha);
        if (renderParticles) this.renderAttachedParticles(animatable);

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
	}

	default void renderRecursively(BufferBuilder builder, GeoBone bone, float red, float green, float blue, float alpha) {
		MATRIX_STACK.push();

		MATRIX_STACK.translate(bone);
		MATRIX_STACK.moveToPivot(bone);
		MATRIX_STACK.rotate(bone);
		MATRIX_STACK.scale(bone);
		MATRIX_STACK.moveBackFromPivot(bone);

		if (!bone.isHidden()) {
			for (GeoCube cube : bone.childCubes) {
				MATRIX_STACK.push();
				GlStateManager.pushMatrix();
                this.renderCube(builder, cube, red, green, blue, alpha);
				GlStateManager.popMatrix();
				MATRIX_STACK.pop();
			}
		}
		if (!bone.childBonesAreHiddenToo()) {
			for (GeoBone childBone : bone.childBones) {
				renderRecursively(builder, childBone, red, green, blue, alpha);
			}
		}

		MATRIX_STACK.pop();
	}

	default void renderCube(BufferBuilder builder, GeoCube cube, float red, float green, float blue, float alpha) {
		MATRIX_STACK.moveToPivot(cube);
		MATRIX_STACK.rotate(cube);
		MATRIX_STACK.moveBackFromPivot(cube);

		for (GeoQuad quad : cube.quads) {
			Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());

			MATRIX_STACK.getNormalMatrix().transform(normal);

			/*
			 * Fix shading dark shading for flat cubes + compatibility wish Optifine shaders
			 */
			if ((cube.size.y == 0 || cube.size.z == 0) && normal.getX() < 0) {
				normal.x *= -1;
			}
			if ((cube.size.x == 0 || cube.size.z == 0) && normal.getY() < 0) {
				normal.y *= -1;
			}
			if ((cube.size.x == 0 || cube.size.y == 0) && normal.getZ() < 0) {
				normal.z *= -1;
			}

			for (GeoVertex vertex : quad.vertices) {
				Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(),
						1.0F);

				MATRIX_STACK.getModelMatrix().transform(vector4f);

				builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).tex(vertex.textureU, vertex.textureV)
						.color(red, green, blue, alpha).normal(normal.getX(), normal.getY(), normal.getZ()).endVertex();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	GeoModelProvider getGeoModelProvider();

	ResourceLocation getTextureLocation(T instance);

	default void renderEarly(T animatable, float ticks, float red, float green, float blue, float partialTicks) {}

	default void renderLate(T animatable, float ticks, float red, float green, float blue, float partialTicks) {}

	default void renderAfter(T animatable, float ticks, float red, float green, float blue, float partialTicks) {}

    default void renderAttachedParticles(T animatable) {
        if (!(animatable instanceof IAnimatable)) return;
        IAnimatable animatableObject = (IAnimatable) animatable;
        Integer uniqueID = this.getUniqueID(animatable);

        List<AnimatedLocator> animatedLocators = animatableObject.getFactory().getOrCreateAnimationData(uniqueID).getAnimatedLocators();
        for (AnimatedLocator animatedLocator : animatedLocators) {
            if (animatedLocator.getParticleEmitter() == null) continue;

            RiftLibParticleEmitter emitter = animatedLocator.getParticleEmitter();

            //update location based on animatedLocator if there is
            BufferUtils.createFloatBuffer(16);
            Vector3d position = ParticleUtils.getCurrentRenderPos();
            emitter.posX = position.x;
            emitter.posY = position.y;
            emitter.posZ = position.z;

            RenderHelper.disableStandardItemLighting();

            GL11.glPushMatrix();

            Matrix4f curRot = ParticleUtils.getCurrentMatrix();

            ParticleUtils.setInitialWorldPos();

            Matrix4f cur2 = ParticleUtils.getCurrentRotation(curRot, ParticleUtils.getCurrentMatrix());

            MATRIX_STACK.push();
            MATRIX_STACK.getModelMatrix().mul(new Matrix4f(
                    cur2.m00, cur2.m01, cur2.m02,0,
                    cur2.m10, cur2.m11, cur2.m12,0,
                    cur2.m20, cur2.m21,cur2.m22,0,
                    0,0,0,1
            ));

            //push locator info to matrix
            MATRIX_STACK.translate(animatedLocator);
            MATRIX_STACK.rotate(animatedLocator);

            Matrix4f full = MATRIX_STACK.getModelMatrix();

            //set final rotations
            emitter.rotationQuaternion = MatrixUtils.matrixToQuaternion(
                    new Matrix3f(
                        full.m00, full.m01, full.m02,
                        full.m10, full.m11, full.m12,
                        full.m20, full.m21, full.m22
                    )
            );

            //set final world position
            emitter.posX += full.m03;
            emitter.posY += full.m13 + 1.6D;
            emitter.posZ += full.m23;

            System.out.println("locator id: "+uniqueID);
            System.out.println("final posX: "+emitter.posX);
            System.out.println("final posY: "+emitter.posY);
            System.out.println("final posZ: "+emitter.posZ);

            MATRIX_STACK.pop();
            //the finalized repositioned locator is ticked in ParticleTicker.onRenderWorldLast
            //this is commented out
            //emitter.render(partialTicks);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();
        }
    }

	default Color getRenderColor(T animatable, float partialTicks) {
		return Color.ofRGBA(255, 255, 255, 255);
	}

	default Integer getUniqueID(T animatable) {
		return animatable.hashCode();
	}
}
