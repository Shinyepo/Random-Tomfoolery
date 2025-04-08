package dev.shinyepo.randomtomfoolery.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shinyepo.randomtomfoolery.RandomTomfoolery;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class ChunkLoaderSaveData extends SavedData {
    // Map of dimension -> set of chunk positions to be kept loaded
    private Map<ResourceKey<Level>, HashSet<Long>> dimensionChunks;

    public static final Codec<HashSet<Long>> LONG_SET_CODEC = Codec.LONG.listOf()
            .xmap(HashSet::new, ArrayList::new);

    // Codec for serializing/deserializing the data
    public static final Codec<ChunkLoaderSaveData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceKey.codec(Registries.DIMENSION), LONG_SET_CODEC)
                    .fieldOf("dimensionChunks")
                    .forGetter(data -> data.dimensionChunks)
    ).apply(instance, ChunkLoaderSaveData::new));
    // Create new instance of saved data

    public static final SavedDataType<ChunkLoaderSaveData> TYPE = new SavedDataType<>(
            RandomTomfoolery.MODID,
            ChunkLoaderSaveData::create,
            CODEC,
            DataFixTypes.SAVED_DATA_FORCED_CHUNKS
    );
    public static ChunkLoaderSaveData create() {
        var newData = new HashMap<ResourceKey<Level>, HashSet<Long>>();
        newData.put(Level.OVERWORLD, new HashSet<>());
        newData.put(Level.NETHER, new HashSet<>());
        newData.put(Level.END, new HashSet<>());
        return new ChunkLoaderSaveData(newData);
    }

    public static ChunkLoaderSaveData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
    public ChunkLoaderSaveData(Map<ResourceKey<Level>, HashSet<Long>> dimensionChunks) {
        this.dimensionChunks = dimensionChunks;
    }

    public static ChunkLoaderSaveData load(CompoundTag tag, HolderLookup.Provider registries)
    {
        Map<ResourceKey<Level>, HashSet<Long>> map = new HashMap<>();

        for (var key : tag.keySet()) {
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(String.valueOf(tag.getString(key))));
            Optional<ListTag> list = tag.getList(key);
            HashSet<Long> chunks = new HashSet<>();

            list.ifPresent(tags -> tags.forEach(item -> {
                chunks.add(item.asLong().get());
            }));

            map.put(dimension, chunks);
        }

        return new ChunkLoaderSaveData(map);
    }

    public CompoundTag save(Map<ResourceKey<Level>, HashSet<Long>> map) {
        CompoundTag tag = new CompoundTag();

        for (Map.Entry<ResourceKey<Level>, HashSet<Long>> entry : map.entrySet()) {
            String dimensionId = entry.getKey().location().toString(); // e.g., "minecraft:overworld"
            ListTag list = new ListTag();
            for (Long chunk : entry.getValue()) {
                list.add(LongTag.valueOf(chunk));
            }
            tag.put(dimensionId, list);
        }

        return tag;
    }

    // Add chunk to specific dimension
    public void addChunk(ResourceKey<Level> dimension, ChunkPos pos) {
        var chunkList = getDimensionChunks(dimension);
        chunkList.add(pos.toLong());
        setDirty();  // Mark the data as dirty to be saved
    }

    // Get chunk positions for a specific dimension
    public HashSet<Long> getDimensionChunks(ResourceKey<Level> dimension) {
        return dimensionChunks.getOrDefault(dimension, new HashSet<>());
    }

    public Codec<? extends SavedData> codec() {
        return CODEC;
    }

    public void removeChunk(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        var chunkList = getDimensionChunks(dimension);
        chunkList.remove(chunkPos.toLong());
        setDirty();
    }
}
