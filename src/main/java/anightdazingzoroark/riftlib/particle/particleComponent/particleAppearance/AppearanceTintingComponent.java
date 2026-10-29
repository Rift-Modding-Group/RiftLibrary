package anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class AppearanceTintingComponent extends RiftLibParticleComponent {
    private IValue red = MolangParser.ONE;
    private IValue green = MolangParser.ONE;
    private IValue blue = MolangParser.ONE;
    private IValue alpha = MolangParser.ONE; //assumed value of colorAlpha, usually

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("color")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("color");

            //if color is a string, it is assumed to be a hex starting with #
            //have to do bit manipulation to get rgb and colorAlpha
            if (componentValue.isString()) {
                String colorAsString = componentValue.asString();
                if (colorAsString.charAt(0) != '#')
                    throw new IllegalArgumentException("String does not correspond to a color value!");
                colorAsString = colorAsString.substring(1);

                //check string length
                if (colorAsString.length() < 6 || colorAsString.length() == 7 || colorAsString.length() > 8)
                    throw new IllegalArgumentException("String does not correspond to a color value!");

                //turn the string into a hex number
                int stringAsHex = Integer.parseInt(colorAsString, 16);

                //perform operations based on length
                if (colorAsString.length() == 6) {
                    this.red = new Constant((stringAsHex >> 16) / 255f);
                    this.green = new Constant(((stringAsHex >> 8) & 0xFF) / 255f);
                    this.blue = new Constant((stringAsHex & 0xFF) / 255f);
                } else if (colorAsString.length() == 8) {
                    this.alpha = new Constant((stringAsHex >> 24) / 255f);
                    this.red = new Constant(((stringAsHex >> 16) & 0xFF) / 255f);
                    this.green = new Constant(((stringAsHex >> 8) & 0xFF) / 255f);
                    this.blue = new Constant((stringAsHex & 0xFF) / 255f);
                }
            }
            //if its an array, each value in the array will correspond to a color
            else if (componentValue.isArray()) {
                //check length first
                if (componentValue.asArray().size() < 3)
                    throw new IllegalArgumentException("Insufficient size for color array");
                if (componentValue.asArray().size() > 4)
                    throw new IllegalArgumentException("Size exceeded for color array");

                this.red = componentValue.asArray().get(0).asMolang().compile(parser);
                this.green = componentValue.asArray().get(1).asMolang().compile(parser);
                this.blue = componentValue.asArray().get(2).asMolang().compile(parser);
                if (componentValue.asArray().size() == 4)
                    this.alpha = componentValue.asArray().get(3).asMolang().compile(parser);
            }
            //interpolation based color
            //todo in the near future
            else if (componentValue instanceof RawParticleComponent.ObjectValue) {
                RiftLib.LOGGER.warn("Color gradient is unsupported, defaulting to argb value #FFFFFFFF");
            }
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        particle.setColor(new IValue[]{this.red, this.green, this.blue}, this.alpha);
    }

    public static class Builder extends RiftLibParticleComponent.Builder<AppearanceTintingComponent> {
        private final AppearanceTintingComponent toReturn = new AppearanceTintingComponent();

        @NotNull
        public Builder setRed(@NotNull IValue value) {
            this.toReturn.red = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setRed(double value) {
            return this.setRed(new Constant(value));
        }

        @NotNull
        public Builder setRed(@NotNull String expression) {
            return this.setRed(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setRed(@NotNull RawMolangValue value) {
            return this.setRed(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setGreen(@NotNull IValue value) {
            this.toReturn.green = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setGreen(double value) {
            return this.setGreen(new Constant(value));
        }

        @NotNull
        public Builder setGreen(@NotNull String expression) {
            return this.setGreen(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setGreen(@NotNull RawMolangValue value) {
            return this.setGreen(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setBlue(@NotNull IValue value) {
            this.toReturn.blue = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setBlue(double value) {
            return this.setBlue(new Constant(value));
        }

        @NotNull
        public Builder setBlue(@NotNull String expression) {
            return this.setBlue(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setBlue(@NotNull RawMolangValue value) {
            return this.setBlue(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setAlpha(@NotNull IValue value) {
            this.toReturn.alpha = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setAlpha(double value) {
            return this.setAlpha(new Constant(value));
        }

        @NotNull
        public Builder setAlpha(@NotNull String expression) {
            return this.setAlpha(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setAlpha(@NotNull RawMolangValue value) {
            return this.setAlpha(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_appearance_tinting";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "color", new IValue[]{Builder.this.toReturn.red, Builder.this.toReturn.green, Builder.this.toReturn.blue, Builder.this.toReturn.alpha}
                    );
                }
            };
        }

        @Override
        @NotNull
        public AppearanceTintingComponent build(@NotNull MolangParser parser) {
            AppearanceTintingComponent result = new AppearanceTintingComponent();
            result.red = RawMolangValue.copy(this.toReturn.red, parser);
            result.green = RawMolangValue.copy(this.toReturn.green, parser);
            result.blue = RawMolangValue.copy(this.toReturn.blue, parser);
            result.alpha = RawMolangValue.copy(this.toReturn.alpha, parser);
            return result;
        }
    }
}
