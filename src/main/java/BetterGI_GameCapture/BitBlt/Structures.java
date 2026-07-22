package BetterGI_GameCapture.BitBlt;

import com.sun.jna.Structure;

import java.util.List;

/**
 * GDI 位图相关 JNA 结构体，对应 C# 中 Vanara.PInvoke 的 Gdi32.BITMAPINFOHEADER / BITMAPINFO / BITMAP
 */
public class Structures {

    /**
     * BITMAPINFOHEADER 结构体（40 字节）
     */
    @Structure.FieldOrder({
            "biSize", "biWidth", "biHeight", "biPlanes", "biBitCount",
            "biCompression", "biSizeImage", "biXPelsPerMeter", "biYPelsPerMeter",
            "biClrUsed", "biClrImportant"
    })
    public static class BITMAPINFOHEADER extends Structure {
        public int biSize;          // DWORD -> uint32
        public int biWidth;         // LONG  -> int32
        public int biHeight;        // LONG  -> int32
        public short biPlanes;      // WORD  -> uint16
        public short biBitCount;    // WORD  -> uint16
        public int biCompression;   // DWORD -> uint32
        public int biSizeImage;     // DWORD -> uint32
        public int biXPelsPerMeter; // LONG  -> int32
        public int biYPelsPerMeter; // LONG  -> int32
        public int biClrUsed;       // DWORD -> uint32
        public int biClrImportant;  // DWORD -> uint32

        public BITMAPINFOHEADER() {
            super();
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of(
                    "biSize", "biWidth", "biHeight", "biPlanes", "biBitCount",
                    "biCompression", "biSizeImage", "biXPelsPerMeter", "biYPelsPerMeter",
                    "biClrUsed", "biClrImportant"
            );
        }
    }

    /**
     * BITMAPINFO 结构体（BITMAPINFOHEADER + 可选颜色表）
     * 对于 24 位 BI_RGB，颜色表为空
     */
    @Structure.FieldOrder({"bmiHeader"})
    public static class BITMAPINFO extends Structure {
        public BITMAPINFOHEADER bmiHeader = new BITMAPINFOHEADER();

        public BITMAPINFO() {
            super();
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("bmiHeader");
        }
    }

    /**
     * BITMAP 结构体，用于 GetObject 获取位图信息
     */
    @Structure.FieldOrder({"bmType", "bmWidth", "bmHeight", "bmWidthBytes", "bmPlanes", "bmBitsPixel", "bmBits"})
    public static class BITMAP extends Structure {
        public int bmType;
        public int bmWidth;
        public int bmHeight;
        public int bmWidthBytes;
        public short bmPlanes;
        public short bmBitsPixel;
        public com.sun.jna.Pointer bmBits;

        public BITMAP() {
            super();
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("bmType", "bmWidth", "bmHeight", "bmWidthBytes", "bmPlanes", "bmBitsPixel", "bmBits");
        }
    }
}
