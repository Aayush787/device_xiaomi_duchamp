echo "Provide libbase-v34"
cd hardware/lineage/compat
git fetch https://github.com/mt6897-devs/hardware_lineage_compat.git
git cherry-pick e167e38df9e0e8623566b006dfdca0dcc908becf
cd ../../..

echo "Exporting prebuilt headers path"
export TARGET_PREBUILT_KERNEL_HEADERS=device/xiaomi/duchamp-kernel/kernel-uapi-headers.tar.gz
