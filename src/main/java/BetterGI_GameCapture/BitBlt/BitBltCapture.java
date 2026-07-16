package BetterGI_GameCapture.BitBlt;


import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import BetterGI_GameCapture.GameCaptureFrame;
import BetterGI_GameCapture.IGameCapture;
import org.opencv.core.Mat;
import com.google.common.base.Stopwatch;
import org.opencv.core.Rect;

public  class BitBltCapture implements IGameCapture { //记得拆 abstract！！！！！
    private boolean _isCapturing = false;

    @Override
    public boolean IsCapturing() {
        return this._isCapturing;
    }

    public void setCapturing(boolean capturing) {
        this._isCapturing = capturing;
    }

    final Stopwatch _sizeCheckTimer = Stopwatch.createUnstarted();
    final ReentrantLock _rwLock = new ReentrantLock();
    volatile long _hWnd;//需要加锁
    private BitBltSession _session;//需要加锁
    Rect _captureRect;

    volatile boolean _lastCaptureFailed;


    @Override
    public void close() {
        Stop();
    }

    @Override
    public GameCaptureFrame Capture() {
        return Capture(false);
    }

    @Override
    public void Start(long hWnd){
        Start(hWnd,null);
    }
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
     *  检查窗口大小，如果改变则更新截图尺寸。
     */
    public void CheckSession(){
        boolean gotLock;
        try {
            gotLock = _rwLock.tryLock(500,TimeUnit.MILLISECONDS);
            // 写锁的获取只会在两个触发：start 和 CheckSession。
            // 无论哪种情况，都会检查&更新session。
            // 因此，当有其他线程正在等待更新时，当前线程将。
        } catch (InterruptedException e) {return;}
        if (!gotLock) {return;}

        try{
            // 窗口状态变化可能会导致会话失效
            // 上次截图失败则重置会话，避免一直截图失败
            if (_session!= null) {
                System.out.println("暂时无效");
            }


        }catch (Exception e){e.getStackTrace();}


    }


    private GameCaptureFrame Capture(boolean recursive) {
        if(_hWnd == 0){
            return null;
        }

        if(!_sizeCheckTimer.isRunning()){
            _sizeCheckTimer.start();
        }

        // 不会经常调整窗口尺寸的，所以隔一段时间检查一次就行
        // 上次如果截图失败的话忽略计时器，避免重复截图失败
        if(_lastCaptureFailed || recursive || _sizeCheckTimer.elapsed(TimeUnit.MILLISECONDS) > 1000){
            _sizeCheckTimer.reset();
            CheckSession();
        }

        try {
            _rwLock.lock();
            var mat = Capture0();
            var result = mat == null
                    ?null
                    : new GameCaptureFrame(mat, _captureRect);

            if(result != null) {
                // 成功截图
                _lastCaptureFailed = false;
                return result;
            }
            else {
                if (_lastCaptureFailed) return  result;// 这不是首次失败,不再进行尝试
                _lastCaptureFailed = true;// 设置失败标志
                if(recursive)return  result;// 已设置递归标志，说明也不是首次失败
            }
        }
        finally {
            if(_rwLock.isLocked()){
                _rwLock.unlock();
            }
        }

        // 首次出现截图异常会跳到这里
        // 首次出现错误重试截图，尽可能不出现截图失败(递归)
        return Capture(true);
    }

    /**
     * 截图功能的实现。需要加锁后调用，一般只由 Capture 方法调用。
     */
    private Mat Capture0(){
        try{


            return new Mat();//←←←←←←←←←←←←←←←！！！未完成！！！
            //return _session?.GetImage();


        }catch (Exception e){
            // 理论这里不应出现异常，除非窗口不存在了或者有什么bug
            return null;
        }
    }

    public void Stop(){


    }



}