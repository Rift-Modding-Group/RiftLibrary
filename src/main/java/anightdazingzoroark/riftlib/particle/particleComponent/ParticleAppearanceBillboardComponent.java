package anightdazingzoroark.riftlib.particle.particleComponent;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;

import java.util.Map;

public class ParticleAppearanceBillboardComponent extends RiftLibParticleComponent {
    public IValue[] size;
    public int textureWidth, textureHeight;
    public IValue[] uv;
    public IValue[] uvSize;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
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
                if (uvComponent.getKey().equals("uv")) {
                    this.uv = this.parseExpressionArray(parser, 2, uvComponent.getValue());
                }
                if (uvComponent.getKey().equals("uv_size")) {
                    this.uvSize = this.parseExpressionArray(parser, 2, uvComponent.getValue());
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
    }
}
