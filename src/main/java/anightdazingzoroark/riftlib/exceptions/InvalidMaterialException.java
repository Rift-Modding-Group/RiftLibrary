package anightdazingzoroark.riftlib.exceptions;

import net.minecraft.util.ResourceLocation;

@SuppressWarnings("serial")
public class InvalidMaterialException extends RuntimeException {
    public InvalidMaterialException(String material, String message) {
        super(material + ": " + message);
    }
}
