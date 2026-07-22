package BetterGI_GameCapture.BitBlt;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * GDI32 函数扩展，声明 BitBlt 截图所需的原生函数
 * 对应 C# 中 Vanara.PInvoke.Gdi32 的相关调用
 */
public interface Gdi32Ext extends Library {

    Gdi32Ext INSTANCE = Native.load("gdi32", Gdi32Ext.class, W32APIOptions.DEFAULT_OPTIONS);

    // ---- Device Capabilities 常量 ----
    int BITSPIXEL = 12;
    int PLANES    = 14;
    int CLIPCAPS  = 36;
    int RASTERCAPS = 38;

    // RC_BITBLT
    int RC_BITBLT = 1;

    // ---- DIB 常量 ----
    int DIB_RGB_COLORS = 0;

    // ---- 光栅操作 ----
    int SRCCOPY = 0x00CC0020;

    // ---- 函数声明 ----

    /**
     * 获取指定设备上下文的像素位数
     */
    int GetDeviceCaps(HDC hdc, int index);

    /**
     * 创建与指定 DC 兼容的内存 DC
     */
    HDC CreateCompatibleDC(HDC hdc);

    /**
     * 创建 DIB Section，获取位图数据指针
     */
    HBITMAP CreateDIBSection(HDC hdc, Structures.BITMAPINFO pbmi,
                             int usage, PointerByReference ppvBits,
                             Pointer hSection, int offset);

    /**
     * 获取 GDI 对象信息（此处用于获取 BITMAP 结构）
     */
    int GetObject(HBITMAP hgdiobj, int cbBuffer, Structures.BITMAP lpvObject);

    /**
     * 选择 GDI 对象到 DC 中
     */
    Pointer SelectObject(HDC hdc, HBITMAP hgdiobj);

    /**
     * 删除 GDI 对象
     */
    boolean DeleteObject(HBITMAP ho);

    /**
     * 删除 DC
     */
    boolean DeleteDC(HDC hdc);

    /**
     * 执行位块传输（BitBlt）
     */
    boolean BitBlt(HDC hdcDest, int xDest, int yDest, int width, int height,
                   HDC hdcSrc, int xSrc, int ySrc, int rop);

    /**
     * 刷新所有待处理的 GDI 操作
     */
    boolean GdiFlush();
}
