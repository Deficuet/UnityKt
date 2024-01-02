package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.AvatarConstant;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import kotlin.UInt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

abstract class AvatarFields extends NamedObjectImpl {
    AvatarFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    UInt fmAvatarSize;
    AvatarConstant fmAvatar;
    Map<UInt, List<String>> fmTOS;
}
