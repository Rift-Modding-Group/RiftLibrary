package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedFireworkStickItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FireworkStickModel extends AnimatedGeoModel<AnimatedFireworkStickItem> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(AnimatedFireworkStickItem object) {
        return "geometry.firework_stick";
    }

    @Override
    public String getTextureLocation(AnimatedFireworkStickItem object) {
        return "item/firework_stick.png";
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(AnimatedFireworkStickItem animatable) {
        return List.of("animation.firework_stick.create_sparks");
    }
}
