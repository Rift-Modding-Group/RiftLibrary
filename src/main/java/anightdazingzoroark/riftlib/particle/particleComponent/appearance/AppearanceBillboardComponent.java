package anightdazingzoroark.riftlib.particle.particleComponent.appearance;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class AppearanceBillboardComponent extends RiftLibParticleComponent {
    public IValue[] size;
    public int textureWidth, textureHeight;
    public IValue[] uv;
    public IValue[] uvSize;
    //these only matter if the particle is flipbook mode
    public boolean particleFlipbook;
    public IValue[] particleUVStepSize;
    public float particleFPS;
    public IValue particleMaxFrame;
    public boolean particleFlipbookStretchToLifetime;
    public boolean particleFlipbookLoop;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        System.out.println("parse");

        //specifies the x/y size of the billboard
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("size")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("size");
            this.size = this.parseExpressionArray(parser, 2, componentValue);
        }
        //get UV information
        if (rawComponent.getValue().componentValues.containsKey("uv")) {
            Map<String, RawParticleComponent.ComponentValue> uvComponents = rawComponent.getValue().componentValues.get("uv").object;

            //uv components
            for (Map.Entry<String, RawParticleComponent.ComponentValue> uvComponent : uvComponents.entrySet()) {
                if (uvComponent.getKey().equals("texture_width")) {
                    this.textureWidth = (int) uvComponent.getValue().number.floatValue();
                }
                if (uvComponent.getKey().equals("texture_height")) {
                    this.textureHeight = (int) uvComponent.getValue().number.floatValue();
                }

                //for flipbook particles
                RawParticleComponent.ComponentValue flipbookVal = uvComponents.get("flipbook");
                if (flipbookVal != null && flipbookVal.object != null) {
                    System.out.println("has flipbook");
                    this.particleFlipbook = true;

                    Map<String, RawParticleComponent.ComponentValue> flip = flipbookVal.object;

                    if (flip.containsKey("base_UV")) {
                        this.uv = this.parseExpressionArray(parser, 2, flip.get("base_UV"));
                    }
                    if (flip.containsKey("size_UV")) {
                        this.uvSize = this.parseExpressionArray(parser, 2, flip.get("size_UV"));
                    }
                    if (flip.containsKey("step_UV")) {
                        this.particleUVStepSize = this.parseExpressionArray(parser, 2, flip.get("step_UV"), true);
                    }
                    if (flip.containsKey("frames_per_second")) {
                        this.particleFPS = flip.get("frames_per_second").number;
                    }
                    if (flip.containsKey("max_frame")) {
                        this.particleMaxFrame = this.parseExpression(parser, flip.get("max_frame"));
                    }
                    if (flip.containsKey("stretch_to_lifetime")) {
                        this.particleFlipbookStretchToLifetime = flip.get("stretch_to_lifetime").bool;
                    }
                    if (flip.containsKey("loop")) {
                        this.particleFlipbookLoop = flip.get("loop").bool;
                    }
                }
                //for static particles
                else {
                    System.out.println("has no flipbook");
                    // NON-flipbook mode: UV / UV_SIZE at top level
                    this.particleFlipbook = false;

                    if (uvComponents.containsKey("uv")) {
                        this.uv = this.parseExpressionArray(parser, 2, uvComponents.get("uv"));
                    }
                    if (uvComponents.containsKey("uv_size")) {
                        this.uvSize = this.parseExpressionArray(parser, 2, uvComponents.get("uv_size"));
                    }
                }
            }
        }
    }

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.particleSize = this.size;
        emitter.particleTextureWidth = this.textureWidth;
        emitter.particleTextureHeight = this.textureHeight;
        emitter.particleUV = this.uv;
        emitter.particleUVSize = this.uvSize;

        //flipbook mode values
        emitter.particleFlipbook = this.particleFlipbook;
        emitter.particleUVStepSize = this.particleUVStepSize;
        emitter.particleFPS = this.particleFPS;
        emitter.particleMaxFrame = this.particleMaxFrame;
        emitter.particleFlipbookStretchToLifetime = this.particleFlipbookStretchToLifetime;
        emitter.particleFlipbookLoop = this.particleFlipbookLoop;
    }
}
