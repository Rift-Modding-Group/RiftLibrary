package anightdazingzoroark.riftlib.renderers.geo;

import javax.vecmath.*;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.*;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.MatrixUtils;
import anightdazingzoroark.riftlib.util.ParticleUtils;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjglx.util.vector.Quaternion;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.util.MatrixStack;

import java.util.Collection;
import java.util.List;

public interface IGeoRenderer<T> {
	MatrixStack MATRIX_STACK = new MatrixStack();

	default void render(GeoModel model, T animatable, float partialTicks, float red, float green, float blue, float alpha) {
		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();
        this.renderEarly(animatable, partialTicks, red, green, blue, alpha);

        this.renderLate(animatable, partialTicks, red, green, blue, alpha);

		this.renderModel(model, red, green, blue, alpha);

		this.renderAfter(animatable, partialTicks, red, green, blue, alpha);
        this.repositionAnimatedLocators(animatable);

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
	}

	default void renderModel(GeoModel model, float red, float green, float blue, float alpha) {
		if (this.makeTranslucencyGlow()) {
			if (alpha <= 0f) return;

			//save some opengl stuff first
			float previousBrightnessX = OpenGlHelper.lastBrightnessX;
			float previousBrightnessY = OpenGlHelper.lastBrightnessY;
			boolean alphaTestWasEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
			int previousAlphaFunction = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
			float previousAlphaReference = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
			boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
			int previousBlendSourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
			int previousBlendDestinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
			int previousBlendSourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
			int previousBlendDestinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
			boolean lightingWasEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
			boolean depthTestWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
			int previousDepthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
			boolean depthMaskWasEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);

			// Fully opaque texels produce the vertex alpha exactly and belong to the normally lit base pass.
			GlStateManager.enableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.enableDepth();
			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.depthMask(true);
			GlStateManager.alphaFunc(GL11.GL_GEQUAL, alpha);
			this.renderModelPass(model, red, green, blue, alpha);

			// Fill nonzero translucent texels without blending so their alpha acts as emission data instead of opacity.
			GlStateManager.depthFunc(GL11.GL_LESS);
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0f);
			this.renderModelPass(model, red, green, blue, alpha);

			// Add full-bright color only where the translucent base pass wrote depth.
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(
					GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA,
					GlStateManager.DestFactor.ONE,
					GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA,
					GlStateManager.DestFactor.ONE
			);
			GlStateManager.depthMask(false);
			GlStateManager.depthFunc(GL11.GL_EQUAL);
			GlStateManager.alphaFunc(GL11.GL_LESS, alpha);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			this.renderModelPass(model, red, green, blue, alpha);

			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousBrightnessX, previousBrightnessY);
			GlStateManager.alphaFunc(previousAlphaFunction, previousAlphaReference);
			if (alphaTestWasEnabled) GlStateManager.enableAlpha();
			else GlStateManager.disableAlpha();
			if (lightingWasEnabled) GlStateManager.enableLighting();
			else GlStateManager.disableLighting();
			GlStateManager.depthFunc(previousDepthFunction);
			GlStateManager.depthMask(depthMaskWasEnabled);
			if (depthTestWasEnabled) GlStateManager.enableDepth();
			else GlStateManager.disableDepth();
			GlStateManager.tryBlendFuncSeparate(
					previousBlendSourceRgb,
					previousBlendDestinationRgb,
					previousBlendSourceAlpha,
					previousBlendDestinationAlpha
			);
			if (blendWasEnabled) GlStateManager.enableBlend();
			else GlStateManager.disableBlend();
		}
		else this.renderModelPass(model, red, green, blue, alpha);
	}

	private void renderModelPass(GeoModel model, float red, float green, float blue, float alpha) {
		BufferBuilder builder = Tessellator.getInstance().getBuffer();

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		// Render all top level bones
		for (GeoBone group : model.getTopLevelBones()) {
			this.renderRecursively(builder, group, red, green, blue, alpha);
		}

		Tessellator.getInstance().draw();
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

		for (GeoQuad quad : cube.getGeoQuads()) {
			Vector3f normal = new Vector3f(quad.getNormal().getX(), quad.getNormal().getY(), quad.getNormal().getZ());

			MATRIX_STACK.getNormalMatrix().transform(normal);

			/*
			 * Fix shading dark shading for flat cubes + compatibility wish Optifine shaders
			 */
			if ((cube.getSize().y == 0 || cube.getSize().z == 0) && normal.getX() < 0) {
				normal.x *= -1;
			}
			if ((cube.getSize().x == 0 || cube.getSize().z == 0) && normal.getY() < 0) {
				normal.y *= -1;
			}
			if ((cube.getSize().x == 0 || cube.getSize().y == 0) && normal.getZ() < 0) {
				normal.z *= -1;
			}

			for (GeoVertex vertex : quad.geoVertices()) {
				Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1f);

				MATRIX_STACK.getModelMatrix().transform(vector4f);

				builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).tex(vertex.textureU, vertex.textureV)
						.color(red, green, blue, alpha).normal(normal.getX(), normal.getY(), normal.getZ()).endVertex();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	GeoModelProvider getGeoModelProvider();

	default ResourceLocation getTextureLocation(T instance) {
		return new ResourceLocation(this.getGeoModelProvider().getModId(), "textures/"+this.getGeoModelProvider().getTextureLocation(instance));
	}

	default void renderEarly(T animatable, float ticks, float red, float green, float blue, float partialTicks) {}

	default void renderLate(T animatable, float ticks, float red, float green, float blue, float partialTicks) {}

	default void renderAfter(T animatable, float ticks, float red, float green, float blue, float partialTicks) {}

    default void repositionAnimatedLocators(T animatable) {
        if (!(animatable instanceof IAnimatable<?> animatableObject)) return;

        Collection<AnimatedLocator> animatedLocators = animatableObject.getAnimationData().getAnimatedLocators().values();
        for (AnimatedLocator animatedLocator : animatedLocators) {
            //update location based on animatedLocator if there is
            BufferUtils.createFloatBuffer(16);
            Vector3d position = ParticleUtils.getCurrentRenderPos();
            double newPosX = position.x;
            double newPosY = position.y;
            double newPosZ = position.z;

            RenderHelper.disableStandardItemLighting();

            GL11.glPushMatrix();

            Matrix4f curRot = ParticleUtils.getCurrentMatrix();

            ParticleUtils.setInitialWorldPos();

            Matrix4f cur2 = ParticleUtils.getCurrentRotation(curRot, ParticleUtils.getCurrentMatrix());

            //apply rotations
            Quaternion renderRotation = MatrixUtils.extractRotationQuaternion(cur2);
            Quaternion locatorRotation = animatedLocator.getModelSpaceYXZQuaternion();
            Quaternion worldRotation = new Quaternion();
            Quaternion.mul(renderRotation, locatorRotation, worldRotation);
            Quaternion.normalise(worldRotation, worldRotation);

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
            animatedLocator.setWorldSpaceYXZQuaternion(worldRotation);

            //set final world position
            newPosX += full.m03;
            newPosY += full.m13 + 1.6D;
            newPosZ += full.m23;
            animatedLocator.setWorldSpacePosition(newPosX, newPosY, newPosZ);

            MATRIX_STACK.pop();
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();
        }
    }

	default Color getRenderColor(T animatable, float partialTicks) {
		return Color.ofRGBA(255, 255, 255, 255);
	}

	/**
	 * Uses translucent texture pixels as an emissive mask. Pixels with lower
	 * nonzero alpha make a stronger additive contribution; fully opaque pixels retain
	 * normal lighting.
	 */
	default boolean makeTranslucencyGlow() {
		return false;
	}
}
