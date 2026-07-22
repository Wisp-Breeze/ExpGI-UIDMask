package BetterGI_GameCapture.BitBlt;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.win32.W32APIOptions;

/**
 * DWM API 辅助类，封装 DwmGetWindowAttribute 调用。
 * 对应 C# 中 Vanara.PInvoke.DwmApi.DwmGetWindowAttribute
 */
public class DwmApiHelper {

    /**
     * 自定义 DWM API JNA 接口（JNA 5.13.0 不自带 DwmApi 类）
     */
    public interface DwmApi extends Library {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class, W32APIOptions.DEFAULT_OPTIONS);

        /**
         * DwmGetWindowAttribute(HWND hWnd, DWORD dwAttribute, PVOID pvAttribute, DWORD cbAttribute)
         */
        int DwmGetWindowAttribute(HWND hWnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);
    }

    /**
     * 获取窗口属性矩形（如 DWMWA_EXTENDED_FRAME_BOUNDS）
     *
     * @param hWnd        窗口句柄
     * @param dwAttribute 属性常量（DWMWA_EXTENDED_FRAME_BOUNDS = 9）
     * @param rect        输出矩形
     */
    public static void dwmGetWindowAttribute(HWND hWnd, int dwAttribute, RECT rect) {
        int hr = DwmApi.INSTANCE.DwmGetWindowAttribute(
                hWnd, dwAttribute, rect.getPointer(), rect.size());
        if (hr != 0) {
            throw new RuntimeException("DwmGetWindowAttribute failed, HRESULT: 0x"
                    + Integer.toHexString(hr));
        }
    }
}
