package anightdazingzoroark.riftlib.block;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stores client renderer associations and chunk indexes. Register renderers before worlds load; chunk indexes stay on the client game thread.
 * */
@SideOnly(Side.CLIENT)
public class AnimatedBlockRegistry {
    public static final AnimatedBlockRegistry INSTANCE = new AnimatedBlockRegistry();
    private final Map<Block, GeoBlockRenderer<?>> renderers = new IdentityHashMap<>();
    private final Map<World, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Entry>>> worlds = new IdentityHashMap<>();
    private final Map<ResourceLocation, ResourceLocation> modelTextures = new HashMap<>();
    private boolean modelsPrepared;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isRegistered(Block block) {
        return this.renderers.containsKey(block);
    }

    /**
     * Called by GeoBlockRenderer.registerBlockRenderer to associate rendering and apply default light opacity.
     * */
    public void addRenderer(Block block, GeoBlockRenderer<?> renderer) {
        Objects.requireNonNull(block);
        Objects.requireNonNull(renderer);
        if (this.modelsPrepared) throw new IllegalStateException("Animated block renderers must be registered before the ModelRegistryEvent finishes");
        if (this.renderers.containsKey(block)) throw new IllegalArgumentException("Animated block renderer already registered: " + block.getRegistryName());
        IBlockState state = block.getDefaultState();
        if (state.isFullBlock() && state.getLightOpacity() == 255) block.setLightOpacity(0);
        this.renderers.put(block, renderer);
    }

    @Nullable
    public GeoBlockRenderer<?> getRenderer(Block block) {
        return this.renderers.get(block);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasRenderers() {
        return !this.renderers.isEmpty();
    }

    @Nullable
    public ResourceLocation getModelTexture(ResourceLocation location) {
        return this.modelTextures.get(location);
    }

    @Nullable
    public Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Entry>> getChunks(World world) {
        return this.worlds.get(world);
    }

    public void update(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) return;
        GeoBlockRenderer<?> renderer = this.getRenderer(state.getBlock());
        Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Entry>> chunks = this.worlds.get(world);
        if (chunks == null) {
            if (renderer == null) return;
            chunks = new Long2ObjectOpenHashMap<>();
            this.worlds.put(world, chunks);
        }
        long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        Long2ObjectOpenHashMap<Entry> entries = chunks.get(chunkKey);
        if (entries == null) {
            if (renderer == null) return;
            entries = new Long2ObjectOpenHashMap<>();
            chunks.put(chunkKey, entries);
        }
        long key = pos.toLong();
        Entry previous = entries.get(key);
        if (previous != null && previous.state.getBlock() == state.getBlock() && previous.renderer == renderer && renderer != null) {
            if (previous.state != state) previous.renderBoundingBox = null;
            previous.state = state;
            if (previous.holder != null) previous.holder.setBlockState(state);
            return;
        }
        if (previous != null) {
            previous.invalidate();
            entries.remove(key);
        }
        if (renderer != null) entries.put(key, new Entry(pos.toImmutable(), state, renderer));
        if (entries.isEmpty()) chunks.remove(chunkKey);
    }

    /**
     * One scan on load or section replacement; ordinary changes update a single indexed position.
     * */
    public void scan(Chunk chunk, int sectionMask) {
        World world = chunk.getWorld();
        if (!world.isRemote || !this.hasRenderers()) return;
        Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Entry>> chunks = this.worlds.get(world);
        Long2ObjectOpenHashMap<Entry> entries = chunks == null ? null : chunks.get(ChunkPos.asLong(chunk.x, chunk.z));
        if (entries != null) {
            // Refresh existing entries first, including sections which have become empty.
            for (Entry entry : entries.values().toArray(new Entry[0])) {
                if ((sectionMask & 1 << (entry.pos.getY() >> 4)) != 0) this.update(world, entry.pos, chunk.getBlockState(entry.pos));
            }
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            ExtendedBlockStorage section = sections[sectionIndex];
            if ((sectionMask & 1 << sectionIndex) == 0 || section == null || section.isEmpty()) continue;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        IBlockState state = section.get(x, y, z);
                        if (!this.isRegistered(state.getBlock())) continue;
                        pos.setPos((chunk.x << 4) + x, (sectionIndex << 4) + y, (chunk.z << 4) + z);
                        this.update(world, pos, state);
                    }
                }
            }
        }
    }

    public static class Entry {
        private final BlockPos pos;
        private IBlockState state;
        private final GeoBlockRenderer<?> renderer;
        private AnimatedBlockStateHolder holder;
        private AxisAlignedBB renderBoundingBox;
        private boolean valid = true;

        private Entry(BlockPos pos, IBlockState state, GeoBlockRenderer<?> renderer) {
            this.pos = pos;
            this.state = state;
            this.renderer = renderer;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public IBlockState getBlockState() {
            return this.state;
        }

        @Nullable
        public AnimatedBlockStateHolder getHolder() {
            return this.holder;
        }

        public AnimatedBlockStateHolder getOrCreateHolder(World world) {
            if (this.holder == null) this.holder = this.renderer.createHolder(world, this.pos, this.state);
            return this.holder;
        }

        public boolean isValid() {
            return this.valid;
        }

        @Nullable
        public AxisAlignedBB getRenderBoundingBox() {
            return this.renderBoundingBox;
        }

        public void setRenderBoundingBox(AxisAlignedBB renderBoundingBox) {
            this.renderBoundingBox = renderBoundingBox;
        }

        public void invalidate() {
            this.valid = false;
            if (this.holder == null) return;
            this.holder.invalidate();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Events {
        /**
         * Generates one empty particle model for each animated block state and installs its state mapping.
         * */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onModelRegistry(ModelRegistryEvent event) {
            INSTANCE.modelTextures.clear();
            for (Map.Entry<Block, GeoBlockRenderer<?>> rendererEntry : INSTANCE.renderers.entrySet()) {
                Block block = rendererEntry.getKey();
                ResourceLocation registryName = Objects.requireNonNull(block.getRegistryName());
                Map<IBlockState, ModelResourceLocation> stateModels = new IdentityHashMap<>();
                int stateIndex = 0;
                for (IBlockState state : block.getBlockState().getValidStates()) {
                    ResourceLocation location = new ResourceLocation(
                            RiftLib.ModID,
                            "animated_block/" + registryName.getNamespace() + "/" + registryName.getPath() + "/" + stateIndex++
                    );
                    ModelResourceLocation modelLocation = new ModelResourceLocation(location, "normal");
                    stateModels.put(state, modelLocation);
                    INSTANCE.modelTextures.put(modelLocation, rendererEntry.getValue().getParticleTexture(state));
                }
                ModelLoader.setCustomStateMapper(block, mappedBlock -> stateModels);
            }
            INSTANCE.modelsPrepared = true;
        }

        @SubscribeEvent
        public void onChunkUnload(ChunkEvent.Unload event) {
            if (!event.getWorld().isRemote) return;
            Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Entry>> chunks = INSTANCE.worlds.get(event.getWorld());
            if (chunks == null) return;
            Long2ObjectOpenHashMap<Entry> entries = chunks.remove(ChunkPos.asLong(event.getChunk().x, event.getChunk().z));
            if (entries != null) for (Entry entry : entries.values()) entry.invalidate();
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (!event.getWorld().isRemote) return;
            Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Entry>> chunks = INSTANCE.worlds.remove(event.getWorld());
            if (chunks == null) return;
            for (Long2ObjectOpenHashMap<Entry> entries : chunks.values()) {
                for (Entry entry : entries.values()) entry.invalidate();
            }
        }
    }
}
