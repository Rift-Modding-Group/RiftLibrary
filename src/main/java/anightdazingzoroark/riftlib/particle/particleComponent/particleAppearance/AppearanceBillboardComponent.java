package anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class AppearanceBillboardComponent extends RiftLibParticleComponent {
    private IValue[] size;
    private ParticleCameraMode cameraMode;
    private int textureWidth, textureHeight;
    private IValue[] uv;
    private IValue[] uvSize;
    //these only matter if the particle is flipbook mode
    private boolean particleFlipbook;
    private IValue[] particleUVStepSize;
    private float particleFPS;
    private IValue particleMaxFrame;
    private boolean particleFlipbookStretchToLifetime;
    private boolean particleFlipbookLoop;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        //specifies the x/y size of the billboard
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("size")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("size");
            this.size = this.parseExpressionArray(parser, 2, componentValue);
        }
        //get camera mode
        if (rawComponent.getValue().componentValues.containsKey("facing_camera_mode")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("facing_camera_mode");
            this.cameraMode = ParticleCameraMode.getCameraModeFromString(componentValue.string);
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
    public void applyComponent(RiftLibParticle particle) {
        particle.textureWidth = this.textureWidth;
        particle.textureHeight = this.textureHeight;
        particle.cameraMode = this.cameraMode;
        particle.size = this.size;

        //differentiate between particle flipbook or not
        if (this.particleFlipbook) {
            particle.flipbook = true;
            particle.flipbookStretchToLifetime = this.particleFlipbookStretchToLifetime;
            particle.flipbookLoop = this.particleFlipbookLoop;
            particle.fps = this.particleFPS;
            particle.maxFrame = (int) this.particleMaxFrame.get();

            //store UV info
            particle.particleUV = this.uv;
            particle.particleUVSize = this.uvSize;
            particle.particleUVStepSize = this.particleUVStepSize;
        }
        else {
            particle.flipbook = false;
            particle.particleUV = this.uv;
            particle.particleUVSize = this.uvSize;
        }
    }
}
