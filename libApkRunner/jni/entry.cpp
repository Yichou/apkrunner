#include "hook.h"


const JNINativeMethod com_edroid_apkrunner_ApkRunner[] = {
		{ "native_setAppInfo", "([Ljava/lang/String;)V",
				(void*) setAppInfo },

		{"native_kill", "(II)V",
				(void*) my_kill}
};

static int regNative(JNIEnv *env)
{
	jclass clazz = env->FindClass("com/apkrunner/ApkRunner");

	if (clazz == NULL) {
		return JNI_ERR;
	}

	if (env->RegisterNatives(clazz, com_edroid_apkrunner_ApkRunner,
			sizeof(com_edroid_apkrunner_ApkRunner) / sizeof(JNINativeMethod))
			< 0) {
		return JNI_ERR;
	}

	return JNI_OK;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv *env;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		LOGE("ERROR: GetEnv failed\n");
		goto bail;
	}

	if(regNative(env) != JNI_OK) {
		LOGE("ERROR: regNative failed\n");
		goto bail;
	}

	my_init();

	return JNI_VERSION_1_4;

bail:
	return JNI_ERR;
}
