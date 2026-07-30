#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit_only.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit from device makefile.
$(call inherit-product, device/xiaomi/duchamp/device.mk)

# Inherit some common Infinity-X stuff.
$(call inherit-product, vendor/infinity/config/common_full_phone.mk)

PRODUCT_NAME := infinity_duchamp
PRODUCT_DEVICE := duchamp
PRODUCT_MANUFACTURER := Xiaomi
PRODUCT_BRAND := POCO
PRODUCT_MODEL := 2311DRK48G
PRODUCT_SYSTEM_NAME := duchamp_global

PRODUCT_CHARACTERISTICS := nosdcard
PRODUCT_GMS_CLIENTID_BASE := android-xiaomi

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildFingerprint=POCO/duchamp_global/duchamp:14/UP1A.230905.011/OS2.0.209.0.VNLMIXM:user/release-keys \
    DeviceProduct=$(PRODUCT_SYSTEM_NAME)

# Infinity-X Specific Flags
INFINITY_BUILD_TYPE := OFFICIAL
INFINITY_MAINTAINER := Aayush787
WITH_GAPPS := true
BYPASS_CHARGE_SUPPORTED := true
SURFACE_FLINGER_BOOST := true
TARGET_OPTIMIZED_DEXOPT := true
BYPASS_CHARGE_SUPPORTED := true
TARGET_BOOT_ANIMATION_RES := 1080
TARGET_SUPPORTED_REFRESH_RATES := 60,90,120
TARGET_HAS_UDFPS := true
USE_REALITY_ENGINE := false


