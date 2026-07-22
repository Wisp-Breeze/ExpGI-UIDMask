package BetterGI_GameCapture;

import com.sun.jna.platform.win32.WinDef;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * 游戏截图帧，对应 C# Fischless.GameCapture.GameCaptureFrame
 */
public class GameCaptureFrame implements AutoCloseable {

    private Mat frame;
    private WinDef.RECT captureRect;

    public GameCaptureFrame(Mat frame, WinDef.RECT captureRect) {
        this.frame = frame;
        this.captureRect = captureRect;
    }

    public GameCaptureFrame(Mat frame) {
        this(frame, null);
    }

    public Mat getFrame() {
        return frame;
    }

    public WinDef.RECT getCaptureRect() {
        return captureRect;
    }

    @Override
    public void close() {
        if (frame != null) {
            frame.release();
            frame = null;
        }
    }
}
