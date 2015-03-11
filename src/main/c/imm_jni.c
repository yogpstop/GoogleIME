#include <windows.h>
#include "com_yogpc_gi_w32_JNIHandler.h"
static HWND hWnd;
static WNDPROC pWndProc;
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
static void pushResult() {
	HIMC  hIMC = ImmGetContext(hWnd);
	LONG  ssiz = ImmGetCompositionString(hIMC, GCS_RESULTSTR , NULL, 0);
	LPTSTR str = malloc(ssiz);
	      ssiz = ImmGetCompositionString(hIMC, GCS_RESULTSTR , str, ssiz);
	// TODO push to java
	ImmReleaseContext(hWnd, hIMC);
	sendNullKeydown();//TODO
}
static void pushComposition() {
	HIMC  hIMC = ImmGetContext(hWnd);
	LONG  size = ImmGetCompositionString(hIMC, GCS_COMPATTR, NULL, 0);
	PBYTE  ret = malloc(size);
	      size = ImmGetCompositionString(hIMC, GCS_COMPATTR, ret, size);
	LONG  ssiz = ImmGetCompositionString(hIMC, GCS_COMPSTR , NULL, 0);
	LPTSTR str = malloc(ssiz);
	      ssiz = ImmGetCompositionString(hIMC, GCS_COMPSTR , str, ssiz);
	// TODO push to java
	ImmReleaseContext(hWnd, hIMC);
}
static void pushCandidate() {
	HIMC  hIMC = ImmGetContext(hWnd);
	LONG  size = ImmGetCandidateList(hIMC, 0, NULL, 0);
	LPCANDIDATELIST cndl = malloc(size);
	      size = ImmGetCandidateList(hIMC, 0, cndl, size);
	// TODO push to java
	ImmReleaseContext(hWnd, hIMC);
}
static void killIME() {
	// TODO check java status
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
LRESULT CALLBACK WndProc(HWND hWnd, UINT msg, WPARAM wp, LPARAM lp) {
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
				;// TODO clear composition FIXME needed?
			return S_OK;
		case WM_IME_ENDCOMPOSITION:
			// TODO clear composition
			return S_OK;
		case WM_IME_NOTIFY:
			switch (wp) {
				case IMN_OPENCANDIDATE:
				case IMN_CHANGECANDIDATE:
					pushCandidate();
					break;
				case IMN_CLOSECANDIDATE:
					;// TODO clear candidate
					break;
			}
			break; // TODO return S_OK on WM_IME_NOTIFY?
		case WM_IME_SETCONTEXT:
			// FIXME review ui mask
			return DefWindowProc(hWnd, msg, wp, lp & ~ISC_SHOWUIALL);
	}
	return CallWindowProc(pWndProc, hWnd, msg, wp, lp);
}
JNIEXPORT void JNICALL Java_com_yogpc_gi_w32_JNIHandler_setHWnd
		(JNIEnv * je, jclass jc, jlong ptr) {
	hWnd = (HWND)(LONG_PTR) ptr;
	pWndProc = (WNDPROC) GetWindowLongPtr(hWnd, GWLP_WNDPROC);
	SetWindowLongPtr(hWnd, GWLP_WNDPROC, (LONG_PTR) WndProc);
	SetActiveWindow(hWnd);
	sendNullKeydown();//TODO
}
static HIMC lastHIMC = NULL;
JNIEXPORT void JNICALL Java_com_yogpc_gi_w32_JNIHandler_linkIME
		(JNIEnv *je, jclass jc) {
	if (!lastHIMC) return;
	HIMC hIMC = ImmGetContext(hWnd);
	ImmAssociateContext(hWnd, lastHIMC);
	lastHIMC = NULL;
	ImmReleaseContext(hWnd, hIMC);
}
JNIEXPORT void JNICALL Java_com_yogpc_gi_w32_JNIHandler_unlinkIME
		(JNIEnv *je, jclass jc) {
	if (lastHIMC) return;
	HIMC hIMC = ImmGetContext(hWnd);
	lastHIMC = ImmAssociateContext(hWnd, 0);
	ImmReleaseContext(hWnd, hIMC);
}
