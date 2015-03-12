#include <windows.h>
#include "com_yogpc_gi_w32_JNIHandler.h"
static HWND hWnd = NULL;
static WNDPROC pWndProc = NULL;
static void sendNullKeydown() {
	if (pWndProc == NULL || hWnd != GetForegroundWindow())
		return;
	INPUT in;
	in.type = INPUT_KEYBOARD;
	in.ki.wVk = VK_BROWSER_REFRESH;
	in.ki.wScan = MapVirtualKey(VK_BROWSER_REFRESH, 0);
	in.ki.dwFlags = KEYEVENTF_EXTENDEDKEY;
	in.ki.time = 0;
	in.ki.dwExtraInfo = 0;//GetMessageExtraInfo()
	SendInput(1, &in, sizeof(INPUT));
}
static JNIEnv *getJE(JavaVM **sr) {
	JavaVM *jv = NULL;
	jsize vms = 0;
	JNI_GetCreatedJavaVMs(&jv, 1, &vms);
	JNIEnv *je = NULL;
	jint ret = (*jv)->GetEnv(jv, (void **) &je, JNI_VERSION_1_6);
	if (ret == JNI_OK) return je;
	(*jv)->AttachCurrentThread(jv, (void **) &je, NULL);
	*sr = jv;
	return je;
}
static void pushResult() {
	HIMC  hIMC = ImmGetContext(hWnd);
	LONG  ssiz = ImmGetCompositionStringW(hIMC, GCS_RESULTSTR , NULL, 0);
	jchar *str = malloc(ssiz);
	      ssiz = ImmGetCompositionStringW(hIMC, GCS_RESULTSTR , str, ssiz);
	JavaVM *sr = NULL; JNIEnv *je = getJE(&sr);
	jstring js = (*je)->NewString(je, str, ssiz / sizeof(jchar));
	jclass jc = (*je)->FindClass(je, "com/yogpc/gi/w32/JNIHandler");
	jmethodID jm = (*je)->GetStaticMethodID(je, jc,
			"cbResult", "(Ljava/lang/String;)V");
	(*je)->CallStaticVoidMethod(je, jc, jm, js);
	if (sr) (*sr)->DetachCurrentThread(sr);
	free(str);//FIXME
	ImmReleaseContext(hWnd, hIMC);
	sendNullKeydown();//FIXME
}
static void pushComposition() {
	HIMC  hIMC = ImmGetContext(hWnd);
	LONG  size = ImmGetCompositionStringW(hIMC, GCS_COMPATTR, NULL, 0);
	jbyte *ret = malloc(size);
	      size = ImmGetCompositionStringW(hIMC, GCS_COMPATTR, ret, size);
	LONG  ssiz = ImmGetCompositionStringW(hIMC, GCS_COMPSTR , NULL, 0);
	jchar *str = malloc(ssiz);
	      ssiz = ImmGetCompositionStringW(hIMC, GCS_COMPSTR , str, ssiz);
	JavaVM *sr = NULL; JNIEnv *je = getJE(&sr);
	jbyteArray jba = (*je)->NewByteArray(je, size / sizeof(jbyte));
	(*je)->SetByteArrayRegion(je, jba, 0, size / sizeof(jbyte), ret);
	jcharArray jca = (*je)->NewCharArray(je, ssiz / sizeof(jchar));
	(*je)->SetCharArrayRegion(je, jca, 0, size / sizeof(jchar), str);
	jclass jc = (*je)->FindClass(je, "com/yogpc/gi/w32/JNIHandler");
	jmethodID jm = (*je)->GetStaticMethodID(je, jc,
			"cbComposition", "([C[B)V");
	(*je)->CallStaticVoidMethod(je, jc, jm, jca, jba);
	if (sr) (*sr)->DetachCurrentThread(sr);
	free(str);//FIXME
	free(ret);//FIXME
	ImmReleaseContext(hWnd, hIMC);
}
static void pushCandidate() {
	// FIXME ImmGetCandidateListCount
	HIMC  hIMC = ImmGetContext(hWnd);
	LONG  size = ImmGetCandidateListW(hIMC, 0, NULL, 0);
	LPCANDIDATELIST cndl = malloc(size);
	      size = ImmGetCandidateListW(hIMC, 0, cndl, size);
	JavaVM *sr = NULL; JNIEnv *je = getJE(&sr);
	jclass sc = (*je)->FindClass(je, "java/lang/String");
	jobjectArray jsa = (*je)->NewObjectArray(je, cndl->dwCount, sc, NULL);
	int i = 0;
	for (; i < cndl->dwCount; i++)
		(*je)->SetObjectArrayElement(je, jsa, i,
				(*je)->NewString(je, (void*)cndl + cndl->dwOffset[i],
				wcslen((void*)cndl + cndl->dwOffset[i])));
	jclass jc = (*je)->FindClass(je, "com/yogpc/gi/w32/JNIHandler");
	jmethodID jm = (*je)->GetStaticMethodID(je, jc,
			"cbCandidate", "([Ljava/lang/String;III)V");
	(*je)->CallStaticVoidMethod(je, jc, jm, jsa,
			cndl->dwSelection, cndl->dwPageStart, cndl->dwPageSize);
	if (sr) (*sr)->DetachCurrentThread(sr);
	free(cndl); //FIXME
	ImmReleaseContext(hWnd, hIMC);
}
static void pushClear(char *n, char *d, int c) {
	JavaVM *sr = NULL; JNIEnv *je = getJE(&sr);
	jclass jc = (*je)->FindClass(je, "com/yogpc/gi/w32/JNIHandler");
	jmethodID jm = (*je)->GetStaticMethodID(je, jc, n, d);
	jvalue *joa = malloc(c * sizeof(jvalue));
	memset(joa, 0, c * sizeof(jvalue));
	(*je)->CallStaticVoidMethodA(je, jc, jm, joa);
	if (sr) (*sr)->DetachCurrentThread(sr);
	free(joa);
}
static void killIME() {
	JavaVM *sr = NULL; JNIEnv *je = getJE(&sr);
	jclass jc = (*je)->FindClass(je, "com/yogpc/gi/TFManager");
	jmethodID jm = (*je)->GetStaticMethodID(je, jc,
			"shouldKill", "()Z");
	jboolean jb = (*je)->CallStaticBooleanMethod(je, jc, jm);
	if (sr) (*sr)->DetachCurrentThread(sr);
	if (!jb) return;
	HIMC hIMC = ImmGetContext(hWnd);
	ImmNotifyIME(hIMC, NI_COMPOSITIONSTR, CPS_CANCEL, 0);
	ImmReleaseContext(hWnd, hIMC);
}
JNIEXPORT jboolean JNICALL Java_com_yogpc_gi_w32_JNIHandler_isOpenIME
		(JNIEnv *je, jclass jc) {
	HIMC hIMC = ImmGetContext(hWnd);
	BOOL b = ImmGetOpenStatus(hIMC);
	ImmReleaseContext(hWnd, hIMC);
	return b;
}
LRESULT CALLBACK WndProc(HWND phWnd, UINT msg, WPARAM wp, LPARAM lp) {
	switch (msg) {
		case WM_IME_STARTCOMPOSITION:
			killIME();
		case WM_IME_CHAR:
			return S_OK;
		case WM_IME_COMPOSITION:
			if (lp & GCS_RESULTSTR)
				pushResult();
			else if (lp & GCS_COMPSTR)
				pushComposition();
			else if (!lp)
				pushClear("cbComposition", "([C[B)V", 2);// FIXME needed?
			return S_OK;
		case WM_IME_ENDCOMPOSITION:
			pushClear("cbComposition", "([C[B)V", 2);
			return S_OK;
		case WM_IME_NOTIFY:
			switch (wp) {
				case IMN_OPENCANDIDATE:
				case IMN_CHANGECANDIDATE:
					pushCandidate();
					break;
				case IMN_CLOSECANDIDATE:
					pushClear("cbCandidate", "([Ljava/lang/String;III)V", 4);
					break;
			}
			break; // FIXME return S_OK on WM_IME_NOTIFY?
		case WM_IME_SETCONTEXT:
			// FIXME review ui mask
			return DefWindowProc(phWnd, msg, wp, lp & ~ISC_SHOWUIALL);
	}
	if (pWndProc) return CallWindowProc(pWndProc, phWnd, msg, wp, lp);
	else          return DefWindowProc(phWnd, msg, wp, lp);
}
JNIEXPORT void JNICALL Java_com_yogpc_gi_w32_JNIHandler_setHWnd
		(JNIEnv * je, jclass jc, jlong ptr) {
	if (!ptr) return;
	hWnd = (HWND)(LONG_PTR) ptr;
	WNDPROC tWndProc = (WNDPROC) GetWindowLongPtr(hWnd, GWLP_WNDPROC);
	if (tWndProc != WndProc && tWndProc) pWndProc = tWndProc;
	SetWindowLongPtr(hWnd, GWLP_WNDPROC, (LONG_PTR) WndProc);
	SetActiveWindow(hWnd);
	sendNullKeydown();//FIXME
}
static HIMC lastHIMC = NULL;
JNIEXPORT void JNICALL Java_com_yogpc_gi_w32_JNIHandler_linkIME
		(JNIEnv *je, jclass jc) {
	if (!lastHIMC || !hWnd) return;
	ImmAssociateContext(hWnd, lastHIMC);
	lastHIMC = NULL;
}
JNIEXPORT void JNICALL Java_com_yogpc_gi_w32_JNIHandler_unlinkIME
		(JNIEnv *je, jclass jc) {
	if (!hWnd) return;
	HIMC hIMC = ImmAssociateContext(hWnd, 0);
	if (hIMC) lastHIMC = hIMC;
}
