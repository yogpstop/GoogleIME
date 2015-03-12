cd src/main/c
javah -cp ../../../target/GoogleIME-1.0.0.jar com.yogpc.gi.w32.JNIHandler
gcc -Wall -Werror -O3 -s -flto -pipe -march=native -shared -o MC-IME.dll -I. -I'/c/Program Files/Java/jdk1.8.0_31/include' -I'/c/Program Files/Java/jdk1.8.0_31/include/win32' imm_jni.c -L'/c/Program Files/Java/jdk1.8.0_31/lib' -ljvm -limm32
