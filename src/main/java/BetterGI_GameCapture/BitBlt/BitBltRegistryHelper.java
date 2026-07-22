package BetterGI_GameCapture.BitBlt;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * BitBlt 注册表辅助类，对应 C# Fischless.GameCapture.BitBlt.BitBltRegistryHelper
 *
 * <p>Win11 下 BitBlt 截图方式不可用，需要关闭窗口优化功能（SwapEffectUpgrade）。
 * 需要在游戏启动前设置才有效。
 *
 * @see <a href="https://github.com/babalae/better-genshin-impact/issues/92">BetterGI Issue #92</a>
 */
public class BitBltRegistryHelper {

    /**
     * 设置 DirectX 用户全局设置，禁用 SwapEffect 升级。
     * 注册表路径：HKEY_CURRENT_USER\Software\Microsoft\DirectX\UserGpuPreferences
     * 值：DirectXUserGlobalSettings = SwapEffectUpgradeEnable=0;
     */
    public static void setDirectXUserGlobalSettings() {
        try {
            final String keyPath = "Software\\Microsoft\\DirectX\\UserGpuPreferences";
            final String valueName = "DirectXUserGlobalSettings";
            final String valueData = "SwapEffectUpgradeEnable=0;";

            Advapi32Util.registrySetStringValue(
                    WinReg.HKEY_CURRENT_USER,
                    keyPath,
                    valueName,
                    valueData
            );
        } catch (Exception e) {
            System.err.println("[BitBltRegistryHelper] Failed to set registry: " + e.getMessage());
        }
    }
}
