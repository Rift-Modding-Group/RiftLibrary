package anightdazingzoroark.riftlib.resource.server;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.jsonParsing.RiftLibResourceReader;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RiftLibCacheServer extends RiftLibResourceHolder {
    private static RiftLibCacheServer INSTANCE;
    @NotNull
    private Map<String, Map<String, Animation>> animations = new HashMap<>();
    @NotNull
    private Map<String, Map<String, GeoModel>> geoModels = new HashMap<>();
    @NotNull
    private Set<String> particleIdentifiers = Set.of();

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

        Map<String, Map<String, Animation>> tempAnimations = new HashMap<>();
        Map<String, Map<String, GeoModel>> tempModels = new HashMap<>();
        Set<String> tempParticleIdentifiers = new HashSet<>();
        RiftLibResourceReader resourceReader = location -> {
            ResourceOpener opener = this.resources.get(location);
            if (opener == null) throw new IOException("Unknown resource " + location);
            return opener.open();
        };

        for (ResourceLocation location : this.resources.keySet()) {
            String path = location.getPath();
            String modId = location.getNamespace();

            if (path.startsWith("animations/") && path.endsWith(".json")) {
                try {
                    //load
                    Map<String, Animation> animations = this.loader.loadAnimationFile(resourceReader, location);

                    //merge w already existing animations
                    Map<String, Animation> modAnims = tempAnimations.computeIfAbsent(modId, key -> new HashMap<>());
                    for (Map.Entry<String, Animation> entry : animations.entrySet()) {
                        Animation previous = modAnims.put(entry.getKey(), entry.getValue());

                        if (previous != null) {
                            RiftLib.LOGGER.warn(
                                    "Duplicate Animation identifier \"{}\" for mod \"{}\" while loading {}",
                                    entry.getKey(), modId, location
                            );
                        }
                    }
                }
                catch (Exception e) {
                    RiftLib.LOGGER.error("Error loading server animation file \"" + location + "\"!", e);
                }
            }
            else if (path.startsWith("geo/") && path.endsWith(".json")) {
                try {
                    //load
                    Map<String, GeoModel> models = this.loader.loadGeoModels(resourceReader, location);

                    //merge with already existing models
                    Map<String, GeoModel> modModels = tempModels.computeIfAbsent(modId, key -> new HashMap<>());
                    for (Map.Entry<String, GeoModel> entry : models.entrySet()) {
                        GeoModel previous = modModels.put(entry.getKey(), entry.getValue());

                        if (previous != null) {
                            RiftLib.LOGGER.warn(
                                    "Duplicate GeoModel identifier \"{}\" for mod \"{}\" while loading {}",
                                    entry.getKey(), modId, location
                            );
                        }
                    }
                }
                catch (Exception e) {
                    RiftLib.LOGGER.error("Error loading server model file \"" + location + "\"!", e);
                }
            }
            else if (path.startsWith("particles/") && path.endsWith(".json")) {
                try {
                    tempParticleIdentifiers.add(this.loader.loadParticleIdentifier(resourceReader, location));
                }
                catch (Exception exception) {
                    RiftLib.LOGGER.error("Error loading server particle file \"" + location + "\"!", exception);
                }
            }
        }

        this.animations = tempAnimations;
        this.geoModels = tempModels;
        this.particleIdentifiers = Set.copyOf(tempParticleIdentifiers);
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
            this.collectFolderResources(domain, "particles", fileName -> fileName.endsWith(".json"));
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
                this.collectZipResource(source, entry.getName(), "particles", fileName -> fileName.endsWith(".json"));
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
    public Map<String, Map<String, Animation>> getAnimations() {
        if (!RiftLib.isInitialized()) {
            throw new RuntimeException("RiftLib was never initialized! Please read the documentation!");
        }

        return Map.copyOf(this.animations);
    }

    @Override
    public Map<String, Map<String, GeoModel>> getGeoModels() {
        if (!RiftLib.isInitialized()) {
            throw new RuntimeException("RiftLib was never initialized! Please read the documentation!");
        }

        return Map.copyOf(this.geoModels);
    }

    @NotNull
    public Set<String> getParticleIdentifiers() {
        if (!RiftLib.isInitialized()) {
            throw new RuntimeException("RiftLib was never initialized! Please read the documentation!");
        }

        return this.particleIdentifiers;
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
