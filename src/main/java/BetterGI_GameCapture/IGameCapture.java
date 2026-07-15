package BetterGI_GameCapture;


import java.util.Map;

public interface IGameCapture extends AutoCloseable {
    boolean IsCapturing();
    void setCapturing(boolean capturing);

    void Start(long hWnd);
    void Start(long hWnd, Map<String,Object> settings);

    GameCaptureFrame Capture();

    void Stop();
}