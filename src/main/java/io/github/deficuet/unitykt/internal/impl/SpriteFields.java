package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.SpriteAtlas;
import io.github.deficuet.unitykt.classes.SpriteRenderData;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.math.Rectangle;
import io.github.deficuet.unitykt.math.Vector2;
import io.github.deficuet.unitykt.math.Vector4;
import io.github.deficuet.unitykt.pptr.PPtr;
import kotlin.Pair;
import kotlin.UInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

abstract class SpriteFields extends NamedObjectImpl{
    SpriteFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    Rectangle fmRect;
    Vector2 fmOffset;
    Vector4 fmBorder;
    Float fmPixelsToUnits;
    Vector2 fmPivot;
    UInt fmExtrude;
    Boolean fmIsPolygon;
    Pair<UUID, Long> fmRenderDataKey;
    String[] fmAtlasTags;
    @Nullable PPtr<SpriteAtlas> fmSpriteAtlas;
    SpriteRenderData fmRD;
    Vector2[][] fmPhysicsShape;
}
