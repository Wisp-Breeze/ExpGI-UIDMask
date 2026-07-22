package BetterGI_GameCapture.BitBlt;

import BetterGI_GameCapture.GameCaptureFrame;
import BetterGI_GameCapture.IGameCapture;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * BitBlt 截图实现，对应 C# Fischless.GameCapture.BitBlt.BitBltCapture
 *
 * <p>使用读写锁保证线程安全，周期性检查窗口尺寸变化，
 * 首次截图失败时递归重试一次。
 */
public class BitBltCapture implements IGameCapture {

    private volatile boolean capturing;
    private final Stopwatch sizeCheckTimer = new Stopwatch();
    private final ReentrantReadWriteLock lockSlim = new ReentrantReadWriteLock();
    private volatile long hWndValue; // native 窗口句柄值
    private BitBltSession session;
    private RECT captureRect;
    private volatile boolean lastCaptureFailed;

    @Override
    public boolean isCapturing() {
        return capturing;
    }

    @Override
    public void start(long hWnd, Map<String, Object> settings) {
        // 可选：Win11 BitBlt 修复
        if (settings != null && Boolean.TRUE.equals(settings.get("autoFixWin11BitBlt"))) {
            BitBltRegistryHelper.setDirectXUserGlobalSettings();
        }

        lockSlim.writeLock().lock();
        try {
            hWndValue = hWnd;
            if (hWndValue == 0) {
                return;
            }

            if (session != null) {
                session.close();
                session = null;
            }
            capturing = true;
        } finally {
            lockSlim.writeLock().unlock();
        }

        checkSession();
    }

    @Override
    public GameCaptureFrame capture() {
        return capture(false);
    }

    /**
     * 检查窗口大小，如果改变则更新截图尺寸
     */
    private void checkSession() {
        // 如果有写锁等待，或无法在 0.5 秒内获取写锁，直接返回
        if (lockSlim.hasQueuedThreads() || !lockSlim.writeLock().tryLock()) {
            return;
        }

        try {
            HWND hWnd = new HWND(Pointer.createConstant(hWndValue));

            // 上次截图失败则重置会话
            if (session != null && (session.isInvalid() || lastCaptureFailed)) {
                session.close();
                session = null;
            }

            // 获取客户区矩形
            RECT clientRect = new RECT();
            if (!User32.INSTANCE.GetClientRect(hWnd, clientRect)
                    || (clientRect.left == 0 && clientRect.top == 0
                    && clientRect.right == 0 && clientRect.bottom == 0)) {
                // 窗口获取不到或最小化
                if (session != null) {
                    session.close();
                    session = null;
                }
                captureRect = null;
                return;
            }

            int width = clientRect.right - clientRect.left;
            int height = clientRect.bottom - clientRect.top;

            // 获取窗口扩展帧边界（DWMWA_EXTENDED_FRAME_BOUNDS = 9）
            RECT windowRect = new RECT();
            DwmApiHelper.dwmGetWindowAttribute(hWnd, 9, windowRect);

            int left = windowRect.left;
            int top = windowRect.top + (windowRect.bottom - windowRect.top) - clientRect.bottom;
            int right = left + clientRect.right;
            int bottom = top + clientRect.bottom;
            captureRect = new RECT();
            captureRect.left = left;
            captureRect.top = top;
            captureRect.right = right;
            captureRect.bottom = bottom;

            if (session != null) {
                if (session.getWidth() == width && session.getHeight() == height) {
                    // 窗口大小没有改变
                    return;
                }
                // 窗口尺寸被改变，释放资源后重新创建
                session.close();
            }

            session = new BitBltSession(hWnd, width, height);
        } catch (Exception e) {
            System.err.println("[BitBlt] Failed to create session: " + e.getMessage());
        } finally {
            lockSlim.writeLock().unlock();
        }
    }

    /**
     * 递归只尝试一次，会设置标志，正常调用置假
     *
     * @param recursive 递归标志
     * @return 截图帧
     */
    private GameCaptureFrame capture(boolean recursive) {
        if (hWndValue == 0) {
            return null;
        }

        if (!sizeCheckTimer.isRunning()) {
            sizeCheckTimer.start();
        }

        // 隔一段时间检查一次窗口尺寸；上次失败则忽略计时器
        if (lastCaptureFailed || recursive || sizeCheckTimer.elapsedMillis() > 1000) {
            sizeCheckTimer.reset();
            checkSession();
        }

        lockSlim.readLock().lock();
        try {
            Mat mat = capture0();
            if (mat != null) {
                GameCaptureFrame result = new GameCaptureFrame(mat, captureRect);
                lastCaptureFailed = false;
                return result;
            } else {
                if (lastCaptureFailed) return null; // 不是首次失败，不再重试
                lastCaptureFailed = true;
                if (recursive) return null; // 已设置递归标志
            }
        } finally {
            if (lockSlim.getReadHoldCount() > 0) {
                lockSlim.readLock().unlock();
            }
        }

        // 首次失败，递归重试一次
        return capture(true);
    }

    /**
     * 截图功能实现。需加锁后调用。
     */
    private Mat capture0() {
        try {
            return session != null ? session.getImage() : null;
        } catch (Exception e) {
            System.err.println("[BitBlt] Failed to capture image: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void stop() {
        lockSlim.writeLock().lock();
        try {
            hWndValue = 0;
            sizeCheckTimer.stop();
            if (session != null) {
                session.close();
                session = null;
            }
            captureRect = null;
        } finally {
            lockSlim.writeLock().unlock();
        }
        capturing = false;
    }

    // ---- 内部辅助类：简易计时器 ----
    private static class Stopwatch {
        private long startNanos = -1;
        private boolean running = false;

        boolean isRunning() {
            return running;
        }

        void start() {
            if (!running) {
                startNanos = System.nanoTime();
                running = true;
            }
        }

        void reset() {
            running = false;
            startNanos = -1;
        }

        void stop() {
            running = false;
        }

        long elapsedMillis() {
            if (!running || startNanos < 0) return 0;
            return (System.nanoTime() - startNanos) / 1_000_000;
        }
    }
}
