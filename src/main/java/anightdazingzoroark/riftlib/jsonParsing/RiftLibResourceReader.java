package anightdazingzoroark.riftlib.jsonParsing;

import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface RiftLibResourceReader {
    InputStream open(ResourceLocation location) throws IOException;
}
