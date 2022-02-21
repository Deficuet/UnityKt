# UnityKt
[![](https://jitpack.io/v/Deficuet/UnityKt.svg)](https://jitpack.io/#Deficuet/UnityKt)

A read-only Unity assets extractor for Kotlin based on [AssetStudio](https://github.com/Perfare/AssetStudio) and refer to [UnityPy](https://github.com/K0lb3/UnityPy).

All the C++ code used to decode texture compression comes from [AssetStudio/Texture2DDecoderNative](https://github.com/Perfare/AssetStudio/tree/master/Texture2DDecoderNative).

[7zip-LZMA SDK](https://www.7-zip.org/sdk.html) is used to decompress bundle files.

For the attempt of implementing the algorithm of decoding ETC2_RGB8A texture compression for Java, see [ETC2_RGB8ADecoderForJava](https://github.com/Deficuet/ETC2_RGBA8DecoderForJava)

**If you encounter an error and want to start an new issue, remember to submit the assets files as well.**

**Many things are not tested yet.**
## Features
- Load
  - File(s)
  - Folder
    - Files under the folder only, or
    - All reachable files under the folder, recursively.
  - ByteArray, bytes of a asset bundle file. 
    - A `String` is required as the identifier "file name" of this `ByteArray`.
- Loading Configurations
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
  - For each file loaded, an `ImportContext` with file name and directory will be given. An `ImportContext` contains all objects read from the file.
  - When load files/folder, a list of `ImportContext` will be returned.
  - `AssetManager` contains all objects read from all files that are loaded through it, except `AssetBundle` objects.
  - Don't forget to run the method `closeAll()` to close and release the resources used by the manager, or run the static method `closeAll()` in `AssetManager` to release resources used by **all** manager instances.
- Shortcuts
  - See [utils.kt](https://github.com/Deficuet/UnityKt/blob/main/src/main/kotlin/io/github/deficuet/unitykt/utils.kt) and [PPtrUtils.kt](https://github.com/Deficuet/UnityKt/blob/main/src/main/kotlin/io/github/deficuet/unitykt/PPtrUtils.kt)
  - Should always use `PPtr<O>.getObj()` to get the object.
## Installation
Used openJDK 11.0.10 and Kotlin Language 1.5.31.
- ### Gradle
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.Deficuet:UnityKt:v0.0.1-alpha.0'
}
```
- ### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Deficuet</groupId>
    <artifactId>UnityKt</artifactId>
    <version>v0.0.1-alpha.0</version>
</dependency>
```
- ### SBT
```sbt
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.Deficuet" % "UnityKt" % "v0.0.1-alpha.0"	
```
- ### Leiningen
```
:repositories [["jitpack" "https://jitpack.io"]]

:dependencies [[com.github.Deficuet/UnityKt "v0.0.1-alpha.0"]]
```
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
- MonoBehaviour
  - `json` - Export its TypeTree as [JSONObject](https://stleary.github.io/JSON-java/index.html), could be `null`.
  - `jsonString` - Export its TypeTree as `String`.
- TextAsset
  - `text()` - The function that is used to export content in this object as `String`. A `Charset` can be passed as a parameter, by default it is `Charsets.UTF_8`.
- Shader
  - `exportString` - Export as String. **Include** the Spir-V Shader data.
## Example
Example for reading and saving an image from a Texture2D object.
```kotlin
import io.github.deficuet.unitykt.*
import io.github.deficuet.unitykt.data.*
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val manager: AssetManager = AssetManager()
    val context: ImportContext = manager.loadFile("C:/path/to/AssetBundle.aab")
    
    //If there is no Texture2D object, IndexOutOfBoundsException will be thrown. 
    //You can consider the function firstOfOrNull<>()
    //The data of this Texture2D object has not been read yet.
    val tex: Texture2D = context.objects.firstObjectOf<Texture2D>()
    
    //The object data will initialize as long as you access its properties.
    ImageIO.write(tex.image, "png", File("C:/whatever/you/want/tex.png"))
    
    val anotherManager = AssetManager()
    //Loading configurations
    //It will not influence the configurations of previous manager.
    //The files and objects loaded by this manager will not show in previous manager as well.
    anotherManager.loadFolder("C:/foo/bar") {
        offsetMode = OffsetMode.MANUAL
        manualIgnoredOffset = 217
    }
    println(anotherManager.objects.firstObjectOf<Shader>().exportString)
}
```
## Changelog
- ### 2022.02.20
  - Publish on JitPack
- ### 2022.02.19
  - Add the export functions for `MonoBehaviour`, `TextAsset` and `Shader`.
  - Change `AssetManager` from an object singleton to an instance class.
