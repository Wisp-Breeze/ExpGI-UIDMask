package GameCapture.BitBlt;


import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import GameCapture.GameCaptureFrame;
import GameCapture.IGameCapture;
import org.apache.commons.lang3.time.StopWatch;

public abstract class BitBltCapture implements IGameCapture { //记得拆 abstract！！！！！
    private boolean _isCapturing = false;

    @Override
    public boolean IsCapturing() {return this._isCapturing;}
    public void setCapturing(boolean capturing) {this._isCapturing = capturing;}

    final StopWatch _sizeCheckTimer = new StopWatch();
    final ReentrantLock _rwLock = new ReentrantLock();
    volatile long _hWnd;//需要加锁
    private BitBltSession _session;//需要加锁

    @Override
    public void close() {Stop();}

    @Override
    public GameCaptureFrame Capture() {return Capture(false);}

    private GameCaptureFrame Capture(boolean recursive) {return null;}

    @Override
    public void Start(long hWnd, Map<String, Object> settings) {
        Object value;
        if (settings == null || !settings.containsKey("autoFixWin11BitBlt")) return;
        value = settings.get("autoFixWin11BitBlt");
        if (value != null && (Boolean) value) {
            BitBltRegistryHelper.SetDirectXUserGlobalSettings();
        }

        synchronized (this) {
            try {
                _hWnd = hWnd;
                if (_hWnd == 0) {return;}
                if (_session != null) {_session.close();}

                _session = null;
                this.setCapturing(true);
            } catch (Exception e) {e.getStackTrace();}
            finally {_rwLock.unlock();}

            CheckSession();
        }
    }

    /**
     *检查窗口大小，如果改变则更新截图尺寸。
     */
    private void CheckSession(){

    }
}