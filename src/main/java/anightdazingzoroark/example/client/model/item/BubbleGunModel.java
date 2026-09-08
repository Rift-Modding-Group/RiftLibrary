package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedBubbleGunItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class BubbleGunModel extends AnimatedGeoModel<AnimatedBubbleGunItem> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AnimatedBubbleGunItem object) {
        return "geometry.bubble_gun";
    }

    @Override
    @NotNull
    public String getTextureLocation(AnimatedBubbleGunItem object) {
        return "item/bubble_gun.png";
    }
}
