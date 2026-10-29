package anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class AppearanceBillboardComponent extends RiftLibParticleComponent {
    private IValue[] size = new IValue[]{MolangParser.ONE, MolangParser.ONE};
    private ParticleCameraMode cameraMode = ParticleCameraMode.LOOKAT_XYZ;
    private int textureWidth = 1, textureHeight = 1;
    private IValue[] uv = new IValue[]{MolangParser.ZERO, MolangParser.ZERO};
    private IValue[] uvSize = new IValue[]{MolangParser.ONE, MolangParser.ONE};

    //these only matter if the particle is flipbook mode
    private boolean particleFlipbook;
    private IValue[] particleUVStepSize = new IValue[]{MolangParser.ZERO, MolangParser.ZERO};
    private float particleFPS = 1F;
    private IValue particleMaxFrame = MolangParser.ONE;
    private boolean particleFlipbookStretchToLifetime;
    private boolean particleFlipbookLoop;

    //return values
    private float uvXMin;
    private float uvYMin;
    private float uvXMax;
    private float uvYMax;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        //specifies the x/y size of the billboard
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("size")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("size");
            this.size = RawMolangValue.compileArray(parser, componentValue.asMolangArray(2));
        }
        //get camera mode
        if (rawComponent.getValue().componentValues.containsKey("facing_camera_mode")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("facing_camera_mode");
            this.cameraMode = ParticleCameraMode.getCameraModeFromString(componentValue.asString());
        }
        //get UV information
        if (rawComponent.getValue().componentValues.containsKey("uv")) {
            Map<String, RawParticleComponent.ComponentValue> uvComponents = rawComponent.getValue().componentValues.get("uv").asObject();

            if (uvComponents.containsKey("texture_width")) {
                this.textureWidth = (int) uvComponents.get("texture_width").asNumber();
            }
            if (uvComponents.containsKey("texture_height")) {
                this.textureHeight = (int) uvComponents.get("texture_height").asNumber();
            }

            //for flipbook particles
            RawParticleComponent.ComponentValue flipbookVal = uvComponents.get("flipbook");
            if (flipbookVal instanceof RawParticleComponent.ObjectValue) {
                this.particleFlipbook = true;

                Map<String, RawParticleComponent.ComponentValue> flip = flipbookVal.asObject();

                if (flip.containsKey("base_UV")) {
                    this.uv = RawMolangValue.compileArray(parser, flip.get("base_UV").asMolangArray(2));
                }
                if (flip.containsKey("size_UV")) {
                    this.uvSize = RawMolangValue.compileArray(parser, flip.get("size_UV").asMolangArray(2));
                }
                if (flip.containsKey("step_UV")) {
                    RawParticleComponent.ComponentValue step = flip.get("step_UV");
                    RawMolangValue[] stepValues = step.asMolangArray(2);
                    for (RawParticleComponent.ComponentValue value : step.asArray()) value.asNumber();
                    this.particleUVStepSize = RawMolangValue.compileArray(parser, stepValues);
                }
                if (flip.containsKey("frames_per_second")) {
                    this.particleFPS = (float) flip.get("frames_per_second").asNumber();
                }
                if (flip.containsKey("max_frame")) {
                    this.particleMaxFrame = flip.get("max_frame").asMolang().compile(parser);
                }
                if (flip.containsKey("stretch_to_lifetime")) {
                    this.particleFlipbookStretchToLifetime = flip.get("stretch_to_lifetime").asBoolean();
                }
                if (flip.containsKey("loop")) {
                    this.particleFlipbookLoop = flip.get("loop").asBoolean();
                }
            }
            //for static particles
            else {
                this.particleFlipbook = false;

                if (uvComponents.containsKey("uv")) {
                    this.uv = RawMolangValue.compileArray(parser, uvComponents.get("uv").asMolangArray(2));
                }
                if (uvComponents.containsKey("uv_size")) {
                    this.uvSize = RawMolangValue.compileArray(parser, uvComponents.get("uv_size").asMolangArray(2));
                }
            }
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        // Each particle keeps its own evaluated UV coordinates.
        AppearanceBillboardComponent appearance = new AppearanceBillboardComponent();
        appearance.size = this.size.clone();
        appearance.cameraMode = this.cameraMode;
        appearance.textureWidth = this.textureWidth;
        appearance.textureHeight = this.textureHeight;
        appearance.uv = this.uv.clone();
        appearance.uvSize = this.uvSize.clone();
        appearance.particleFlipbook = this.particleFlipbook;
        appearance.particleUVStepSize = this.particleUVStepSize.clone();
        appearance.particleFPS = this.particleFPS;
        appearance.particleMaxFrame = this.particleMaxFrame;
        appearance.particleFlipbookStretchToLifetime = this.particleFlipbookStretchToLifetime;
        appearance.particleFlipbookLoop = this.particleFlipbookLoop;
        particle.setAppearance(appearance);
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

    public static class Builder extends RiftLibParticleComponent.Builder<AppearanceBillboardComponent> {
        private final AppearanceBillboardComponent toReturn = new AppearanceBillboardComponent();

        @NotNull
        public Builder setSize(@NotNull IValue x, @NotNull IValue y) {
            this.toReturn.size = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y)};
            return this;
        }

        @NotNull
        public Builder setSize(double x, double y) {
            return this.setSize(new Constant(x), new Constant(y));
        }

        @NotNull
        public Builder setCameraMode(@NotNull ParticleCameraMode value) {
            this.toReturn.cameraMode = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setTextureWidth(int value) {
            if (value <= 0) throw new IllegalArgumentException("Texture dimensions must be positive");
            this.toReturn.textureWidth = value;
            return this;
        }

        @NotNull
        public Builder setTextureHeight(int value) {
            if (value <= 0) throw new IllegalArgumentException("Texture dimensions must be positive");
            this.toReturn.textureHeight = value;
            return this;
        }

        @NotNull
        public Builder setUv(@NotNull IValue x, @NotNull IValue y) {
            this.toReturn.uv = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y)};
            return this;
        }

        @NotNull
        public Builder setUv(double x, double y) {
            return this.setUv(new Constant(x), new Constant(y));
        }

        @NotNull
        public Builder setUvSize(@NotNull IValue x, @NotNull IValue y) {
            this.toReturn.uvSize = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y)};
            return this;
        }

        @NotNull
        public Builder setUvSize(double x, double y) {
            return this.setUvSize(new Constant(x), new Constant(y));
        }

        @NotNull
        public Builder setFlipbook(boolean value) {
            this.toReturn.particleFlipbook = value;
            return this;
        }

        @NotNull
        public Builder setUvStepSize(@NotNull IValue x, @NotNull IValue y) {
            RawMolangValue.constant(x);
            RawMolangValue.constant(y);
            this.toReturn.particleUVStepSize = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y)};
            return this;
        }

        @NotNull
        public Builder setUvStepSize(double x, double y) {
            return this.setUvStepSize(new Constant(x), new Constant(y));
        }

        @NotNull
        public Builder setFramesPerSecond(float value) {
            if (value < 0F || Float.compare(value, Float.MAX_VALUE) > 0)
                throw new IllegalArgumentException("Value must be finite and nonnegative");
            this.toReturn.particleFPS = value;
            return this;
        }

        @NotNull
        public Builder setMaxFrame(@NotNull IValue value) {
            this.toReturn.particleMaxFrame = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setMaxFrame(double value) {
            return this.setMaxFrame(new Constant(value));
        }

        @NotNull
        public Builder setMaxFrame(@NotNull String expression) {
            return this.setMaxFrame(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setMaxFrame(@NotNull RawMolangValue value) {
            return this.setMaxFrame(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setStretchToLifetime(boolean value) {
            this.toReturn.particleFlipbookStretchToLifetime = value;
            return this;
        }

        @NotNull
        public Builder setLoop(boolean value) {
            this.toReturn.particleFlipbookLoop = value;
            return this;
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_appearance_billboard";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    Map<String, Object> uvValues = new java.util.LinkedHashMap<>();
                    uvValues.put("texture_width", Builder.this.toReturn.textureWidth);
                    uvValues.put("texture_height", Builder.this.toReturn.textureHeight);
                    if (Builder.this.toReturn.particleFlipbook) {
                        uvValues.put("flipbook", Map.of(
                                "base_UV", Builder.this.toReturn.uv, "size_UV", Builder.this.toReturn.uvSize, "step_UV", Builder.this.toReturn.particleUVStepSize,
                                "frames_per_second", Builder.this.toReturn.particleFPS, "max_frame", Builder.this.toReturn.particleMaxFrame,
                                "stretch_to_lifetime", Builder.this.toReturn.particleFlipbookStretchToLifetime, "loop", Builder.this.toReturn.particleFlipbookLoop));
                    }
                    else {
                        uvValues.put("uv", Builder.this.toReturn.uv);
                        uvValues.put("uv_size", Builder.this.toReturn.uvSize);
                    }
                    return Map.of(
                            "size", Builder.this.toReturn.size,
                            "facing_camera_mode", Builder.this.toReturn.cameraMode.name().toLowerCase(),
                            "uv", uvValues
                    );
                }
            };
        }

        @Override
        @NotNull
        public AppearanceBillboardComponent build(@NotNull MolangParser parser) {
            AppearanceBillboardComponent result = new AppearanceBillboardComponent();
            result.size = RawMolangValue.copy(this.toReturn.size, parser);
            result.cameraMode = this.toReturn.cameraMode;
            result.textureWidth = this.toReturn.textureWidth;
            result.textureHeight = this.toReturn.textureHeight;
            result.uv = RawMolangValue.copy(this.toReturn.uv, parser);
            result.uvSize = RawMolangValue.copy(this.toReturn.uvSize, parser);
            result.particleFlipbook = this.toReturn.particleFlipbook;
            result.particleUVStepSize = RawMolangValue.copy(this.toReturn.particleUVStepSize, parser);
            result.particleFPS = this.toReturn.particleFPS;
            result.particleMaxFrame = RawMolangValue.copy(this.toReturn.particleMaxFrame, parser);
            result.particleFlipbookStretchToLifetime = this.toReturn.particleFlipbookStretchToLifetime;
            result.particleFlipbookLoop = this.toReturn.particleFlipbookLoop;
            return result;
        }
    }
}
