# UnityKt
A read-only Unity assets extractor for Kotlin based on [AssetStudio](https://github.com/Perfare/AssetStudio) and refer to [UnityPy](https://github.com/K0lb3/UnityPy).

All the C++ code used to decode texture compression comes from [AssetStudio/Texture2DDecoderNative](https://github.com/Perfare/AssetStudio/tree/master/Texture2DDecoderNative).

[7zip-LZMA SDK](https://www.7-zip.org/sdk.html) is used to decompress bundle files.

For the attempt of implementing the algorithm of decoding ETC2_RGB8A texture compression for Java, see [ETC2_RGB8ADecoderForJava](https://github.com/Deficuet/ETC2_RGBA8DecoderForJava)

**If you encounter an error and want to start an new issue, remember to submit the assets files as well.**
## Features
- Load
  - File(s)
  - Folder
    - Files under the folder only, or
    - All reachable files under the folder, recursively.
  - ByteArray, bytes of a asset bundle file. 
    - A `String` is required as the identifier "file name" of this `ByteArray`.
- Loading Configuration
  - When loading file(s)/folder/ByteArray, there is a lambda parameter that can be used to configure loading behaviour.
  - The lambda will be applied **before** loading.
  - `offsetMode` - `MANUAL` or `AUTO`
    - If the `offsetMode` is `AUTO`, all the `\x00` bytes at the beginning of the file will be ignored. The reading of the file will start at first byte that is non-zero.
    - If the `offsetMode` is `MANUAL`, the number of bytes that is ignored depends on `manualIgnoredOffset`.
  - `manualIgnoredOffset`
    - Determine the number of bytes that needs to be ignored at the beginning of the file, no matter the byte is `\x00` or not.
    - This propery works under `OffsetMode.MANUAL` mode only, will be ignored under `AUTO` mode.
    - e.g. If an asset bundle file has 1024 bytes of useless non-zero data at the beginning, The `offsetMode` can be set to `MANUAL` and `manualIgnoredOffset` can be set to 1024 in order to skip 1024 bytes.
- Object Loading
  - The data of Object will not be read until you access any of its properties.
    - However, an access to the properties related to the Object's info will not cause the initialization of the Object.
      - e.g. Its `assetFile` which is a `SerializedFile`, `mPathID`, `unityVersion`, etc. Those are the properties without a `getter`. See [Object class](https://github.com/Deficuet/UnityKt/blob/main/src/main/kotlin/io/github/deficuet/unitykt/data/Object.kt).
- AssetManager & ImportContext
  - For each file loaded, an `ImportContext` with file name will be given. An `ImportContext` contains all objects read from the file.
  - When load files/folder, a list of `ImportContext` will be returned.
  - `AssetManager` contains all objects read from all files, except `AssetBundle` objects.
- Shortcuts
  - See [utils.kt](https://github.com/Deficuet/UnityKt/blob/main/src/main/kotlin/io/github/deficuet/unitykt/utils.kt) and [PPtrUtils.kt](https://github.com/Deficuet/UnityKt/blob/main/src/main/kotlin/io/github/deficuet/unitykt/PPtrUtils.kt)
  - Should always use `PPtr<O>.getObj()` to get the object.
## Installation
---Work in Progress---

## Export
So far the objects that can export data includes:
- Mesh
  - `exportString` - The string with same content as exporting mesh to .obj file.
  - `exportVertices` - The data of lines starts with "v" in .obj file, grouped by Vector3.
  - `exportUV` - The data of lines starts with "vt" in .obj file, grouped by Vector2.
  - `exportNormals` - The data of lines starts with "vn" in .obj file, grouped by Vector3.
  - `exportFaces` - The data of lines starts with "f" in .obj file, grouped by Vector3.
- Texture2D
  - `decompressedImageData` - Image data after decoding. Can be used to create image directly.
  - `image` - A BufferedImage created from `decompressedImageData`. It is usually up-side-down.
## Example
Example for reading and saving an image from a Texture2D object.
```kotlin
import io.github.deficuet.unitykt.*
import io.github.deficuet.unitykt.data.*
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val context: ImportContext = AssetManager.loadFile("C:/path/to/AssetBundle.aab")
    
    //If there is no Texture2D object, IndexOutOfBoundsException will be thrown. 
    //You can consider the function firstOfOrNull<>()
    //The data of this Texture2D object has not been read yet.
    val tex: Texture2D = context.objects.firstObjectOf<Texture2D>()
    
    //The object data will initialize as long as you access its properties.
    ImageIO.write(tex.image, "png", File("C:/whatever/you/want/tex.png"))
}
```
