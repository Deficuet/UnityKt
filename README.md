# UnityKt
[![](https://jitpack.io/v/Deficuet/UnityKt.svg)](https://jitpack.io/#Deficuet/UnityKt)

A read-only Unity assets extractor for Kotlin based on [AssetStudio](https://github.com/Perfare/AssetStudio) and refers to [UnityPy](https://github.com/K0lb3/UnityPy).

All the C++ code used to decode texture compression come from [AssetStudio/Texture2DDecoderNative](https://github.com/Perfare/AssetStudio/tree/master/Texture2DDecoderNative).

[7zip-LZMA SDK](https://www.7-zip.org/sdk.html) is included to decompress bundle files.

**If you encounter an error and want to start a new issue, remember to submit the assets files as well.**

**Many things are not tested yet.**
## Features
- Load
  - File(s)
  - Folder
    - Files under the folder only, or
    - All reachable files under the folder, recursively.
  - ByteArray, bytes of an asset bundle file. 
    - A `String` is required as the identifier "file name" of this `ByteArray`.
- UnityAssetManager
  - The entry point of loading object. It contains all objects read from all files that are loaded through it, except `AssetBundle` objects. 
  - Use `UnityAssetManager.new(...)` with three parameters `assetRootFolder`, `readerConfig` and `debugOutput` to get an instance. 
    - `assetRootFolder` is optional (nullable), and it can be a `java.io.File`, `java.nio.file.Path` or just a `String`. When present, it will be used by `PPtr` to look for object according to the `mDependencies` in the `AssetBundle` object of the file.
    - `readerConfig` is the loading configuration and is also optional.
    - `debugOutput` is a lambda function takes a `String`. It is used to redirect the debug information. By default, it does nothing.
- Loading Configurations
  - When loading file(s)/folder/ByteArray, an optional `ReaderConfig` can also be passed to override the configuration given to the manager, or the configuration given to the manager will be used by default. 
  - The configuration will be applied **before** loading.
  - `offsetMode` - `MANUAL` or `AUTO`; Default: `MANUAL`
    - If the `offsetMode` is `AUTO`, all the `\x00` bytes at the beginning of the file will be ignored. The reading of the file will start at the first non-zero byte. The config `manualOffset` is ignored under this mode.
    - If the `offsetMode` is `MANUAL`, the number of bytes that is ignored depends on `manualOffset`.
  - `manualOffset`: `Int`; Default: `0`
    - Determine the number of bytes that needs to be ignored at the beginning of the file, no matter the byte is `\x00` or not.
    - This property works under `OffsetMode.MANUAL` mode only, will be ignored under `AUTO` mode.
    - e.g. If an asset bundle file has 1024 bytes of useless non-zero data at the beginning, these bytes can be skipped by setting `offsetMode` to `MANUAL` and setting `manualOffset` to 1024.
- ImportContext
  - For each file loaded, an `ImportContext` with file name and directory will be returned. An `ImportContext` contains all objects read from the file.
  - When load files/folder, an array of `ImportContext` will be returned.
  - Don't forget to call the method `close()` to close and release the resources used by the manager, or call the static method `closeAll()` in `UnityAssetManager` to release resources used by **all** manager instances. It implements `Closeable` interface, so the function `use` for `Closeable` can be used.
- Object Reading
  - The data of a UnityObject will not be read until you access any of its properties.
    - However, access to the fields related to the UnityObject's metadata will not cause the initialization of the UnityObject.
      - e.g. Its `assetFile` which is a `SerializedFile`, `mPathID`, `unityVersion`, etc. Those are the fields enclosed in the metadata region. See [UnityObject class](https://github.com/Deficuet/UnityKt/blob/main/src/main/kotlin/io/github/deficuet/unitykt/classes/UnityObject.kt).
- Shortcuts
  - See [utils.kt](https://github.com/Deficuet/UnityKt/blob/main/src/main/kotlin/io/github/deficuet/unitykt/utils.kt) and [PPtr.kt](https://github.com/Deficuet/UnityKt/blob/main/src/main/kotlin/io/github/deficuet/unitykt/classes/PPtr.kt)
  - Should always use `PPtr<T>.safeGetObj()` or `PPtr<T>.getObj()` to get the object, or use `PPtr<*>.safeGetObjAs<T: UnityObject>()` to get and cast.

## Export
So far the objects that can export data includes:
- Mesh
  - `exportString` - A string with the same content as exporting mesh to .obj file using [AssetStudio](https://github.com/Perfare/AssetStudio).
  - `exportVertices` - The data of lines starts with "v" in the .obj file, grouped by Vector3.
  - `exportUV` - The data of lines starts with "vt" in the .obj file, grouped by Vector2. Only the data `mUV0` is exported.
  - `exportNormals` - The data of lines starts with "vn" in the .obj file, grouped by Vector3.
  - `exportFaces` - The data of lines starts with "f" in the .obj file, grouped by Vector3.
- Texture2D
  - `getRawData` - Returns compressed texture data in `ByteArray`.
  - `getDecompressedData` - Image data as `ByteArray` after decoding. Can be used to create image directly. The color channels are `BGRA`. The size of the array is `mWidth * mHeight * 4`.
  - `getImage` - A BufferedImage created from the decompressed data. **It is usually up-side-down**.
  - If the format of the texture is unsupported, both functions will return `null`.
- Sprite
  - `getImage` - An BufferedImage cropped from a `Texture2D` image. Will return `null` if the `Texture2D` object is not found or the format is unsupported.
    - The packing mode `SpritePackingMode.Tight` is not supported yet.
- TextAsset
  - `text(charset)` - This function is used to export content in this object as `String`. A `Charset` can be passed as a parameter, by default it is `Charsets.UTF_8`.
- Shader
  - `exportString` - Export as String. **Include** the Spir-V Shader data (experimental).
- `AudioClip` and `VideoClip`
  - `getRawData` - To get the raw data `ByteArray`. The export functions are not implemented yet. 
- All objects
  - `typeTreeJson` - A [JSONObject](https://stleary.github.io/JSON-java/org/json/JSONObject.html) contains all the properties they have, including those properties that is not implemented yet. The json could be `null`.
    - May throw exception for some types of object like `Font` because of some unsolved problems.

## Build/Installation
Used openJDK 11.0.10 and Kotlin Language 1.8.21.

<ins>**Note:**</ins> The decoding for texture compression of `Texture2D` that needs to call native library is available on <ins>Windows 64-bit JVM</ins> **only**.
- ### Gradle
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.Deficuet:UnityKt:{version}'
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
    <version>{version}</version>
</dependency>
```
- ### SBT
```sbt
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.Deficuet" % "UnityKt" % "{version}"	
```
- ### Leiningen
```
:repositories [["jitpack" "https://jitpack.io"]]

:dependencies [[com.github.Deficuet/UnityKt "{version}"]]
```

## Example
Example for reading and saving an image from a Texture2D object.
```kotlin
import io.github.deficuet.unitykt.*
import io.github.deficuet.unitykt.classes.*
import java.io.File
import javax.imageio.ImageIO

fun main() {
    UnityAssetManager.new().use {
        val context: ImportContext = it.loadFile("C:/path/to/AssetBundle.aab")
        
        //If there is no Texture2D object, an IndexOutOfBoundsException will be thrown. 
        //Use the function firstOfOrNull<>() if the existence of the object is not guaranteed.
        //The data of this Texture2D object has not been read yet.
        val tex: Texture2D = context.objects.firstObjectOf<Texture2D>()
        
        //The object data will initialize as long as you access its properties.
        tex.getImage()?.let {
            ImageIO.write(it, "png", File("C:/whatever/you/want/name.png"))
        }
        
        //The manager will automatically close under the scope of 'use' function.
    }
    
    // Instantiate another manager
    UnityAssetManager.new(
        "C:/path/to/asset/system/root/folder",
        ReaderConfig(OffsetMode.AUTO)
    ).use {
        //Loading configurations
        //The new config with mode `MANUAL` and offset `217` will be used instead of the config with mode `AUTO` given to the manager.
        //The files and objects loaded by this manager will not show in previous manager.
        val context = it.loadFolder("C:/foo/bar", ReaderConfig(OffsetMode.MANUAL, 217))
        //The manager holds all the objects loaded through it, except those AssetBundle objects, their PathID is usually (always?) 1.
        println(it.objects.firstObjectOf<Shader>().exportString)

        context.objectMap.getAs<Material>(4054671897103428721)      //Map<Long, UnityObject>.getAs<T: UnityObject>(pathId: Long): T
                                                                    //                  or  .safeGetAs<T: UnityObject>(pathId: Long): T?
            .mSavedProperties.mTexEnvs.values.first()[0].mTexture   //PPtr<Texture>
            .safeGetObj()       // if PPtr can not find the object under the same assetFile (SerializedFile) nor others loaded assetFiles
                                // Then it will look for the file under the directory "C:/path/to/asset/system/root/folder" which was
                                // given to the manager. The file name comes from the mDependencies property of the AssetBundle object
                                // in the same file as this Material object being in. The target file will be loaded using the same
                                // reader config passed to the manager. Finally, PPtr will try to find the object in the new loaded files.
            ?.safeCast<Texture2D>()?.getImage()?.let { image ->
                ImageIO.write(image, "png", File("C:/whatever/you/want/name.png"))
            }
    }
}
```
## Changelog
- ### 2024.01.02
  - Refactor
  - Renamed `Object` to `UnityObject`.
  - Fixed some errors and incorrect behaviors in `Matrix4x4` and `PPtr`.
  - Removed implementation of `Font` object because of some unsolved errors.
  - Extended some fields for `GameObject` and `VideoClip`.
  - ...many other things.
- ### 2022.12.25
  - Create Canvas object, tested on version 2018.4.34f1, not guaranteed to be stable on other versions. 
- ### 2022.12.23
  - Catch up on the newest version of AssetStudio. Try to optimize memory use, re-design some structures. 
- ### 2022.02.20
  - Publish on JitPack
- ### 2022.02.19
  - Add the export functions for `MonoBehaviour`, `TextAsset` and `Shader`.
  - Change `AssetManager` from an object singleton to an instance class.
