@echo off
set JPRE32=C:\Program Files (x86)\Java\jdk1.8.0_40\
set JPRE64=C:\Program Files\Java\jdk1.8.0_40\
set GPRE32=C:\msys64\mingw32\bin\i686-w64-mingw32-
set GPRE64=C:\msys64\mingw64\bin\x86_64-w64-mingw32-
set GOPT1=-Wall -Werror -O3 -s -flto -pipe -shared -I.
set G32OPT=-I"%JPRE32%\include" -I"%JPRE32%\include\win32" -L"%JPRE32%\lib"
set G64OPT=-I"%JPRE64%\include" -I"%JPRE64%\include\win32" -L"%JPRE64%\lib"
set GOPT2=-ljvm -limm32
set FB=..\ForgeBuilder\target\ForgeBuilder-0.0.1-SNAPSHOT.jar
set L4J=..\launch4j\launch4j.jar
set GI=target\GoogleIME-1.0.0.jar
set NCLS=com.yogpc.gi.w32.JNIHandler
set NSRC=src\main\c\imm_jni.c
set OUT64=src\main\resources\com\yogpc\gi\w32\MC-IME-64.dll
set OUT32=src\main\resources\com\yogpc\gi\w32\MC-IME-32.dll
set MKDIR=src\main\resources\com\yogpc\gi\w32
cd %~dp0
mkdir %MKDIR%
"%JPRE64%bin\java" -jar %FB% .
"%JPRE64%bin\javah" -cp %GI% %NCLS%
"%GPRE64%gcc" %GOPT1% -o %OUT64% %G64OPT% %NSRC% %GOPT2%
"%JPRE32%bin\javah" -cp %GI% %NCLS%
"%GPRE32%gcc" %GOPT1% -o %OUT32% %G32OPT% %NSRC% %GOPT2%
"%JPRE64%bin\java" -jar %FB% .
"%JPRE32%bin\java" -jar %L4J% l4j.xml
DEL *.h
pause
