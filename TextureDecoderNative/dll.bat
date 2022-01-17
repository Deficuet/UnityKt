cd ./lib
set native=D:\UnityKt\native
g++ -m64 -static -Wl,--add-stdcall-alias -I"openjdk-11.0.10_9\include" -I"openjdk-11.0.10_9\include\win32" -shared -o D:\unitykt_main_jar\TextureDecoder.dll %native%\jni.cpp ./*.cpp -fpermissive
pause