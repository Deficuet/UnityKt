#include "io_github_deficuet_unitykt_extension_TextureDecoder.h"
#include "lib/crunch.h"
#include "lib/unitycrunch.h"
#include "lib/bcn.h"
#include "lib/pvrtc.h"
#include "lib/etc.h"
#include "lib/atc.h"
#include "lib/astc.h"

void release(JNIEnv *env, jbyteArray a1, jbyteArray a2, jbyte *p1, jbyte *p2) {
    env -> SetByteArrayRegion(a2, 0, env -> GetArrayLength(a2), p2);
    env -> ReleasePrimitiveArrayCritical(a1, p1, 0);
    env -> ReleasePrimitiveArrayCritical(a2, p2, 0);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeDXT1(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_bc1((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeDXT5(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_bc3((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeBC4(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_bc4((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeBC5(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_bc5((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeBC6(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_bc6((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeBC7(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_bc7((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodePVRTC(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out, jboolean is2bpp
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_pvrtc((uint8_t *) pd, w, h, (uint32_t *) po, is2bpp ? 1 : 0);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeETC1(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_etc1((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeETC2(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_etc2((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeETC2A1(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_etc2a1((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeETC2A8(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_etc2a8((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeATCRGB4(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_atc_rgb4((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeATCRGBA8(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_atc_rgba8((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeASTC(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out, jint blockSize
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_astc((uint8_t *) pd, w, h, blockSize, blockSize, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeEACR(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_eacr((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeEACRSigned(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_eacr_signed((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeEACRG(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_eacrg((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeEACRGSigned(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    jbyte *po = env -> GetByteArrayElements(out, NULL);
    decode_eacrg_signed((uint8_t *) pd, w, h, (uint32_t *) po);
    release(env, data, out, pd, po);
}

uint8_t* unpackCrunch(const void *data, uint32_t dataSize, uint32_t *pResultSize) {
    void *result;
    if (!crunch_unpack_level(static_cast<const uint8_t *>(data), dataSize, 0, &result, pResultSize)) {
        return nullptr;
    }
    return (uint8_t *) result;
}

uint8_t* unpackUnityCrunch(const void *data, uint32_t dataSize, uint32_t *pResultSize) {
    void *result;
    if (!unity_crunch_unpack_level(static_cast<const uint8_t *>(data), dataSize, 0, &result, pResultSize)) {
        return nullptr;
    }
    return (uint8_t *) result;
}

JNIEXPORT jbyteArray JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_unpackCrunch(
    JNIEnv *env, jclass clazz, jbyteArray data
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    uint32_t resultSize = 0;
    uint8_t *pr = unpackCrunch(pd, env -> GetArrayLength(data), &resultSize);
    if (pr == nullptr) {
        return NULL;
    }
    jbyteArray result = env -> NewByteArray(resultSize);
    env -> SetByteArrayRegion(result, 0, resultSize, reinterpret_cast<jbyte *>(pr));
    env -> ReleasePrimitiveArrayCritical(data, pd, 0);
    return result;
}

JNIEXPORT jbyteArray JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_unpackUnityCrunch(
    JNIEnv *env, jclass clazz, jbyteArray data
) {
    jbyte *pd = env -> GetByteArrayElements(data, NULL);
    uint32_t resultSize = 0;
    uint8_t *pr = unpackUnityCrunch(pd, env -> GetArrayLength(data), &resultSize);
    if (pr == nullptr) {
        return NULL;
    }
    jbyteArray result = env -> NewByteArray(resultSize);
    env -> SetByteArrayRegion(result, 0, resultSize, reinterpret_cast<jbyte *>(pr));
    env -> ReleasePrimitiveArrayCritical(data, pd, 0);
    return result;
}