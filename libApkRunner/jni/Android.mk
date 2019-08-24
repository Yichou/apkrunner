LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := apkrunner
LOCAL_LDLIBS	:= -llog -latomic
LOCAL_SRC_FILES := entry.cpp hook.c ashman.cpp

LOCAL_C_INCLUDES := $(MAIN_LOCAL_PATH) \
					$(MAIN_LOCAL_PATH)/Utils \
					$(MAIN_LOCAL_PATH)/Substrate

LOCAL_SRC_FILES += Utils/SymbolFinder.cpp \
				   Utils/StrUtils.cpp \
                   Substrate/SubstrateDebug.cpp \
                   Substrate/SubstrateHook.cpp \
                   Substrate/SubstratePosixMemory.cpp
                   
LOCAL_CFLAGS := -Wno-error=format-security -fpermissive -DDEBUG
LOCAL_CFLAGS += -fno-rtti -fno-exceptions


include $(BUILD_SHARED_LIBRARY)
