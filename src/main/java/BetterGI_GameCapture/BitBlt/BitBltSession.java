package BetterGI_GameCapture.BitBlt;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.Pointer;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.ConcurrentSkipListSet;

public abstract class BitBltSession implements AutoCloseable { //记得拆abstract
    // 窗口句柄
    private /*final*/ WinDef.HWND _hWnd;

    private final Object _lockObject = new Object();

    // 位图句柄
    private WinDef.HBITMAP _hBitmap;

    // 位图数据指针，这个指针会在位图释放时自动释放
    private Pointer _bitsPtr;

    // 位图数据一行字节数
    private /*final*/ int _stride;

    // 缓冲区 CompatibleDC
    private WinDef.HDC _hdcDest;

    // 来源DC
    private WinDef.HDC _hdcSrc;

    // 旧位图，析构时一起释放掉
    private WinDef.HBITMAP _oldBitmap;

    // Bitmap buffer 大小
    private /*final*/ int _bufferSize;


}
