package anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class AppearanceTintingComponent extends RiftLibParticleComponent {
    private IValue red = MolangParser.ONE;
    private IValue green = MolangParser.ONE;
    private IValue blue = MolangParser.ONE;
    private IValue alpha = MolangParser.ONE; //assumed value of colorAlpha, usually

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("color")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("color");

            //if color is a string, it is assumed to be a hex starting with #
            //have to do bit manipulation to get rgb and colorAlpha
            if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) {
                String colorAsString = componentValue.string;
                if (colorAsString.charAt(0) != '#') throw new InvalidValueException("String does not correspond to a color value!");
                colorAsString = colorAsString.substring(1);

                //check string length
                if (colorAsString.length() < 6 || colorAsString.length() == 7 || colorAsString.length() > 8) throw new InvalidValueException("String does not correspond to a color value!");

                //turn the string into a hex number
                int stringAsHex = Integer.parseInt(colorAsString, 16);

                //perform operations based on length
                if (colorAsString.length() == 6) {
                    this.red = new Constant((stringAsHex >> 16) / 255f);
                    this.green = new Constant(((stringAsHex >> 8) & 0xFF) / 255f);
                    this.blue = new Constant((stringAsHex & 0xFF) / 255f);
                }
                else if (colorAsString.length() == 8) {
                    this.alpha = new Constant((stringAsHex >> 24) / 255f);
                    this.red = new Constant(((stringAsHex >> 16) & 0xFF) / 255f);
                    this.green = new Constant(((stringAsHex >> 8) & 0xFF) / 255f);
                    this.blue = new Constant((stringAsHex & 0xFF) / 255f);
                }
            }
            //if its an array, each value in the array will correspond to a color
            else if (componentValue.valueType == RawParticleComponent.ComponentValueType.ARRAY) {
                //check length first
                if (componentValue.array.size() < 3) throw new InvalidValueException("Insufficient size for color array");
                if (componentValue.array.size() > 4) throw new InvalidValueException("Size exceeded for color array");

                this.red = this.parseExpression(parser, componentValue.array.get(0));
                this.green = this.parseExpression(parser, componentValue.array.get(1));
                this.blue = this.parseExpression(parser, componentValue.array.get(2));
                if (componentValue.array.size() == 4) this.alpha = this.parseExpression(parser, componentValue.array.get(3));
            }
            //interpolation based color
            //todo in the near future
            else if (componentValue.valueType == RawParticleComponent.ComponentValueType.OBJECT) {
                RiftLib.LOGGER.warn("Color gradient is unsupported, defaulting to argb value #FFFFFFFF");
            }
        }
    }

    @Override
    public void applyComponent(RiftLibParticle particle) {
        particle.colorArray = new IValue[]{this.red, this.green, this.blue};
        particle.colorAlpha = this.alpha;
    }
}
