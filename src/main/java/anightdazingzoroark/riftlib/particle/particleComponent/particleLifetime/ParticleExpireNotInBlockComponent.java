package anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.ParticleBlockRule;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//if a particle is not in these blocks, they will expire
public class ParticleExpireNotInBlockComponent extends RiftLibParticleComponent {
    private List<String> blocksExpireIfNotIn = new ArrayList<>();

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("$value")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("$value");

            //parse the array
            this.blocksExpireIfNotIn = componentValue.array.stream()
                    .map(o -> o.string)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void applyComponent(RiftLibParticle particle) {
        particle.blocksExpireIfNotIn = this.blocksExpireIfNotIn.stream()
                .map(this::parseRule).collect(Collectors.toList());
    }

    private ParticleBlockRule parseRule(String blockString) {
        int colonOne = blockString.indexOf(":");
        int colonTwo = blockString.indexOf(":", colonOne + 1);

        String blockName = blockString;
        int meta = 0;
        if (colonTwo >= 0) {
            blockName = blockString.substring(0, colonTwo);
            meta = Integer.parseInt(blockString.substring(colonTwo + 1));
        }

        Block block = Block.getBlockFromName(blockName);
        if (block == null) return null;
        return new ParticleBlockRule(block, meta);
    }
}
