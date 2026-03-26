package anightdazingzoroark.riftlibrary.main.assetLoader;

import anightdazingzoroark.riftlibrary.main.RiftLibraryMod;
import anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model.RawGeometryTree;
import anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model.RawModel;
import anightdazingzoroark.riftlibrary.main.geo.RiftLibModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class AssetLoader {
    private final Gson gson = new GsonBuilder().create();

    public RiftLibModel loadModel(IResourceManager resourceManager, ResourceLocation location) {
        //deserialize from json into basic json objects, bones are still stored as a
        //flat list
        RawModel rawModel = this.gson.fromJson(this.getResourceAsString(location, resourceManager), RawModel.class);

        //Parse the flat list of bones into a raw hierarchical tree of "BoneGroup"s
        RawGeometryTree rawGeometryTree = new RawGeometryTree(rawModel, location);

        // Build the quads and cubes from the raw tree into a built and ready to be
        // rendered GeoModel
        return GeoConstructor.constructGeoModel(rawGeometryTree);
    }

    private String getResourceAsString(ResourceLocation location, IResourceManager manager) {
        try (InputStream inputStream = manager.getResource(location).getInputStream()) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
        catch (Exception e) {
            String message = "Couldn't load " + location;
            RiftLibraryMod.LOGGER.error(message, e);
            throw new RuntimeException(new FileNotFoundException(location.toString()));
        }
    }
}
