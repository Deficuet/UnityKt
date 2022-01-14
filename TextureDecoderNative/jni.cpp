#include "io_github_deficuet_unitykt_extension_TextureDecoder.h"
#include <stdint.h>
#include <iostream>
using namespace std;

uint8_t* JavaByteArrayAsUInt8_T(JNIEnv *env, jbyteArray array) {
    int len = env -> GetArrayLength(array);
    uint8_t *p = new uint8_t[len];
    env -> GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(p));
    return p;
}

JNIEXPORT void JNICALL Java_io_github_deficuet_unitykt_extension_TextureDecoder_decodeETC2A8(
    JNIEnv *env, jclass clazz, jbyteArray data, jint w, jint h, jbyteArray out
) {
    uint8_t *pData = JavaByteArrayAsUInt8_T(env, data);
    cout << (int) *(pData) << endl;
    pData ++;
    cout << (int) *(pData) << endl;
    
}