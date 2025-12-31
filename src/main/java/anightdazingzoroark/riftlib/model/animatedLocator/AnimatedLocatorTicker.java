package anightdazingzoroark.riftlib.model.animatedLocator;

import anightdazingzoroark.riftlib.ClientProxy;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.event.ParticleAttachEvent;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

@SideOnly(Side.CLIENT)
public class AnimatedLocatorTicker {
    public static long RENDER_FRAME_ID = 0L;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        RENDER_FRAME_ID++;
    }

    /***
     * This is for attaching particles to entities and tileentities
     * They do not need any further ticking based on certain attributes because
     * their locators don't need further specialization based on cameras n stuff
     * ***/
    @SubscribeEvent
    public void createParticlesUsingBuilders(ParticleAttachEvent event) {
        if (event.animatable instanceof Entity || event.animatable instanceof TileEntity || event.animatable instanceof Item) {
            ClientProxy.EMITTER_LIST.add(new RiftLibParticleEmitter(
                    event.animatedLocator.getParticleBuilder(),
                    Minecraft.getMinecraft().world,
                    event.animatedLocator
            ));
        }
    }

    /***
     * This is for ticking locators attached to items from the 1st person perspective
     * ***/
    @SubscribeEvent
    public void tickOnItemFirstPerson(RenderSpecificHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Item item = stack.getItem();
        if (!(item instanceof IAnimatable)) return;
        IAnimatable itemAnimatable = (IAnimatable) item;

        //get camera and partial ticks
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) return;
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

        //block when in third person, thats taken care of in tickOnItemThirdPerson
        if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) return;

        //get yaw and pitch rotation of camera
        float yaw = camera.prevRotationYaw + (camera.rotationYaw - camera.prevRotationYaw) * partialTicks;
        float pitch = camera.prevRotationPitch + (camera.rotationPitch - camera.prevRotationPitch) * partialTicks;

        //base forward position for held item
        Vec3d look = camera.getLook(partialTicks);
        Vec3d right = look.crossProduct(new Vec3d(0, 1, 0)).normalize();
        Vec3d up = right.crossProduct(look).normalize();

        //finally le hand base
        Vec3d handBase = getFirstPersonHandOffsetForItem(event.getHand(), stack, camera, partialTicks, look, right, up);

        for (IAnimatedLocator animatedLocator : itemAnimatable.getFactory().getAnimatedLocators()) {
            if (!(animatedLocator instanceof ItemAnimatedLocator)) continue;
            ItemAnimatedLocator itemAnimatedLocator = (ItemAnimatedLocator) animatedLocator;
            Vec3d geoLocatorPos = itemAnimatedLocator.getGeoLocator().getPosition();

            Vec3d worldPos = handBase
                            .add(right.scale(geoLocatorPos.x))
                            .add(up.scale(geoLocatorPos.y))
                            .add(look.scale(-geoLocatorPos.z));
            Vec3d worldRot = new Vec3d(Math.toRadians(pitch), Math.toRadians(yaw), 0);

            itemAnimatedLocator.updateFromRender(worldPos, worldRot);
        }
    }

    //private helper method for getting locator offset based on hand in which item is held in 1st person
    private static Vec3d getFirstPersonHandOffsetForItem(EnumHand hand, ItemStack itemStack, Entity camera, float partialTicks, Vec3d look, Vec3d right, Vec3d up) {
        Vec3d toReturn = Vec3d.ZERO;

        if (hand == null) return toReturn;

        Vec3d eye = camera.getPositionEyes(partialTicks);

        //rough offsets for item hand position
        double rightOffset = 0.6 * (hand == EnumHand.MAIN_HAND ? 1 : -1);
        double upOffset = -0.65;
        double forwardOffset = 0.45;

        //change based on item hand position
        Vec3d handForward = look.normalize();
        Vec3d handRight = handForward.crossProduct(new Vec3d(0, 1, 0)).normalize();
        Vec3d handUp = handRight.crossProduct(handForward).normalize();
        toReturn = eye
                .add(handRight.scale(rightOffset))
                .add(handUp.scale(upOffset))
                .add(handForward.scale(forwardOffset));

        //change based on details in item model
        ItemCameraTransforms.TransformType type = hand == EnumHand.MAIN_HAND ?
                ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
        IBakedModel baked = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(itemStack, Minecraft.getMinecraft().world, Minecraft.getMinecraft().player);
        ItemTransformVec3f itemTransform = baked.getItemCameraTransforms().getTransform(type);
        Vec3d scaledItemTransform = new Vec3d(
                itemTransform.translation.x * itemTransform.scale.x,
                itemTransform.translation.y * itemTransform.scale.y,
                itemTransform.translation.z * itemTransform.scale.z
        );
        Vec3d modelOffset = right.scale(scaledItemTransform.x)
                .add(up.scale(scaledItemTransform.y))
                .add(look.scale(-scaledItemTransform.z));
        toReturn = toReturn.add(modelOffset);

        return toReturn;
    }

    /***
     * This is for ticking locators attached to items in third person and the perspective of all other players
     * ***/
    @SubscribeEvent
    public void tickOnItemThirdPerson(RenderPlayerEvent.Post event) {
        //get camera and partial ticks
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) return;
        EntityPlayer renderedPlayer = event.getEntityPlayer();
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

        //block when in first person, thats taken care of in tickOnItemFirstPerson
        if (renderedPlayer == camera && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) return;

        //set offset for item on each hand
        thirdPersonSetHandOffsetForItem(EnumHand.MAIN_HAND, renderedPlayer, event.getRenderer().getMainModel(), partialTicks);
        thirdPersonSetHandOffsetForItem(EnumHand.OFF_HAND, renderedPlayer, event.getRenderer().getMainModel(), partialTicks);
    }

    private static void thirdPersonSetHandOffsetForItem(EnumHand hand, EntityPlayer player, ModelPlayer model, float partialTicks) {
        if (hand == null) return;

        //get held item stack
        ItemStack itemStack = player.getHeldItem(hand);
        if (itemStack.isEmpty()) return;

        //get held item and animatable
        Item item = itemStack.getItem();
        if (!(item instanceof IAnimatable)) return;
        IAnimatable itemAnimatable = (IAnimatable) item;

        //select arm based on hand
        ModelRenderer arm = (hand == EnumHand.MAIN_HAND) ? model.bipedRightArm : model.bipedLeftArm;

        //base world position (interpolated player position)
        Vec3d base = new Vec3d(
                player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks,
                player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks,
                player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks
        );

        //torso yaw rotation and clamping
        double bodyYaw = Math.toRadians(player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks);
        if (bodyYaw >= 2 * Math.PI) bodyYaw -= 2 * Math.PI;
        if (bodyYaw < 0) bodyYaw += 2 * Math.PI;

        //arm pivot location
        Vec3d armPivot = new Vec3d(arm.rotationPointX, arm.rotationPointY, arm.rotationPointZ).scale(0.0625);

        //rough ideal offsets for item hand position
        // are ideally perpendicular to the players arm
        double rightOffset = 0.375 * (hand == EnumHand.MAIN_HAND ? 1 : -1);
        double upOffset = 0.9375;
        double forwardOffset = 0.0625;

        //create held item pivot from above offsets and change to be perpendicular to the arm
        Vec3d heldItemPivot = new Vec3d(rightOffset, upOffset, -forwardOffset);

        //create hand pivot with above information
        Vec3d handVec = armPivot.subtract(heldItemPivot);

        //create offset from hand based on details in item model
        ItemCameraTransforms.TransformType type = hand == EnumHand.MAIN_HAND
                ? ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        IBakedModel baked = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(itemStack, Minecraft.getMinecraft().world, Minecraft.getMinecraft().player);
        ItemTransformVec3f itemTransform = baked.getItemCameraTransforms().getTransform(type);
        Vec3d scaledItemTransform = new Vec3d(
                itemTransform.translation.x * itemTransform.scale.x,
                itemTransform.translation.y * itemTransform.scale.y,
                itemTransform.translation.z * itemTransform.scale.z
        ).rotatePitch(-(float) Math.PI / 2f);;

        //iterate over each locator
        for (IAnimatedLocator animatedLocator : itemAnimatable.getFactory().getAnimatedLocators()) {
            if (!(animatedLocator instanceof ItemAnimatedLocator)) continue;
            ItemAnimatedLocator itemAnimatedLocator = (ItemAnimatedLocator) animatedLocator;

            //get locator position
            Vec3d geoLocatorPos = itemAnimatedLocator.getGeoLocator().getPosition().rotatePitch(-(float) Math.PI / 2f);

            //create final rotated vector
            Vec3d handBase = VectorUtils.rotateVector(
                    handVec.add(scaledItemTransform).add(geoLocatorPos),
                    arm.rotateAngleX, arm.rotateAngleY - bodyYaw, arm.rotateAngleZ
            );

            Vec3d worldPos = base.add(handBase);
            Vec3d worldRot = new Vec3d(arm.rotateAngleX - Math.PI / 2, arm.rotateAngleY - bodyYaw, arm.rotateAngleZ);
            itemAnimatedLocator.updateFromRender(worldPos, worldRot);
        }
    }
}
