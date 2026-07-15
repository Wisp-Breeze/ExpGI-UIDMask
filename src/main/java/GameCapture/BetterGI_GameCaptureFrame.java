package GameCapture;


import org.opencv.core.Mat;
import org.opencv.core.Rect;

public abstract class BetterGI_GameCaptureFrame implements AutoCloseable{ //记得拆abstract

    protected Mat Frame;
    protected Rect CaptureRect;

    public BetterGI_GameCaptureFrame(Mat frame, Rect captureRect){
        Frame = frame;
        CaptureRect = captureRect;
    }

    public Mat getFrame(){return Frame;}
    public Rect getCaptureRect(){return CaptureRect;}

    @Override
    public void close(){
        if(this.Frame!=null && !this.Frame.empty()){
            this.Frame.release();
        }
    }

    

}
