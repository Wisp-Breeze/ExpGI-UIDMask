package GameCapture.BitBlt;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class BitBltRegistryHelper {
    /**
     * <a href="https://github.com/babalae/better-genshin-impact/issues/92">BetterGI 参考文档</a>
     * Win11下 BitBlt截图方式不可用，需要关闭窗口优化功能，这是具体的注册表操作
     * \HKEY_CURRENT_USER\Software\Microsoft\DirectX\UserGpuPreferences
     * DirectXUserGlobalSettings = SwapEffectUpgradeEnable=0;
     *要在游戏启动前设置才有效
     */
    public static void SetDirectXUserGlobalSettings() {
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
        } catch (Exception e) {e.getStackTrace();}
    }
}
