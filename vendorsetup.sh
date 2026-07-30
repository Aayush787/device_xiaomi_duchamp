#!/bin/bash

echo "Clonning Private keys"
croot && git clone https://github.com/ProjectMatrixx/vendor_matrixx-priv_keys-template vendor/enchanted/keys
cd vendor/enchanted/keys
./keys.sh
cd ../../..

echo "Unlock 4K 60FPS Cam Recording"
cd p*/a*/Aperture
git fetch https://github.com/Adarsh0127-Elite/android_packages_apps_Aperture.git
git cherry-pick 9509277efc852ad8bdcce204e0d9cfe104b6d190
cd ../../..

echo "Fixup! L2CAP and A2DP offload coex mechanism for MTK"
cd packages/modules/Bluetooth
git fetch https://github.com/Adarsh0127-Elite/packages_modules_Bluetooth.git
git cherry-pick 92549f3b3ed77c98ca4bc7316bfd2f0662ac11f9
cd ../../..

echo "Clonning Vendor Tree"
git clone -b HOS2.0.209 https://gitlab.com/ash737/vendor_xiaomi_duchamp.git vendor/xiaomi/duchamp

echo "Clonning kernel"
git clone -b bka https://github.com/Evolution-X-Devices/device_xiaomi_duchamp-kernel.git device/xiaomi/duchamp-kernel

echo "Clonning Mediatek Sepolicy"
git clone -b lineage-23.2 https://github.com/mt6897-devs/device_mediatek_sepolicy_vndr.git device/mediatek/sepolicy_vndr

echo "Clonning Mediatek Hardware"
git clone -b lineage-23.2 https://github.com/mt6897-devs/hardware_mediatek.git hardware/mediatek

echo "Clonning Xiaomi Hardware"
git clone -b lineage-23.2 https://github.com/mt6897-devs/hardware_xiaomi.git hardware/xiaomi

echo "Exporting prebuilt headers path"
export TARGET_PREBUILT_KERNEL_HEADERS=device/xiaomi/duchamp-kernel/kernel-uapi-headers.tar.gz
