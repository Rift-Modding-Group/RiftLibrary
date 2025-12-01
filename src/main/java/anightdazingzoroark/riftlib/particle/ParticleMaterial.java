package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.exceptions.InvalidMaterialException;

public enum ParticleMaterial {
    ADD, //Enables color blending and transparency in colored pixels, uses an additive blend mode
    ALPHA, //Pixels with an alpha of 0 will be fully transparent, colored pixels will always be opaque
    BLEND; //Enables color blending and transparency in colored pixels, uses a normal blend mode

    public static ParticleMaterial getMaterialFromString(String value) {
        switch (value) {
            case "particles_add":
                return ADD;
            case "particles_alpha":
                return ALPHA;
            case "particles_blend":
                return BLEND;
            default:
                throw new InvalidMaterialException(value, "Invalid particle material");
        }
    }
}
