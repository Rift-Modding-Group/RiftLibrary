package anightdazingzoroark.riftlib.resource.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.io.File;
import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import anightdazingzoroark.riftlib.animation.AnimationFile;
import anightdazingzoroark.riftlib.hitbox.HitboxDefinitionList;
import anightdazingzoroark.riftlib.jsonParsing.RiftLibResourceReader;

import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.resource.RiftLibResourceHolder;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LegacyV2Adapter;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.geo.GeoModel;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class RiftLibCacheClient extends RiftLibResourceHolder implements IResourceManagerReloadListener {
	private static RiftLibCacheClient INSTANCE;
	@NotNull
	private Map<ResourceLocation, AnimationFile> animations = new HashMap<>();
	@NotNull
	private Map<ResourceLocation, GeoModel> geoModels = new HashMap<>();
	@NotNull
	private Map<ResourceLocation, HitboxDefinitionList> hitboxDefinitions = new HashMap<>();
    @NotNull
	private Map<ResourceLocation, ParticleBuilder> particleBuilders = new HashMap<>();

	public static RiftLibCacheClient getInstance() {
		if (INSTANCE == null) INSTANCE = new RiftLibCacheClient();
		return INSTANCE;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		Map<ResourceLocation, AnimationFile> tempAnimations = new HashMap<>();
		Map<ResourceLocation, GeoModel> tempModels = new HashMap<>();
		Map<ResourceLocation, HitboxDefinitionList> tempHitboxes = new HashMap<>();
        Map<ResourceLocation, ParticleBuilder> tempParticleBuilders = new HashMap<>();
		List<IResourcePack> packs = this.getPacks();
		RiftLibResourceReader resourceReader = location -> resourceManager.getResource(location).getInputStream();

		if (packs == null) return;

		for (IResourcePack pack : packs) {
			for (ResourceLocation location : this.getLocations(pack, "animations",
					fileName -> fileName.endsWith(".json"))) {
				try {
					tempAnimations.put(location, this.loader.loadAnimationFile(resourceReader, location));
				}
				catch (Exception e) {
					e.printStackTrace();
					RiftLib.LOGGER.error("Error loading animation file \"" + location + "\"!", e);
				}
			}

			//this must be where the model files are being loaded
			for (ResourceLocation location : this.getLocations(pack, "geo", fileName -> fileName.endsWith(".json"))) {
				try {
					tempModels.put(location, this.loader.loadGeoModel(resourceReader, location));
				}
				catch (Exception e) {
					e.printStackTrace();
					RiftLib.LOGGER.error("Error loading model file \"" + location + "\"!", e);
				}
			}

			//load the hitbox files
			for (ResourceLocation location : this.getLocations(pack, "hitboxes", filename -> filename.endsWith(".json"))) {
				try {
					tempHitboxes.put(location, this.loader.loadHitboxDefinitionList(resourceReader, location));
				}
				catch (Exception e) {
					e.printStackTrace();
					RiftLib.LOGGER.error("Error loading hitbox file \""+location+"\"!", e);
				}
			}

            //load the particle files
            for (ResourceLocation location : this.getLocations(pack, "particles", filename -> filename.endsWith(".json"))) {
                try {
                    //temporarily like this coz it a print only void method
                    tempParticleBuilders.put(location, this.loader.loadParticle(this.parser, resourceReader, location));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    RiftLib.LOGGER.error("Error loading particle file \""+location+"\"!", e);
                }
            }
		}

		this.animations = tempAnimations;
		this.geoModels = tempModels;
		this.hitboxDefinitions = tempHitboxes;
        this.particleBuilders = tempParticleBuilders;
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

	//particle builders are only relevant on the client
	public Map<ResourceLocation, ParticleBuilder> getParticleBuilders() {
		if (!RiftLib.isInitialized()) {
			throw new RuntimeException("RiftLib was never initialized! Please read the documentation!");
		}
		return this.particleBuilders;
	}

	@SuppressWarnings("unchecked")
	private List<IResourcePack> getPacks() {
		try {
			Field field = FMLClientHandler.class.getDeclaredField("resourcePackList");
			field.setAccessible(true);

			return (List<IResourcePack>) field.get(FMLClientHandler.instance());
		}
		catch (Exception e) {
			RiftLib.LOGGER.error("Error accessing resource pack list!", e);
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
				catch (Exception ignored) {}
			}
		}

		List<ResourceLocation> locations = new ArrayList<>();

		if (pack instanceof FolderResourcePack folderPack) {
			this.handleFolderResourcePack(folderPack, folder, predicate, locations);
		}
		else if (pack instanceof FileResourcePack filePack) {
			this.handleZipResourcePack(filePack, folder, predicate, locations);
		}

		return locations;
	}

	private void handleFolderResourcePack(FolderResourcePack folderPack, String folder, Predicate<String> predicate, List<ResourceLocation> locations) {
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

					this.enumerateFiles(pathFile, predicate, locations, domain, folder);
				}
			}
			catch (IllegalAccessException e) {
				RiftLib.LOGGER.error(e);
			}
		}
	}

	private void enumerateFiles(File parent, Predicate<String> predicate, List<ResourceLocation> locations, String domain, String prefix) {
		File[] files = parent.listFiles();

		if (files == null) return;

		for (File file : files) {
			if (file.isFile() && predicate.test(file.getName())) {
				locations.add(new ResourceLocation(domain, prefix + "/" + file.getName()));
			}
			else if (file.isDirectory()) {
				this.enumerateFiles(file, predicate, locations, domain, prefix + "/" + file.getName());
			}
		}
	}

	private void handleZipResourcePack(FileResourcePack filePack, String folder, Predicate<String> predicate, List<ResourceLocation> locations) {
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
				RiftLib.LOGGER.error(e);
			}
		}
	}

	private void enumerateZipFile(FileResourcePack filePack, String folder, ZipFile file, Predicate<String> predicate, List<ResourceLocation> locations) {
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
