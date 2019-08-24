#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <string.h>
#include <android/log.h>
#include <sys/stat.h>
#include <sys/statfs.h>
#include <sys/types.h>
#include <signal.h>
#include <sys/syscall.h>
#include <sys/system_properties.h>

#include "hook.h"

#include "Utils/SymbolFinder.h"
#include "Utils/StrUtils.h"
#include "Substrate/CydiaSubstrate.h"

#define HOOK_DEF(ret, func, ...) \
  ret (*orig_##func)(__VA_ARGS__); \
  ret new_##func(__VA_ARGS__)

#define HOOK_DEF_O(ret, func, ...) \
  ret (*orig_##func)(__VA_ARGS__);

#define HOOK_DEF_N(ret, func, ...) \
  ret new_##func(__VA_ARGS__)

#define HOOK_SYMBOL(handle, func) \
	hook_function(handle, #func, (void*) new_##func, (void**) &orig_##func)

#define FREE(ptr, org_ptr) { if ((void*) ptr != NULL && (void*) ptr != (void*) org_ptr) { free((void*) ptr); } }
#define FREE1(ptr) if ((void*) ptr != NULL) free((void*) ptr)

#define PATH_MAX_LENGTH 256;


int showLog = 1;
static int 	b_inited = 0;

static char sd_path[64] = {0}; //  end /
static char sys_app_data_path[128] = {0}; //系统安装后apkdata路径 end /
static char app_data_path[128] = {0}; // end /
static char app_data_path_lib[128] = {0}; // end /
static char app_priv_data_path[128] = {0};// end /
static char app_priv_data_path_lib[128] = {0};// end /

static char shell_pkg[64] = {0}; // no /
static char app_pkg[64] = {0};

static void *libc_hd;

void onSoLoaded(const char *name, void *handle);
HOOK_DEF_O(void *, dlsym, void *handle, const char *symbol);

static void hook_function(void *handle, const char *symbol, void *new_func, void **old_func)
{
//	ALOGI("hook_function %s", symbol);

    void *addr = orig_dlsym(handle, symbol);
//	ALOGI("hook_function %s ad=%p", symbol, addr);
    if (!addr) {
    	ALOGE("hook_function %p %s fail", handle, symbol);
    } else {
//    	ALOGI("hook_function so=%p %s=%p old=%p new=%p", handle, symbol, addr, old_func, new_func);
    	MSHookFunction(addr, new_func, old_func);
    	ALOGD("hook_function so=%p %s=%p old=%p new=%p", handle, symbol, addr, old_func, new_func);
    }
}

static int startWith(const char *str, const char *s)
{
	int l = strlen(s);
	//若参数s1 和s2 字符串相同则返回0。s1 若大于s2 则返回大于0 的值，s1 若小于s2 则返回小于0 的值。
	return (l>0 &&  0 == strncasecmp(str, s, l));
}

static const char * findp(const char *p, int s)
{
	int c = 0;
	int b = 0;
	while (*p) {
		if (*p == '/') {
			if (b == 0) {
				c++;
				b = 1;
			}
		} else {
			b = 0;
		}

		if (c == s)
			break;

		p++;
	}

//	send_signal()

	return p;
}

const char * redirect_path(const char *src)
{
	char newPath[256] = {0};

//	ALOGI("==file: %s", src);
	if(startWith(src, sys_app_data_path)) {
		snprintf(newPath, sizeof(newPath), "%s%s", app_data_path, findp(src, 4)+1);
		LOGW(" redirect_path %s -> %s", src, newPath);
		return strdup(newPath);
	}

	return src;
}



//////////////////
HOOK_DEF(int, chmod, const char * path, mode_t mode)
//int chmod_hook(const char * path, mode_t mode)
{
	if(startWith(path, sd_path))
		return 0;
	return orig_chmod(path, mode);
}

HOOK_DEF(int, mkdir, const char * path, mode_t mode)
//int mkdir_hook(const char * path, mode_t mode)
{
	const char *np = redirect_path(path);
	int ret = orig_mkdir(path, mode);
	FREE(np, path);

	return ret;
}

HOOK_DEF(int, stat, const char * path, struct stat * s)
//int stat_hook(const char * path, struct stat * s)
{
	const char *np = redirect_path(path);
	int ret = orig_stat(path, s);
	FREE(np, path);

	return ret;
}

HOOK_DEF(int, statfs, const char *path, struct statfs *s)
//int statfs_hook(const char *path, struct statfs *s)
{
	const char *np = redirect_path(path);
	int ret = orig_statfs(path, s);
	FREE(np, path);

	return ret;
}

/*
void hookso(const char *path)
{
	LOGD("hook so [%s] ", path);

	uint32_t ret = 0;

	ret = do_hook(path, (uint32_t) open_hook, "open");
	LOGI(" > open %s", ret? "suc." : "fail.");

	ret = do_hook(path, (uint32_t) fopen_hook, "fopen");
	LOGI(" > fopen %s", ret? "suc." : "fail.");

	ret = do_hook(path, (uint32_t) chmod_hook, "chmod");
	LOGI(" > chmod %s", ret? "suc." : "fail.");

	ret = do_hook(path, (uint32_t) mkdir_hook, "mkdir");
	LOGI(" > mkdir %s", ret? "suc." : "fail.");

	ret = do_hook(path, (uint32_t) stat_hook, "stat");
	LOGI(" > stat %s", ret? "suc." : "fail.");
	ret = do_hook(path, (uint32_t) stat_hook, "statfs");
	LOGI(" > statfs %s", ret? "suc." : "fail.");
	ret = do_hook(path, (uint32_t) fstat_hook, "fstat");
	LOGI(" > fstat %s", ret? "suc." : "fail.");
	ret = do_hook(path, (uint32_t) lstat_hook, "lstat");
	LOGI(" > lstat %s", ret? "suc." : "fail.");
	ret = do_hook(path, (uint32_t) fstatat_hook, "fstatat");
	LOGI(" > fstatat %s", ret? "suc." : "fail.");
}
*/

void exit_hook(int code)
{
	LOGE("EXIT CALL! %d", code);
	return exit(code);
}

int kill_hook(pid_t pid, int sig)
{
	LOGE("kill %d,%d", pid, sig);

	return kill(pid, sig);
}


// int __openat(int fd, const char *pathname, int flags, int mode);
HOOK_DEF(int, __openat, int fd, const char *pathname, int flags, int mode)
{
//	ALOGI("__openat %s %d %d", pathname, flags, mode);
    const char *np = redirect_path(pathname);
//    int ret = syscall(__NR_openat, fd, np, flags, mode);
    int ret = orig___openat(fd, np, flags, mode);
    FREE(np, pathname);

    return ret;
}

// int __open(const char *pathname, int flags, int mode);
HOOK_DEF(int, __open, const char *pathname, int flags, int mode)
{
    const char *np = redirect_path(pathname);
    int ret = syscall(__NR_open, np, flags, mode);
    FREE(np, pathname);

    return ret;
}

// int (*origin_execve)(const char *pathname, char *const argv[], char *const envp[]);
HOOK_DEF(int, execve, const char *pathname, char *argv[], char *const envp[])
{
    /**
     * CANNOT LINK EXECUTABLE "/system/bin/cat": "/data/app/io.virtualapp-1/lib/arm/libva-native.so" is 32-bit instead of 64-bit.
     *
     * We will support 64Bit to adopt it.
     */
//    ALOGE("execve : %s", pathname);

    int res;
    char *np = pathname;//relocate_path(pathname, &res);
    char buf[512] = {0};
    char nbuf[512] = {0};

    char *ptmp = NULL;
    int i=0;
    int iret;

	char *p = buf;
//    p += sprintf(buf, "%s, ", pathname);
    for(i=0; argv[i]; i++)
    	p += sprintf(p, (argv[i+1]? "%s " : "%s"), argv[i]);
//    for(i=0; envp[i]; i++)
//    	p += sprintf(p, "%s ", envp[i]);
    ALOGE("execve: %s", buf);
    //sh+-c+getprop ro.build.version.release

//    if (strstr(pathname, "dex2oat")) {
//        char **new_envp = build_new_env(envp);
//        int ret = syscall(__NR_execve, redirect_path, argv, new_envp);
//        FREE(redirect_path, pathname);
//        free(new_envp);
//        return ret;
//    }

//    int ret = orig_syscall(__NR_execve, redirect_path, argv, envp);
    int ret = orig_execve(np, argv, envp);
    FREE(np, pathname);
    FREE1(ptmp);

    return ret;
}

HOOK_DEF(void*, dlopen, const char *filename, int flag)
{
    int res;
    const char *redirect_path = filename;//relocate_path(filename, &res);
    void *ret = orig_dlopen(redirect_path, flag);

    onSoLoaded(filename, ret);
//    ALOGD("dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, filename);

    return ret;
}

HOOK_DEF(void*, do_dlopen_V19, const char *filename, int flag, const void *extinfo)
{
    int res;
    const char *redirect_path = filename;//relocate_path(filename, &res);
    void *ret = orig_do_dlopen_V19(redirect_path, flag, extinfo);

    onSoLoaded(filename, ret);
//    ALOGD("do_dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, filename);

    return ret;
}

HOOK_DEF(void*, do_dlopen_V24, const char *filename, int flags, const void *extinfo, void *caller_addr)
{
    int res;
    const char *redirect_path = filename;//relocate_path(name, &res);
    void *ret = orig_do_dlopen_V24(redirect_path, flags, extinfo, caller_addr);

    onSoLoaded(filename, ret);
//    ALOGD("do_dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, filename);

    return ret;
}

//void * dlsym(void*  handle, const char*  symbol);
HOOK_DEF_N(void *, dlsym, void *handle, const char *symbol)
{
	void *ret = orig_dlsym(handle, symbol);
	if(handle == libc_hd)
		ALOGI("dlsym %s from %p addr=%p", symbol, handle, ret);
	return ret;
}

int findSymbol(const char *name, const char *libn, unsigned long *addr)
{
    return find_name(getpid(), name, libn, addr);
}

void hook_dlopen(int api_level)
{
    void *symbol = NULL;
    if (api_level > 23) {
        if (findSymbol("__dl__Z9do_dlopenPKciPK17android_dlextinfoPv", "linker", (unsigned long *) &symbol) == 0) {
            MSHookFunction(symbol, (void *) new_do_dlopen_V24, (void **) &orig_do_dlopen_V24);
        }
    } else if (api_level >= 19) {
        if (findSymbol("__dl__Z9do_dlopenPKciPK17android_dlextinfo", "linker", (unsigned long *) &symbol) == 0) {
            MSHookFunction(symbol, (void *) new_do_dlopen_V19, (void **) &orig_do_dlopen_V19);
        }
    } else {
        if (findSymbol("__dl_dlopen", "linker", (unsigned long *) &symbol) == 0) {
            MSHookFunction(symbol, (void *) new_dlopen, (void **) &orig_dlopen);
        }
    }

	if (findSymbol("__dl_dlsym", "linker", (unsigned long *) &symbol) == 0) {
		MSHookFunction(symbol, (void *) new_dlsym, (void **) &orig_dlsym);
	}
}

/*
static void hookdvm()
{
	uint32_t ret;

	LOGI("hook dvm...");
	ret = do_hook("/system/lib/libdvm.so", (uint32_t) dlopen_hook, "dlopen");
	if (ret == 0) {
		LOGE("ERROR: hook dlopen failed!");
	}

	LOGI("hook art...");
	ret = do_hook("/system/lib/libart.so", (uint32_t) dlopen_hook, "dlopen");
	if (ret == 0) {
		LOGE("ERROR: hook art fail!");
	}

	//会导致结束进程失败
//	ret = do_hook("/system/lib/libandroid_runtime.so", (uint32_t) kill_hook, "kill");
//	if (ret == 0) {
//		LOGE("ERROR: hook kill failed!");
//	}

	hook_exec("/system/lib/libdvm.so");
}
*/

void onSoLoaded(const char *name, void *handle)
{
	ALOGD("onSoLoaded %s %p", name, handle);

	if(str_eq_with(name, "/system/lib64/libc.so")) {
//		fcpy("/sdcard/szlm.so", name);
	}
}

static void getArg(JNIEnv *env, jobjectArray args, int index, char *out)
{
	jstring obj = (jstring)env->GetObjectArrayElement(args, index);
	if(obj) {
		jsize l = env->GetStringUTFLength(obj);
		if(l > 0) {
			env->GetStringUTFRegion(obj, 0, l, out);
			*(out + l + 1) = 0;
		}

		env->DeleteLocalRef(obj);
	}
}

static void checkPath(char *path)
{
	int i = strlen(path);
	if(*(path + i - 1) != '/') {
		*(path + i) = '/';
		*(path + i + 1) = 0;
	}
}

void setAppInfo(JNIEnv* env, jobject thiz, jobjectArray args)
{
//	jstring sdPath[],
//	jstring shellpkg,
//	jstring pkg,
//	jstring dataPath,
//	jstring privDataPath

	getArg(env, args, 0, sd_path);
	checkPath(sd_path);
	LOGD("sd path = %s", sd_path);

	getArg(env, args, 1, shell_pkg);
	LOGD("shell_pkg = %s", shell_pkg);

	getArg(env, args, 2, app_pkg);
	LOGD("app_pkg = %s", app_pkg);

	getArg(env, args, 3, app_data_path);
	checkPath(app_data_path);
	LOGD("app_data_path = %s", app_data_path);

	getArg(env, args, 4, app_priv_data_path);
	checkPath(app_priv_data_path);
	LOGD("app_priv_data_path = %s", app_priv_data_path);

	snprintf(sys_app_data_path, sizeof(sys_app_data_path), "%s%s/",
			_DATA, app_pkg);
	LOGD("sys_app_data_path = %s", sys_app_data_path);
	snprintf(app_data_path_lib, sizeof(app_data_path_lib), "%s%s/", app_data_path, DIR_APP_LIB);
	LOGD("app_data_path_lib = %s", app_data_path_lib);
	snprintf(app_priv_data_path_lib, sizeof(app_priv_data_path_lib), "%s%s/", app_priv_data_path, DIR_APP_LIB);
	LOGD("app_priv_data_path_lib = %s", app_priv_data_path_lib);
}

void my_kill(int pid, int sig)
{
	kill(pid, sig);
}

void my_init()
{
	char buf[100] = {0};

	__system_property_get("ro.build.version.sdk", buf);
	int sdk = atoi(buf);

	void *handle = dlopen("libc.so", RTLD_NOW);
	hook_dlopen(sdk);
	if (handle) {
		ALOGE("-----libc.so hanele=%p", handle);
		libc_hd = handle;

		HOOK_SYMBOL(handle, __openat);
		HOOK_SYMBOL(handle, execve);
//		HOOK_SYMBOL(handle, __system_property_get);
//		HOOK_SYMBOL(handle, popen);
//		HOOK_SYMBOL(handle, fork);
		HOOK_SYMBOL(handle, chmod);
		HOOK_SYMBOL(handle, mkdir);
//		HOOK_SYMBOL(handle, stat);
//		HOOK_SYMBOL(handle, statfs);

		if (sdk <= 20) {
			HOOK_SYMBOL(handle, __open);
		}
	}

//	hook_dlopen(sdk);
	ALOGE("self init done------------------");

//	hookdvm();
//
//	hookso("/system/lib/libjavacore.so");
//	hook_exec("/system/lib/libjavacore.so");
//	hookso("/system/lib/libsqlite.so");
}
