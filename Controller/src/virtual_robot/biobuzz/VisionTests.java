package virtual_robot.biobuzz;
import com.qualcomm.hardware.limelightvision.*;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
public class VisionTests {
    static int n;static void check(boolean b,String s){n++;if(!b)throw new AssertionError(s);}
    static void near(double a,double b,String s){check(Math.abs(a-b)<1e-8,s+": "+a+" != "+b);}
    static LLResult frame(Physics m,Limelight3A camera,double t){m.time=t;camera.sample();return camera.getLatestResult();}
    public static void main(String[] args)throws Exception{
        Settings cfg=new Settings();cfg.cameraPitch=25;cfg.visionLatency=.06;
        Physics m=new Physics(cfg);m.robotX=m.hives[0].x;m.robotY=-1.3;
        Limelight3A camera=new Limelight3A(m);camera.pipelineSwitch(1);camera.start();
        camera.sample();check(camera.getLatestResult()==null,"latency delays first result");
        LLResult r=frame(m,camera,.06);check(r!=null&&r.isValid(),"visible red CELL produces detections");
        check(r.getFiducialResults().size()==4,"four geometrically visible red tags");
        check(r.getFiducialResults().stream().allMatch(t->t.getFiducialId()>=34&&t.getFiducialId()<=37),"red audience cluster mapping");
        near(r.getBotpose().getPosition().y,-1.3,"botpose stays field-relative");
        near(r.getBotpose().getOrientation().getYaw(AngleUnit.DEGREES),90,"north-facing chassis uses +90 field yaw");
        check(r.getBotpose_MT2()==null,"MT2 requires orientation feed");
        near(r.getTargetingLatency(),60,"SDK reports latency in milliseconds");
        check(r.getTa()>0&&r.getTa()<4,"tag area is percentage, not tag count");
        var tag=r.getFiducialResults().get(0);
        check(tag.getTargetPoseCameraSpace().getPosition().z>0,"optical depth is positive");
        near(tag.getTargetPoseRobotSpace().getPosition().z,(1.261519154795+.25*Physics.IN*.5),"robot target height matches official CAD and production-art tag center");
        near(tag.getTargetPoseRobotSpace().getPosition().y,-(.327341117433+.25*Physics.IN*Math.sqrt(3)/2)-m.robotY,"robot target distance matches official CAD and production-art tag center");
        camera.updateRobotOrientation(37);frame(m,camera,.10);r=frame(m,camera,.17);
        near(r.getBotpose_MT2().getOrientation().getYaw(AngleUnit.DEGREES),37,"MT2 uses supplied yaw");
        cfg.frozenCamera=true;frame(m,camera,.4);check(camera.getLatestResult()==r,"freeze preserves frame identity");
        check(r.getStaleness()>=200,"simulation time ages frozen frame");
        Thread.sleep(210);check(r.getStaleness()>=200,"wall time ages paused frame");
        cfg.frozenCamera=false;cfg.cameraEnabled=false;frame(m,camera,.5);r=frame(m,camera,.57);
        check(!r.isValid()&&r.getBotpose()==null,"disabled camera clears targets and localization after latency");
        cfg.cameraEnabled=true;frame(m,camera,0);r=frame(m,camera,.06);check(r.isValid(),"field reset restarts timestamps");
        camera.stop();check(camera.getLatestResult()==null,"stopped camera has no result");camera.start();
        check(camera.getLatestResult()==null,"restart cannot leak pre-stop frame");
        frame(m,camera,.07);check(camera.getLatestResult()==null,"restart incurs fresh frame latency");
        r=frame(m,camera,.14);check(r!=null,"restart delivers fresh frame");
        camera.pipelineSwitch(0);check(camera.getLatestResult().getPipelineIndex()==1,"pipeline switch is asynchronous");
        frame(m,camera,.2);r=frame(m,camera,.27);check(r.getPipelineIndex()==0,"new pipeline arrives after capture and latency");
        check(r.getBotpose()==null,"color pipeline cannot know robot field pose");
        // Isolated ball projection test: 640x480 square-pixel pinhole, horizontal FOV 90 degrees.
        camera.stop();m.balls.clear();cfg.cameraPitch=0;cfg.cameraFov=90;cfg.cameraHeight=.3;cfg.visionLatency=0;cfg.cameraFps=30;
        m.robotX=0;m.robotY=0;m.heading=0;m.floor(-1,.25,1).z=.3;camera.start();r=frame(m,camera,1);
        near(r.getTx(),Math.toDegrees(Math.atan(.25)),"positive tx is right");near(r.getTy(),0,"target at camera height is centered vertically");
        var corners=r.colors.get(0).getTargetCorners();near((corners.get(0).get(0)+corners.get(1).get(0))/2,400,"pinhole pixel center");
        double expected=Math.PI*Math.pow(2*1.4*Physics.IN*320,2)/4/(640*480)*100;
        near(r.getTa(),expected,"ball silhouette area uses circular disk");
        cfg.cameraRight=.25;r=frame(m,camera,1.04);near(r.getTx(),0,"camera lateral mount applied");
        cfg.cameraForward=.5;r=frame(m,camera,1.08);near(r.getTa(),expected*4,"forward mount changes distance and angular size");
        cfg.cameraRight=0;cfg.cameraForward=0;cfg.cameraYaw=90;r=frame(m,camera,1.12);check(!r.isValid(),"side-facing mount excludes front target");
        cfg.cameraYaw=0;cfg.cameraRoll=90;r=frame(m,camera,1.16);near(r.getTx(),0,"roll rotates horizontal displacement");check(r.getTy()>0,"roll moves displacement into vertical axis");
        cfg.cameraRoll=0;m.balls.get(0).z=1.1;r=frame(m,camera,1.20);check(!r.isValid(),"vertical FOV derives from image aspect, not hardcoded degrees");
        m.balls.get(0).z=.3;camera.stop();camera.setPollRateHz(5);camera.start();r=frame(m,camera,2);LLResult first=r;
        frame(m,camera,2.1);check(camera.getLatestResult()==first,"USB poll cadence independent of capture FPS");
        r=frame(m,camera,2.2);check(r!=first,"poll receives latest completed frame");
        check(camera.diagnostics().get("framePipeline").equals(0),"read-only diagnostics expose delivered pipeline");
        boolean rejected=false;try{camera.pipelineSwitch(10);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"reject out-of-range pipeline");
        camera.pipelineSwitch(1);cfg.cameraPitch=25;m.robotX=m.hives[0].x;m.robotY=-1.3;
        r=frame(m,camera,3);check(r.isValid(),"tag pipeline detects official board");
        cfg.cameraHeight=2.5;cfg.cameraPitch=-50;r=frame(m,camera,3.6);
        check(!r.isValid(),"CAD underside rejects viewing tag board from above its back face");
        cfg.cameraHeight=.3;cfg.cameraPitch=25;r=frame(m,camera,3.9);
        check(r.isValid(),"CAD underside is visible from the mounted camera below");
        var byId=new java.util.HashMap<Integer,LLResultTypes.FiducialResult>();
        for(var t:r.tags)byId.put(t.getFiducialId(),t);
        near(byId.get(37).getTargetPoseRobotSpace().getPosition().x-byId.get(34).getTargetPoseRobotSpace().getPosition().x,.3302,"official production art outer tag centers span 13 inches");
        near(byId.get(36).getTargetPoseRobotSpace().getPosition().x-byId.get(35).getTargetPoseRobotSpace().getPosition().x,.1397,"official production art center gap is 5.5 inches");
        near(byId.get(35).getTargetPoseRobotSpace().getPosition().x-byId.get(34).getTargetPoseRobotSpace().getPosition().x,.09525,"official production art outer gap is 3.75 inches");
        camera.stop();System.out.println("PASS: "+n+" camera geometry/lifecycle checks");
    }
}
