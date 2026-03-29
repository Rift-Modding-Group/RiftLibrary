package anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
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

    //return values
    private float uvXMin;
    private float uvYMin;
    private float uvXMax;
    private float uvYMax;

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
        particle.particleAppearance = this;
    }

    public double[] getSize() {
        return new double[]{this.size[0].get(), this.size[1].get()};
    }

    public ParticleCameraMode getCameraMode() {
        return this.cameraMode;
    }

    public void updateAppearance(RiftLibParticle particle) {
        //static appearance
        if (!this.particleFlipbook) {
            this.uvXMin = (float) (this.uv[0].get() / this.textureWidth);
            this.uvYMin = (float) (this.uv[1].get() / this.textureHeight);
            this.uvXMax = (float) ((this.uv[0].get() + this.uvSize[0].get()) / this.textureWidth);
            this.uvYMax = (float) ((this.uv[1].get() + this.uvSize[1].get()) / this.textureHeight);
            return;
        }
        //flipbook appearance from here on out

        //compute frame
        float timePercentage = particle.getAge() / (float) particle.getLifetime();
        float ageSeconds = particle.getAge() / 20f;
        float frame = this.particleFlipbookStretchToLifetime && particle.getLifetime() > 0 ?
                (float) (timePercentage * this.particleMaxFrame.get()) : ageSeconds * this.particleFPS;

        //wrap around
        if (this.particleFlipbookLoop) {
            frame = (float) (frame % this.particleMaxFrame.get());
            if (frame < 0) frame += (float) this.particleMaxFrame.get();
        }
        //clamp
        else {
            if (frame >= this.particleMaxFrame.get()) frame = (int) this.particleMaxFrame.get() - 1;
            if (frame < 0) frame = 0;
        }

        float uvXPixels = (float) (this.uv[0].get() + this.particleUVStepSize[0].get() * Math.floor(frame));
        float uvYPixels = (float) (this.uv[1].get() + this.particleUVStepSize[1].get() * Math.floor(frame));

        //set values
        this.uvXMin = uvXPixels / this.textureWidth;
        this.uvYMin = uvYPixels / this.textureHeight;
        this.uvXMax = (float) (uvXPixels + this.uvSize[0].get()) / this.textureWidth;
        this.uvYMax = (float) (uvYPixels + this.uvSize[1].get()) / this.textureHeight;
    }

    public float[] getUVs() {
        return new float[]{this.uvXMin, this.uvYMin, this.uvXMax, this.uvYMax};
    }
}
