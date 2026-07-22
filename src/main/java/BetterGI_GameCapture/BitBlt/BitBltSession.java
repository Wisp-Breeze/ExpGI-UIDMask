package BetterGI_GameCapture.BitBlt;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.PointerByReference;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.concurrent.ConcurrentLinkedDeque;

import static BetterGI_GameCapture.BitBlt.Gdi32Ext.INSTANCE;

/**
 * BitBlt 截图会话，管理 GDI 资源链。
 * 对应 C# Fischless.GameCapture.BitBlt.BitBltSession
 *
 * <p>资源链：GetDC → CreateCompatibleDC → CreateDIBSection → SelectObject
 */
public class BitBltSession implements AutoCloseable {

    // 窗口句柄
    private final HWND hWnd;

    private final Object lockObject = new Object();

    // 位图句柄
    private HBITMAP hBitmap;

    // 位图数据指针（随位图释放自动释放）
    private Pointer bitsPtr;

    // 位图数据一行字节数（可能含对齐填充）
    private final int stride;

    // 缓冲区 CompatibleDC
    private HDC hdcDest;

    // 来源 DC
    private HDC hdcSrc;

    // 旧位图（析构时先选回再释放）
    private HBITMAP oldBitmap;

    // Bitmap buffer 大小
    private final int bufferSize;

    // Bitmap 内存池
    private final ConcurrentLinkedDeque<Pointer> bufferPool = new ConcurrentLinkedDeque<>();

    // 窗口原宽高
    private final int width;
    private final int height;

    /**
     * 不是所有失效情况都能被检测到
     */
    public boolean isInvalid() {
        synchronized (lockObject) {
            return hWnd == null || hWnd.getPointer() == null
                    || hdcSrc == null || hdcDest == null
                    || hBitmap == null || bitsPtr == null || bitsPtr == Pointer.NULL;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BitBltSession(HWND hWnd, int w, int h) {
        if (hWnd == null || hWnd.getPointer() == null) {
            throw new RuntimeException("hWnd is invalid");
        }
        this.hWnd = hWnd;

        if (w <= 0 || h <= 0) {
            throw new RuntimeException("Invalid width or height");
        }

        this.width = w;
        this.height = h;

        synchronized (lockObject) {
            try {
                // 获取窗口 DC
                hdcSrc = User32.INSTANCE.GetDC(hWnd);
                if (hdcSrc == null) {
                    throw new RuntimeException("Failed to get DC for window");
                }

                // 检查是否支持 BitBlt
                int rasterCaps = INSTANCE.GetDeviceCaps(hdcSrc, INSTANCE.RASTERCAPS);
                if ((rasterCaps & INSTANCE.RC_BITBLT) == 0) {
                    throw new RuntimeException("BitBlt not supported");
                }

                // 检查像素位数
                int pixelBits = INSTANCE.GetDeviceCaps(hdcSrc, INSTANCE.BITSPIXEL);
                if (pixelBits != 32 && pixelBits != 24) {
                    throw new RuntimeException("BitBlt only support 24 or 32 bit pixel color");
                }

                // 检查颜色平面数
                int planes = INSTANCE.GetDeviceCaps(hdcSrc, INSTANCE.PLANES);
                if (planes > 1) {
                    throw new RuntimeException("BitBlt only support 1 plane");
                }

                // 检查剪切能力
                int clipCaps = INSTANCE.GetDeviceCaps(hdcSrc, INSTANCE.CLIPCAPS);
                if (clipCaps == 0) {
                    throw new RuntimeException("Device does not support clipping");
                }

                // 创建兼容 DC
                hdcDest = INSTANCE.CreateCompatibleDC(hdcSrc);
                if (hdcDest == null) {
                    throw new RuntimeException("Failed to create CompatibleDC");
                }

                // 构造 BITMAPINFO（24 位 BGR，自上而下）
                Structures.BITMAPINFO bmi = new Structures.BITMAPINFO();
                bmi.bmiHeader.biSize = bmi.bmiHeader.size(); // 40
                bmi.bmiHeader.biWidth = width;
                bmi.bmiHeader.biHeight = -height; // 负值 = Top-down image
                bmi.bmiHeader.biPlanes = 1;
                bmi.bmiHeader.biBitCount = 24;
                bmi.bmiHeader.biCompression = 0; // BI_RGB，内存里是 BGR
                bmi.bmiHeader.biSizeImage = 0;

                // 创建 DIB Section
                PointerByReference ppvBits = new PointerByReference();
                hBitmap = INSTANCE.CreateDIBSection(
                        hdcDest, bmi, INSTANCE.DIB_RGB_COLORS,
                        ppvBits, null, 0
                );

                bitsPtr = ppvBits.getValue();
                if (hBitmap == null || bitsPtr == null || bitsPtr == Pointer.NULL) {
                    if (hBitmap != null) {
                        INSTANCE.DeleteObject(hBitmap);
                    }
                    throw new RuntimeException("Failed to create DIB section");
                }

                // 获取 BITMAP 对象信息
                Structures.BITMAP bitmap = new Structures.BITMAP();
                INSTANCE.GetObject(hBitmap, bitmap.size(), bitmap);

                if (bitmap.bmPlanes != 1 || bitmap.bmBitsPixel != 24) {
                    throw new RuntimeException("Unsupported bitmap format");
                }

                stride = bitmap.bmWidthBytes;
                bufferSize = bitmap.bmWidth * bitmap.bmHeight * 3;

                // 选入 DC
                HBITMAP result = new HBITMAP(INSTANCE.SelectObject(hdcDest, hBitmap));
                if (result == null || result.getPointer() == null) {
                    throw new RuntimeException("Failed to select object");
                }
                oldBitmap = result;

            } catch (Exception e) {
                releaseResources();
                throw new RuntimeException("BitBltSession init failed", e);
            } finally {
                INSTANCE.GdiFlush();
            }
        }
    }

    @Override
    public void close() {
        synchronized (lockObject) {
            releaseResources();
        }
    }

    /**
     * 调用 GDI BitBlt 复制到缓冲区，返回新 Mat
     */
    public Mat getImage() {
        synchronized (lockObject) {
            // 截图
            boolean success = INSTANCE.BitBlt(
                    hdcDest, 0, 0, width, height,
                    hdcSrc, 0, 0, INSTANCE.SRCCOPY
            );
            if (!success || !INSTANCE.GdiFlush()) {
                return null;
            }

            // 创建新 Mat 并将 GDI 位图数据写入其原生缓冲区
            int step = width * 3;
            Mat mat = new Mat(height, width, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
            // data() 返回 BytePointer，指向 Mat 的原生数据缓冲区
            BytePointer matDataPtr = mat.data();

            if (stride == step) {
                // stride 与 step 一致，一次性复制
                byte[] data = bitsPtr.getByteArray(0, bufferSize);
                matDataPtr.position(0).put(data);
            } else {
                // stride 与 step 不同，逐行复制
                for (int i = 0; i < height; i++) {
                    byte[] rowBuf = bitsPtr.getByteArray((long) stride * i, step);
                    matDataPtr.position((long) step * i).put(rowBuf);
                }
            }

            return mat;
        }
    }

    /**
     * 在加锁环境下释放所有 GDI 资源
     */
    private void releaseResources() {
        INSTANCE.GdiFlush();

        // 先选回旧位图再释放 hBitmap
        if (oldBitmap != null && oldBitmap.getPointer() != null) {
            INSTANCE.SelectObject(hdcDest, oldBitmap);
            oldBitmap = null;
        }

        if (hBitmap != null) {
            INSTANCE.DeleteObject(hBitmap);
            hBitmap = null;
        }

        if (hdcDest != null) {
            INSTANCE.DeleteDC(hdcDest);
            hdcDest = null;
        }

        if (hdcSrc != null) {
            User32.INSTANCE.ReleaseDC(hWnd, hdcSrc);
            hdcSrc = null;
        }

        bitsPtr = null;

        // 释放内存池中的所有缓冲区
        for (Pointer buffer : bufferPool) {
            Native.free(Pointer.nativeValue(buffer));
        }
        bufferPool.clear();
    }
}
