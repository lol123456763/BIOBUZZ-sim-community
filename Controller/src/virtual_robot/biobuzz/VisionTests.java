package virtual_robot.biobuzz;
import com.qualcomm.hardware.limelightvision.*;
public class VisionTests {
    static int n;static void check(boolean b,String s){n++;if(!b)throw new AssertionError(s);}
    public static void main(String[] args)throws Exception{
        Settings.S.cameraPitch=25;Settings.S.visionLatency=.06;
        Physics m=new Physics(Settings.S);m.robotX=m.hives[0].x;m.robotY=-1.3;
        Limelight3A camera=new Limelight3A(m);camera.pipelineSwitch(1);camera.start();
        camera.sample();check(camera.getLatestResult()==null,"latency delays first result");m.time=.1;camera.sample();
        LLResult r=camera.getLatestResult();check(r!=null&&r.isValid(),"visible red CELL produces detections");
        check(r.getFiducialResults().size()==4,"four geometrically visible red tags");
        check(r.getFiducialResults().get(0).getFiducialId()==34,"red audience cluster mapping");
        check(Math.abs(r.getBotpose().getPosition().y+1.3)<1e-9,"botpose stays field-relative");
        check(r.getFiducialResults().get(0).getTargetPoseCameraSpace().getPosition().z>0,"camera target pose has positive forward depth");
        Settings.S.frozenCamera=true;m.time=.3;camera.sample();check(camera.getLatestResult()==r,"freeze preserves frame identity");
        Thread.sleep(210);check(r.getStaleness()>=200,"frozen frame ages into stale detection");
        Settings.S.frozenCamera=false;Settings.S.cameraEnabled=false;m.time=.4;camera.sample();m.time=.5;camera.sample();
        check(!camera.getLatestResult().isValid(),"disabled camera clears detections after pipeline latency");
        Settings.S.cameraEnabled=true;m.time=0;camera.sample();m.time=.1;camera.sample();
        check(camera.getLatestResult().isValid(),"field reset restarts camera timestamps");
        camera.stop();check(camera.getLatestResult()==null,"stopped camera has no results");
        System.out.println("PASS: "+n+" camera geometry/lifecycle checks");
    }
}
