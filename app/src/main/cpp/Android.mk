LOCAL_CPP_FEATURES += exceptions

LOCAL_CFLAGS := -pedantic -v -Wno-error=format-security

LOCAL_DISABLE_FATAL_LINKER_WARNINGS := true

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := SNesoid

LOCAL_JNI_SHARED_LIBRARIES := \
	libsnes \
	libemu

include $(BUILD_PACKAGE)

# ============================================================
MY_DIR := $(LOCAL_PATH)

# Also build all of the sub-targets under this one: the shared library.
include $(call all-makefiles-under,$(MY_DIR))
#include $(MY_DIR)/common/Android.mk