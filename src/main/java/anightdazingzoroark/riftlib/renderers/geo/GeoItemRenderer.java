package anightdazingzoroark.riftlib.renderers.geo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import anightdazingzoroark.riftlib.animation.ItemAnimationTicker;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.util.MatrixUtils;
import anightdazingzoroark.riftlib.util.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class GeoItemRenderer<T extends Item & IAnimatable> extends TileEntityItemStackRenderer
		implements IGeoRenderer<T> {
	// Register a model fetcher for this renderer
	static {
		AnimationController.addModelFetcher((IAnimatable object) -> {
			if (object instanceof Item) {
				Item item = (Item) object;
				TileEntityItemStackRenderer renderer = item.getTileEntityItemStackRenderer();
				if (renderer instanceof GeoItemRenderer) {
					return (IAnimatableModel<Object>) ((GeoItemRenderer<?>) renderer).getGeoModelProvider();
				}
			}
			return null;
		});
	}

	protected AnimatedGeoModel<T> modelProvider;
	private ItemStack currentItemStack;
    private ItemCameraTransforms.TransformType transformType;

	public GeoItemRenderer(AnimatedGeoModel<T> modelProvider) {
		this.modelProvider = modelProvider;
	}

	public void setModel(AnimatedGeoModel<T> model) {
		this.modelProvider = model;
	}

	@Override
	public AnimatedGeoModel<T> getGeoModelProvider() {
		return this.modelProvider;
	}

    public void setLastTransformType(ItemCameraTransforms.TransformType transformType) {
        this.transformType = transformType;
    }

	@Override
	public void renderByItem(ItemStack itemStack, float partialTicks) {
		this.render((T) itemStack.getItem(), itemStack);
	}

	public void render(T animatable, ItemStack itemStack) {
		this.currentItemStack = itemStack;
		GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(animatable));
        Integer uniqueID = this.getUniqueID(animatable);
		AnimationEvent itemEvent = new AnimationEvent(animatable, 0, 0,
				Minecraft.getMinecraft().getRenderPartialTicks(), false, Collections.singletonList(itemStack));
		this.modelProvider.setLivingAnimations(animatable, uniqueID, itemEvent);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.01f, 0);
		GlStateManager.translate(0.5, 0.5, 0.5);

		Minecraft.getMinecraft().renderEngine.bindTexture(this.getTextureLocation(animatable));
		Color renderColor = getRenderColor(animatable, 0f);
        this.render(model, animatable, 0f,
                (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
				(float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
		GlStateManager.popMatrix();

        ItemAnimationTicker.refreshRenderedStackEntry(itemStack, this.getUniqueID(animatable), this.transformType);
	}

	@Override
	public ResourceLocation getTextureLocation(T instance) {
		return this.modelProvider.getTextureLocation(instance);
	}

	@Override
	public Integer getUniqueID(T animatable) {
		return Objects.hash(
                this.currentItemStack.getItem(),
                this.currentItemStack.getCount(),
                this.currentItemStack.hasTagCompound() ? this.currentItemStack.getTagCompound().toString() : 1,
                this.currentItemStack.hashCode()
        );
	}

    public ItemStack getCurrentItemStack() {
        return this.currentItemStack;
    }

    @Override
    public void renderAttachedParticles(T animatable) {
        if (this.transformType == null) return;
        Integer uniqueID = this.getUniqueID(animatable);

        List<AnimatedLocator> animatedLocators = animatable.getFactory().getOrCreateAnimationData(uniqueID).getAnimatedLocators();
        for (AnimatedLocator animatedLocator : animatedLocators) {
            if (animatedLocator.getParticleEmitter() == null) continue;

            RiftLibParticleEmitter emitter = animatedLocator.getParticleEmitter();

            //differentiate based on transform type for repositioning locator
            //note that for now its blocked within all guis, including in hotbar
            //might add it back, idk how much effort would be needed to do so but
            //i'd imagine it would be a lot
            if (this.transformType != ItemCameraTransforms.TransformType.GUI) {
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

                MATRIX_STACK.pop();
                //the finalized repositioned locator is ticked in ParticleTicker.onRenderWorldLast
                //this is commented out
                //emitter.render(partialTicks);
                RenderHelper.enableStandardItemLighting();
                GL11.glPopMatrix();
            }
        }
    }
}
