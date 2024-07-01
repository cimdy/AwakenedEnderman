package com.cimdy.awakenedenderman.Attachment;

import com.cimdy.awakenedenderman.AwakenedEnderman;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AttachRegister {
    public static DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AwakenedEnderman.MODID);

    public static Supplier<AttachmentType<String>> FOLLOW_PLAYER_UUID = ATTACHMENT_TYPES.register(
            "follow_player_uuid", () -> AttachmentType.builder(() -> "Null").serialize(Codec.STRING).build());

    public static Supplier<AttachmentType<Integer>> EXAMINE_TIME = ATTACHMENT_TYPES.register(
            "examine_time", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
}
