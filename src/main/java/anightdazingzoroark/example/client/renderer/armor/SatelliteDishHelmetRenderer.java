package anightdazingzoroark.example.client.renderer.armor;

import anightdazingzoroark.example.client.model.armor.SatelliteDishHelmetModel;
import anightdazingzoroark.example.armor.AnimatedSatelliteDishHelmetHolder;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;

public class SatelliteDishHelmetRenderer extends GeoArmorRenderer<AnimatedSatelliteDishHelmetHolder> {
    public SatelliteDishHelmetRenderer() {
        super(new SatelliteDishHelmetModel(), AnimatedSatelliteDishHelmetHolder::new);
        this.bindBoneToEquipmentSlot(EntityEquipmentSlot.HEAD, "head");
        this.bindBoneToBodyPart(ArmorBodyPart.HEAD, "head");
    }
}
