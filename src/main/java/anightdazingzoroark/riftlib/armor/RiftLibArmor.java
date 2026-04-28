package anightdazingzoroark.riftlib.armor;

import javax.annotation.Nullable;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class RiftLibArmor extends ItemArmor implements IAnimatable<AnimationDataArmor> {
	private final AnimationDataArmor animationData = new AnimationDataArmor(this);

	public RiftLibArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot slot) {
		super(materialIn, renderIndexIn, slot);
	}

	@Nullable
	@SideOnly(Side.CLIENT)
	@Override
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
			ModelBiped defaultArmor) {
		GeoArmorRenderer<?> renderer = GeoArmorRenderer.getRenderer(this.getClass());
		if (renderer == null) return defaultArmor;

		this.animationData.setRenderContext(entityLiving, itemStack, armorSlot);

		renderer.setCurrentItem(entityLiving, itemStack, armorSlot);
		renderer.applyEntityStats(defaultArmor).applySlot(armorSlot);
		return renderer;
	}

	@Nullable
	@SideOnly(Side.CLIENT)
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		GeoArmorRenderer<?> renderer = GeoArmorRenderer.getRenderer(this.getClass());
		return renderer == null ? null : renderer.getArmorTexture(stack).toString();
	}

	@Override
	public AnimationDataArmor getAnimationData() {
		return this.animationData;
	}

	@Override
	public abstract void registerAnimationControllers(AnimationDataArmor data);
}
