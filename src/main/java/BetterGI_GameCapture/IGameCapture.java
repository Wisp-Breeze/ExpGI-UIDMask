package BetterGI_GameCapture;

import org.bytedeco.opencv.opencv_core.Mat;

import java.util.Map;

/**
 * 游戏截图接口，对应 C# Fischless.GameCapture.IGameCapture
 */
public interface IGameCapture extends AutoCloseable {

    boolean isCapturing();

    /**
     * 启动截图
     *
     * @param hWnd     窗口句柄（native pointer）
     * @param settings 可选设置
     */
    void start(long hWnd, Map<String, Object> settings);

    /**
     * 捕获一帧
     *
     * @return 帧数据，如果截图失败返回 null
     */
    GameCaptureFrame capture();

    /**
     * 停止截图
     */
    void stop();

    @Override
    default void close() {
        stop();
    }
}
