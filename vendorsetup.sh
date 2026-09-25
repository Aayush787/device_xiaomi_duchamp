#!/bin/bash

echo "Unlock 4K 60FPS Cam Recording"
cd p*/a*/Aperture
git fetch https://github.com/Nothing-2A/android_packages_apps_Aperture.git
git cherry-pick 9509277efc852ad8bdcce204e0d9cfe104b6d190
cd ../../..

echo "Fixup! L2CAP and A2DP offload coex mechanism for MTK"
cd packages/modules/Bluetooth
git fetch https://github.com/mt6897-devs/packages_modules_Bluetooth.git
git cherry-pick bea05a62eef1fcc37e5a10d476580a017a0bd32e
cd ../../..

echo "Provide libbase-v34"
cd hardware/lineage/compat
git fetch https://github.com/mt6897-devs/hardware_lineage_compat.git
git cherry-pick e167e38df9e0e8623566b006dfdca0dcc908becf
cd ../../..

echo "Exporting prebuilt headers path"
export TARGET_PREBUILT_KERNEL_HEADERS=device/xiaomi/duchamp-kernel/kernel-uapi-headers.tar.gz
