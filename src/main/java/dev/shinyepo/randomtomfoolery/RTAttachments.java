package dev.shinyepo.randomtomfoolery;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Supplier;

public class RTAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RandomTomfoolery.MODID);

    public static final Codec<HashSet<Long>> LONG_SET_CODEC = Codec.LONG.listOf()
            .xmap(HashSet::new, ArrayList::new);

    public static final Supplier<AttachmentType<BlockPos>> OVERWORLD_HOME = ATTACHMENT_TYPES.register(
            "overworld_home", () -> AttachmentType.builder(() -> new BlockPos(0, 0, 0)).serialize(BlockPos.CODEC).build()
    );

    public static final Supplier<AttachmentType<BlockPos>> NETHER_HOME = ATTACHMENT_TYPES.register(
            "nether_home", () -> AttachmentType.builder(() -> new BlockPos(0, 0, 0)).serialize(BlockPos.CODEC).build()
    );

    public static final Supplier<AttachmentType<BlockPos>> END_HOME = ATTACHMENT_TYPES.register(
            "end_home", () -> AttachmentType.builder(() -> new BlockPos(0, 0, 0)).serialize(BlockPos.CODEC).build()
    );

    public static final Supplier<AttachmentType<HashSet<Long>>> OVERWORLD_CHUNKS = ATTACHMENT_TYPES.register(
            "overworld_chunks", () -> AttachmentType.builder(() -> new HashSet<Long>()).serialize(LONG_SET_CODEC).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<HashSet<Long>>> NETHER_CHUNKS = ATTACHMENT_TYPES.register(
            "nether_chunks", () -> AttachmentType.builder(() -> new HashSet<Long>()).serialize(LONG_SET_CODEC).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<HashSet<Long>>> END_CHUNKS = ATTACHMENT_TYPES.register(
            "end_chunks", () -> AttachmentType.builder(() -> new HashSet<Long>()).serialize(LONG_SET_CODEC).copyOnDeath().build()
    );
}
