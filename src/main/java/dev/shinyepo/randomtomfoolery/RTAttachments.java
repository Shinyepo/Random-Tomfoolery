package dev.shinyepo.randomtomfoolery;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class RTAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RandomTomfoolery.MODID);

    public static final Supplier<AttachmentType<BlockPos>> OVERWORLD_HOME = ATTACHMENT_TYPES.register(
            "overworld_home", () -> AttachmentType.builder(()-> new BlockPos(0,0,0)).serialize(BlockPos.CODEC).build()
    );

    public static final Supplier<AttachmentType<BlockPos>> NETHER_HOME = ATTACHMENT_TYPES.register(
            "nether_home", () -> AttachmentType.builder(()-> new BlockPos(0,0,0)).serialize(BlockPos.CODEC).build()
    );

    public static final Supplier<AttachmentType<BlockPos>> END_HOME = ATTACHMENT_TYPES.register(
            "end_home", () -> AttachmentType.builder(()-> new BlockPos(0,0,0)).serialize(BlockPos.CODEC).build()
    );
}
