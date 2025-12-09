package anightdazingzoroark.riftlib.particle.particleComponent.appearance;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class AppearanceLightingComponent extends RiftLibParticleComponent {
    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {}

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.particleUseLocalLighting = true;
    }
}
