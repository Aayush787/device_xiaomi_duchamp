#!/bin/bash

echo "Clonning Private keys"
croot && git clone https://github.com/ProjectInfinity-X/vendor_infinity-priv_keys-template vendor/infinity-priv/keys
cd vendor/infinity-priv/keys
./keys.sh
cd ../../..

echo "Unlock 4K 60FPS Cam Recording"
cd p*/a*/Aperture
git fetch https://github.com/Nothing-2A/android_packages_apps_Aperture.git
git cherry-pick 9509277efc852ad8bdcce204e0d9cfe104b6d190
cd ../../..

echo "Provide libbase-v34"
cd hardware/lineage/compat
git fetch https://github.com/mt6897-devs/hardware_lineage_compat.git
git cherry-pick e167e38df9e0e8623566b006dfdca0dcc908becf
cd ../../..

echo "Cloning Dolby"
git clone -b 16 https://github.com/Pong-Development/hardware_dolby.git hardware/dolby

echo "Exporting prebuilt headers path"
export TARGET_PREBUILT_KERNEL_HEADERS=device/xiaomi/duchamp-kernel/kernel-uapi-headers.tar.gz
