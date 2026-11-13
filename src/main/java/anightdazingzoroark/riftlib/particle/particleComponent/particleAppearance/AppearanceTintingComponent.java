package anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.exceptions.MolangException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntToDoubleFunction;

public class AppearanceTintingComponent extends RiftLibParticleComponent {
    @NotNull
    private IValue red = MolangParser.ONE;
    @NotNull
    private IValue green = MolangParser.ONE;
    @NotNull
    private IValue blue = MolangParser.ONE;
    @NotNull
    private IValue alpha = MolangParser.ONE; //assumed value of colorAlpha, usually

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("color")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("color");

            if (componentValue.isString()) {
                String input = componentValue.asString();
                try {
                    IValue source = parser.parseExpression(input);
                    AtomicReference<Color> evaluatedColor = new AtomicReference<>(Color.WHITE);
                    AtomicInteger readChannels = new AtomicInteger();
                    AtomicBoolean hasLoggedInvalidColor = new AtomicBoolean();
                    IntToDoubleFunction channelValue = channel -> {
                        int channelMask = 1 << channel;
                        int readMask = readChannels.get();
                        Color color = evaluatedColor.get();
                        // This component's RGBA reads share one evaluation of the source.
                        if (readMask == 0 || (readMask & channelMask) != 0) {
                            readMask = 0;
                            color = Color.WHITE;
                            try {
                                Object value = source.getValue();
                                if (!(value instanceof String string)) {
                                    throw new IllegalArgumentException("Particle color expression must return a color string, got: " + value);
                                }
                                color = parseColor(string);
                            }
                            catch (IllegalArgumentException exception) {
                                if (!hasLoggedInvalidColor.getAndSet(true)) {
                                    RiftLib.LOGGER.error("Could not evaluate particle tint; using white.", exception);
                                }
                            }
                            evaluatedColor.set(color);
                        }
                        readChannels.set(readMask | channelMask);
                        return switch (channel) {
                            case 0 -> color.getRed() / 255D;
                            case 1 -> color.getGreen() / 255D;
                            case 2 -> color.getBlue() / 255D;
                            case 3 -> color.getAlpha() / 255D;
                            default -> throw new IllegalArgumentException("Unknown color channel: " + channel);
                        };
                    };
                    this.red = () -> channelValue.applyAsDouble(0);
                    this.green = () -> channelValue.applyAsDouble(1);
                    this.blue = () -> channelValue.applyAsDouble(2);
                    this.alpha = () -> channelValue.applyAsDouble(3);
                }
                catch (MolangException exception) {
                    Color color = parseColor(input);
                    this.red = new Constant(color.getRed() / 255D);
                    this.green = new Constant(color.getGreen() / 255D);
                    this.blue = new Constant(color.getBlue() / 255D);
                    this.alpha = new Constant(color.getAlpha() / 255D);
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
                else this.alpha = MolangParser.ONE;
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

    @NotNull
    public static Color parseColor(@NotNull String value) {
        if (!value.matches("#[0-9a-fA-F]{6}(?:[0-9a-fA-F]{2})?")) {
            throw new IllegalArgumentException("Expected a color string in #RRGGBB or #AARRGGBB format, got: " + value);
        }
        int color = Integer.parseUnsignedInt(value.substring(1), 16);
        return value.length() == 7 ? Color.ofOpaque(color) : Color.ofTransparent(color);
    }

    public static class Builder extends RiftLibParticleComponent.Builder<AppearanceTintingComponent> {
        private final AppearanceTintingComponent toReturn = new AppearanceTintingComponent();

        @NotNull
        public Builder setColor(int color, boolean hasAlpha) {
            if (hasAlpha) {
                this.setRed((color >> 24) / 255D);
                this.setGreen(((color >> 16) & 0xFF) / 255D);
                this.setBlue(((color >> 8) & 0xFF) / 255D);
                this.setAlpha((color & 0xFF) / 255D);
            }
            else {
                this.setRed((color >> 16) / 255D);
                this.setGreen(((color >> 8) & 0xFF) / 255D);
                this.setBlue((color & 0xFF) / 255D);
                this.setAlpha(1D);
            }
            return this;
        }

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
        public Builder copy() {
            Builder result = new Builder();
            result.toReturn.red = this.toReturn.red;
            result.toReturn.green = this.toReturn.green;
            result.toReturn.blue = this.toReturn.blue;
            result.toReturn.alpha = this.toReturn.alpha;
            return result;
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
