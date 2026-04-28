package anightdazingzoroark.example.armor;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import anightdazingzoroark.riftlib.armor.RiftLibArmor;
import net.minecraft.inventory.EntityEquipmentSlot;

public class SatelliteDishHelmet extends RiftLibArmor {
    public SatelliteDishHelmet(ArmorMaterial materialIn, int renderIndexIn) {
        super(materialIn, renderIndexIn, EntityEquipmentSlot.HEAD);
        this.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
    }

    @Override
    public void registerAnimationControllers(AnimationDataArmor data) {
        data.addAnimationController(new AnimationController<>(
                this, "spin", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.satellite_dish_helmet.spin", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
        ));
        data.addAnimationController(new AnimationController<>(
                this, "emit", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.satellite_dish_helmet.signal", LoopType.PLAY_ONCE));
                    return PlayState.CONTINUE;
                }
        ));
    }
}
