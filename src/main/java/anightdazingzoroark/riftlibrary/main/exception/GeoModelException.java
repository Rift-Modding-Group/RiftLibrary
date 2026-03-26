package anightdazingzoroark.riftlibrary.main.exception;

import net.minecraft.util.ResourceLocation;

public class GeoModelException extends RuntimeException {
    public GeoModelException(ResourceLocation fileLocation, String message) {
        super(fileLocation + ": " + message);
    }
}
