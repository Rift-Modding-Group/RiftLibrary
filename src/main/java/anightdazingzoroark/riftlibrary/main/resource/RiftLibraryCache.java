package anightdazingzoroark.riftlibrary.main.resource;

import anightdazingzoroark.riftlibrary.main.RiftLibrary;
import anightdazingzoroark.riftlibrary.main.RiftLibraryMod;
import anightdazingzoroark.riftlibrary.main.assetLoader.AssetLoader;
import anightdazingzoroark.riftlibrary.main.geo.basic.RiftLibModel;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.FMLFolderResourcePack;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("deprecation")
public class RiftLibraryCache implements IResourceManagerReloadListener {
    private static RiftLibraryCache INSTANCE;

    private final AssetLoader loader;


    public HashMap<ResourceLocation, RiftLibModel> getGeoModels() {
        if (!RiftLibrary.isInitialized()) {
            throw new RuntimeException("RiftLibrary was never initialized! Please read the documentation!");
        }
        return this.geoModels;
    }

    private HashMap<ResourceLocation, RiftLibModel> geoModels = new HashMap<>();

    protected RiftLibraryCache() {
        this.loader = new AssetLoader();
    }
    public static RiftLibraryCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RiftLibraryCache();
            return INSTANCE;
        }
        return INSTANCE;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        HashMap<ResourceLocation, RiftLibModel> tempModels = new HashMap<>();

        List<IResourcePack> packs = this.getPacks();

        if (packs == null) return;

        for (IResourcePack pack : packs) {
            for (ResourceLocation location : this.getLocations(pack, "geo", fileName -> fileName.endsWith(".json"))) {
                try {
                    tempModels.put(location, this.loader.loadModel(resourceManager, location));
                    //todo: add config that allows for removing this from log
                    RiftLibraryMod.LOGGER.debug("Loaded model file at \"" + location + "\"");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    RiftLibraryMod.LOGGER.error("Error loading model file \"" + location + "\"!", e);
                }
            }
        }

        this.geoModels = tempModels;
    }

    @SuppressWarnings("unchecked")
    private List<IResourcePack> getPacks() {
        try {
            Field field = FMLClientHandler.class.getDeclaredField("resourcePackList");
            field.setAccessible(true);

            return (List<IResourcePack>) field.get(FMLClientHandler.instance());
        }
        catch (Exception e) {
            RiftLibraryMod.LOGGER.error("Error accessing resource pack list!", e);
        }

        return null;
    }

    private List<ResourceLocation> getLocations(IResourcePack pack, String folder, Predicate<String> predicate) {
        if (pack instanceof LegacyV2Adapter adapter) {
            Field packField = null;

            for (Field field : adapter.getClass().getDeclaredFields()) {
                if (field.getType() == IResourcePack.class) {
                    packField = field;
                    break;
                }
            }

            if (packField != null) {
                packField.setAccessible(true);

                try {
                    return this.getLocations((IResourcePack) packField.get(adapter), folder, predicate);
                }
                catch (Exception e) {}
            }
        }

        List<ResourceLocation> locations = new ArrayList<ResourceLocation>();

        if (pack instanceof FolderResourcePack folderResourcePack) {
            this.handleFolderResourcePack(folderResourcePack, folder, predicate, locations);
        }
        else if (pack instanceof FileResourcePack fileResourcePack) {
            this.handleZipResourcePack(fileResourcePack, folder, predicate, locations);
        }

        return locations;
    }

    //-----folder handling-----
    private void handleFolderResourcePack(FolderResourcePack folderPack, String folder, Predicate<String> predicate,
                                          List<ResourceLocation> locations) {
        Field fileField = null;

        for (Field field : AbstractResourcePack.class.getDeclaredFields()) {
            if (field.getType() == File.class) {
                fileField = field;
                break;
            }
        }

        if (fileField != null) {
            fileField.setAccessible(true);

            try {
                File file = (File) fileField.get(folderPack);
                Set<String> domains = folderPack.getResourceDomains();

                if (folderPack instanceof FMLFolderResourcePack) {
                    domains.add(((FMLFolderResourcePack) folderPack).getFMLContainer().getModId());
                }

                for (String domain : domains) {
                    String prefix = "assets/" + domain + "/" + folder;
                    File pathFile = new File(file, prefix);

                    this.enumerateFiles(folderPack, pathFile, predicate, locations, domain, folder);
                }
            }
            catch (IllegalAccessException e) {
                RiftLibraryMod.LOGGER.error(e);
            }
        }
    }



    private void enumerateFiles(FolderResourcePack folderPack, File parent, Predicate<String> predicate,
                                List<ResourceLocation> locations, String domain, String prefix) {
        File[] files = parent.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && predicate.test(file.getName())) {
                locations.add(new ResourceLocation(domain, prefix + "/" + file.getName()));
            } else if (file.isDirectory()) {
                this.enumerateFiles(folderPack, file, predicate, locations, domain, prefix + "/" + file.getName());
            }
        }
    }

    //-----zip file handling-----
    private void handleZipResourcePack(FileResourcePack filePack, String folder, Predicate<String> predicate,
                                       List<ResourceLocation> locations) {
        Field zipField = null;

        for (Field field : FileResourcePack.class.getDeclaredFields()) {
            if (field.getType() == ZipFile.class) {
                zipField = field;

                break;
            }
        }

        if (zipField != null) {
            zipField.setAccessible(true);

            try {
                this.enumerateZipFile(filePack, folder, (ZipFile) zipField.get(filePack), predicate, locations);
            }
            catch (IllegalAccessException e) {
                RiftLibraryMod.LOGGER.error(e);
            }
        }
    }

    private void enumerateZipFile(FileResourcePack filePack, String folder, ZipFile file, Predicate<String> predicate,
                                  List<ResourceLocation> locations) {
        Set<String> domains = filePack.getResourceDomains();
        Enumeration<? extends ZipEntry> it = file.entries();

        while (it.hasMoreElements()) {
            String name = it.nextElement().getName();

            for (String domain : domains) {
                String assets = "assets/" + domain + "/";
                String path = assets + folder + "/";

                if (name.startsWith(path) && predicate.test(name)) {
                    locations.add(new ResourceLocation(domain, name.substring(assets.length())));
                }
            }
        }
    }
}
