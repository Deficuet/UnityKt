package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.Sprite;
import io.github.deficuet.unitykt.classes.SpriteAtlasData;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

abstract class SpriteAtlasFields extends NamedObjectImpl {
    SpriteAtlasFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtrImpl<Sprite>[] fmPackedSprites;
    Map<Pair<UUID, Long>, SpriteAtlasData> fmRenderDataMap;
    Boolean fmIsVariant;
}
