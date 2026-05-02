package anightdazingzoroark.example.armor;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerNew;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import anightdazingzoroark.riftlib.armor.RiftLibArmor;
import net.minecraft.inventory.EntityEquipmentSlot;

import java.util.List;

public class SatelliteDishHelmet extends RiftLibArmor {
    public SatelliteDishHelmet(ArmorMaterial materialIn, int renderIndexIn) {
        super(materialIn, renderIndexIn, EntityEquipmentSlot.HEAD);
        this.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
    }

    @Override
    public List<AnimationControllerNew<?, AnimationDataArmor>> createAnimationControllers() {
        return List.of(
                new AnimationControllerNew<SatelliteDishHelmet, AnimationDataArmor>(
                        this, "satelliteDish", "default",
                        new AnimationControllerState<AnimationDataArmor>("default")
                                .addAnimation("animation.satellite_dish_helmet.spin")
                                .addAnimation("animation.satellite_dish_helmet.signal")
                )
        );
    }
}
