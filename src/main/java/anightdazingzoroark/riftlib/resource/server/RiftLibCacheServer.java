package anightdazingzoroark.riftlib.resource.server;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.animation.AnimationFile;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.hitbox.HitboxDefinitionList;
import anightdazingzoroark.riftlib.jsonParsing.RiftLibResourceReader;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.resource.RiftLibResourceHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RiftLibCacheServer extends RiftLibResourceHolder {
    private static RiftLibCacheServer INSTANCE;
    @NotNull
    private Map<ResourceLocation, AnimationFile> animations = new HashMap<>();
    @NotNull
    private Map<ResourceLocation, GeoModel> geoModels = new HashMap<>();
    @NotNull
    private Map<ResourceLocation, HitboxDefinitionList> hitboxDefinitions = new HashMap<>();

    private final Map<ResourceLocation, ResourceOpener> resources = new HashMap<>();

    public static RiftLibCacheServer getInstance() {
        if (INSTANCE == null) INSTANCE = new RiftLibCacheServer();
        return INSTANCE;
    }

    public void load() {
        this.resources.clear();

        for (ModContainer modContainer : Loader.instance().getModList()) {
            File source = modContainer.getSource();
            if (source == null) continue;

            if (source.isDirectory()) {
                this.collectFolderResources(source);
                for (File resourceRoot : this.getDevelopmentResourceRoots(source)) {
                    this.collectFolderResources(resourceRoot);
                }
            }
            else if (source.isFile()) this.collectZipResources(source);
        }

        for (File assetRoot : this.getClasspathAssetRoots()) {
            this.collectAssetRoot(assetRoot);
        }

        Map<ResourceLocation, AnimationFile> tempAnimations = new HashMap<>();
        Map<ResourceLocation, GeoModel> tempModels = new HashMap<>();
        Map<ResourceLocation, HitboxDefinitionList> tempHitboxes = new HashMap<>();
        RiftLibResourceReader resourceReader = location -> {
            ResourceOpener opener = this.resources.get(location);
            if (opener == null) throw new IOException("Unknown resource " + location);
            return opener.open();
        };

        for (ResourceLocation location : this.resources.keySet()) {
            String path = location.getPath();

            if (path.startsWith("animations/") && path.endsWith(".json")) {
                try {
                    tempAnimations.put(location, this.loader.loadAnimationFile(resourceReader, location));
                }
                catch (Exception e) {
                    RiftLib.LOGGER.error("Error loading server animation file \"" + location + "\"!", e);
                }
            }
            else if (path.startsWith("geo/") && path.endsWith(".json")) {
                try {
                    tempModels.put(location, this.loader.loadGeoModel(resourceReader, location));
                }
                catch (Exception e) {
                    RiftLib.LOGGER.error("Error loading server model file \"" + location + "\"!", e);
                }
            }
            else if (path.startsWith("hitboxes/") && path.endsWith(".json")) {
                try {
                    tempHitboxes.put(location, this.loader.loadHitboxDefinitionList(resourceReader, location));
                }
                catch (Exception e) {
                    RiftLib.LOGGER.error("Error loading server hitbox file \"" + location + "\"!", e);
                }
            }
        }

        this.animations = tempAnimations;
        this.geoModels = tempModels;
        this.hitboxDefinitions = tempHitboxes;
    }

    private void collectFolderResources(File source) {
        this.collectAssetRoot(new File(source, "assets"));
    }

    private void collectAssetRoot(File assets) {
        File[] domains = assets.listFiles(File::isDirectory);

        if (domains == null) return;

        for (File domain : domains) {
            this.collectFolderResources(domain, "animations", fileName -> fileName.endsWith(".json"));
            this.collectFolderResources(domain, "geo", fileName -> fileName.endsWith(".json"));
            this.collectFolderResources(domain, "hitboxes", fileName -> fileName.endsWith(".json"));
        }
    }

    private List<File> getDevelopmentResourceRoots(File source) {
        List<File> roots = new ArrayList<>();
        String path = source.getPath().replace('\\', '/');

        if (path.endsWith("build/classes/java/main")) {
            File javaDir = source.getParentFile();
            File classesDir = javaDir == null ? null : javaDir.getParentFile();
            File buildDir = classesDir == null ? null : classesDir.getParentFile();

            if (buildDir != null) {
                roots.add(new File(buildDir, "resources/main"));
            }
        }

        return roots;
    }

    private List<File> getClasspathAssetRoots() {
        List<File> roots = new ArrayList<>();

        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources("assets");

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (!"file".equals(url.getProtocol())) continue;

                roots.add(new File(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8)));
            }
        }
        catch (IOException e) {
            RiftLib.LOGGER.error("Error scanning classpath assets!", e);
        }

        return roots;
    }

    private void collectFolderResources(File domain, String folder, Predicate<String> predicate) {
        File root = new File(domain, folder);

        this.collectFolderResources(domain.getName(), root, folder, predicate);
    }

    private void collectFolderResources(String domain, File parent, String prefix, Predicate<String> predicate) {
        File[] files = parent.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && predicate.test(file.getName())) {
                ResourceLocation location = new ResourceLocation(domain, prefix + "/" + file.getName());
                this.resources.put(location, () -> new FileInputStream(file));
            }
            else if (file.isDirectory()) {
                this.collectFolderResources(domain, file, prefix + "/" + file.getName(), predicate);
            }
        }
    }

    private void collectZipResources(File source) {
        try (ZipFile zipFile = new ZipFile(source)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) continue;

                this.collectZipResource(source, entry.getName(), "animations", fileName -> fileName.endsWith(".json"));
                this.collectZipResource(source, entry.getName(), "geo", fileName -> fileName.endsWith(".json"));
                this.collectZipResource(source, entry.getName(), "hitboxes", fileName -> fileName.endsWith(".json"));
            }
        }
        catch (IOException e) {
            RiftLib.LOGGER.error("Error scanning server resource jar \"" + source + "\"!", e);
        }
    }

    private void collectZipResource(File source, String entryName, String folder, Predicate<String> predicate) {
        String assetsPrefix = "assets/";
        String folderPart = "/" + folder + "/";
        int folderIndex = entryName.indexOf(folderPart);

        if (!entryName.startsWith(assetsPrefix) || folderIndex < 0 || !predicate.test(entryName)) return;

        String domain = entryName.substring(assetsPrefix.length(), folderIndex);
        String path = entryName.substring(folderIndex + 1);
        ResourceLocation location = new ResourceLocation(domain, path);

        this.resources.put(location, () -> {
            ZipFile zipFile = new ZipFile(source);
            ZipEntry zipEntry = zipFile.getEntry(entryName);

            if (zipEntry == null) {
                zipFile.close();
                throw new IOException("Missing zip entry " + entryName);
            }

            return new ZipEntryInputStream(zipFile, zipFile.getInputStream(zipEntry));
        });
    }

    @Override
    public Map<ResourceLocation, AnimationFile> getAnimations() {
        if (!RiftLib.isInitialized()) {
            throw new RuntimeException("RiftLib was never initialized! Please read the documentation!");
        }

        return this.animations;
    }

    @Override
    public Map<ResourceLocation, GeoModel> getGeoModels() {
        if (!RiftLib.isInitialized()) {
            throw new RuntimeException("RiftLib was never initialized! Please read the documentation!");
        }

        return this.geoModels;
    }

    @Override
    public Map<ResourceLocation, HitboxDefinitionList> getHitboxDefinitions() {
        if (!RiftLib.isInitialized()) {
            throw new RuntimeException("RiftLib was never initialized! Please read the documentation!");
        }

        return this.hitboxDefinitions;
    }

    @FunctionalInterface
    private interface ResourceOpener {
        InputStream open() throws IOException;
    }

    private static class ZipEntryInputStream extends InputStream {
        private final ZipFile zipFile;
        private final InputStream inputStream;

        private ZipEntryInputStream(ZipFile zipFile, InputStream inputStream) {
            this.zipFile = zipFile;
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return this.inputStream.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return this.inputStream.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            try {
                this.inputStream.close();
            }
            finally {
                this.zipFile.close();
            }
        }
    }
}
