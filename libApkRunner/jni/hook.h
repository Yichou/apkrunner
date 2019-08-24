#ifndef _HOOK_H_
#define _HOOK_H_

#include <jni.h>
#include <stdio.h>
#include <android/log.h>



#define TAG "apkrunner_jni"
#define LOG_TAG TAG
#define LOG_TPYE
extern int showLog;

#ifdef DEBUG
# define log_info(...) \
		if(showLog) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
# define log_debug(...) \
		if(showLog) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
# define log_warn(...) \
		if(showLog) __android_log_print(ANDROID_LOG_WARN,  TAG, __VA_ARGS__)
# define log_error(...) \
		if(showLog) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
# define log_fatal(...) \
		__android_log_print(ANDROID_LOG_FATAL, TAG, __VA_ARGS__)
#else
# define log_info(...)
# define log_debug(...)
# define log_warn(...)
# define log_error(...)
# define log_fatal(...)
#endif

#define LOGI log_info
#define LOGW log_warn
#define LOGE log_error
#define LOGD log_debug

#define ALOGI log_info
#define ALOGW log_warn
#define ALOGE log_error
#define ALOGD log_debug


#define DIR_PUBLIC 		"Android/data/"
#define String DIR_APPS "apps"
#define DIR_APP_LIB		"lib"
#define _DATA			"/data/data/"


void my_init();
void my_kill(int pid, int sig);
void setAppInfo(JNIEnv* env, jobject thiz, jobjectArray args);


extern "C" uint32_t do_hook(const char *module_path, uint32_t hook_func, const char *symbol_name);

#endif
