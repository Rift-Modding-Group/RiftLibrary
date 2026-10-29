package anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.ParticleBlockRule;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//if a particle is not in these blocks, they will expire
public class ParticleExpireNotInBlockComponent extends RiftLibParticleComponent {
    private List<String> blocksExpireIfNotIn = new ArrayList<>();

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("$value")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("$value");

            //parse the array
            this.blocksExpireIfNotIn = componentValue.asArray().stream()
                    .map(RawParticleComponent.ComponentValue::asString)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        particle.setExpireNotInBlocks(this.blocksExpireIfNotIn.stream()
                .map(this::parseRule).collect(Collectors.toList()));
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

    public static class Builder extends RiftLibParticleComponent.Builder<ParticleExpireNotInBlockComponent> {
        private final ParticleExpireNotInBlockComponent toReturn = new ParticleExpireNotInBlockComponent();

        @NotNull
        public Builder setBlocks(@NotNull String... blocks) {
            this.toReturn.blocksExpireIfNotIn = List.copyOf(Arrays.asList(blocks));
            return this;
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_expire_if_not_in_blocks";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "$value", Builder.this.toReturn.blocksExpireIfNotIn
                    );
                }
            };
        }

        @Override
        @NotNull
        public ParticleExpireNotInBlockComponent build(@NotNull MolangParser parser) {
            ParticleExpireNotInBlockComponent result = new ParticleExpireNotInBlockComponent();
            result.blocksExpireIfNotIn = List.copyOf(this.toReturn.blocksExpireIfNotIn);
            return result;
        }
    }
}
