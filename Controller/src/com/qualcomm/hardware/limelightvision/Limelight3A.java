package com.qualcomm.hardware.limelightvision;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import java.util.*;
import virtual_robot.biobuzz.*;
import org.firstinspires.ftc.robotcore.external.navigation.*;
/** Pinhole geometry + delayed sampled frames, not camera firmware or an image detector.
 * Board centers/normals are from official STEP; printed tags from FIRST production art. */
public class Limelight3A implements HardwareDevice {
    private final Physics model;private final Settings cfg;
    private int pipeline,pollHz=100;private boolean running;private LLResult result;
    private double lastFrame=Double.NEGATIVE_INFINITY,lastPoll=Double.NEGATIVE_INFINITY,lastTime=-1,robotYaw=Double.NaN;
    private final Deque<LLResult> pending=new ArrayDeque<>();
    public Limelight3A(Physics model){this.model=model;this.cfg=model.cfg;}
    public synchronized void pipelineSwitch(int n){if(n<0||n>9)throw new IllegalArgumentException("Pipeline must be 0-9");pipeline=n;}
    public synchronized int getPipelineIndex(){return pipeline;}
    public synchronized void start(){if(!running){clearFrames();running=true;}}
    public synchronized void stop(){running=false;clearFrames();}
    private void clearFrames(){pending.clear();result=null;lastFrame=lastPoll=Double.NEGATIVE_INFINITY;}
    public synchronized void setPollRateHz(int hz){if(hz<=0)throw new IllegalArgumentException("Poll rate must be positive");pollHz=hz;}
    public synchronized void updateRobotOrientation(double deg){if(Double.isFinite(deg))robotYaw=deg;}
    public synchronized LLResult getLatestResult(){return running?result:null;}
    public synchronized void sample(){
        if(model.time<lastTime){clearFrames();robotYaw=Double.NaN;}
        lastTime=model.time;
        if(!running)return;
        if(cfg.frozenCamera){pending.clear();lastFrame=Double.NEGATIVE_INFINITY;return;}
        if(model.time-lastFrame+1e-9>=1.0/cfg.cameraFps){
            lastFrame=model.time;pending.add(capture());
        }
        // Sensor capture and USB polling are separate. Drain even between camera frames.
        if(model.time-lastPoll+1e-9>=1.0/pollHz){
            lastPoll=model.time;
            while(!pending.isEmpty()&&model.time-pending.peek().timestamp+1e-9>=pending.peek().latencySeconds){
                result=pending.remove();result.received(model.time);
            }
        }
    }
    private LLResult capture(){
        List<LLResultTypes.FiducialResult> tags=new ArrayList<>();List<LLResultTypes.ColorResult> colors=new ArrayList<>();
        if(cfg.cameraEnabled){
            if(pipeline==1){for(Physics.Hive h:model.hives){
                if(model.time<h.tippingUntil)continue;
                int first=h.color==0?(h.side<0?34:30):(h.side<0?38:42);
                for(int i=0;i<4;i++){
                    // am-5888 board centers from official AndyMark STEP (meters).
                    // FIRST production PDF: 3.25in black border; unequal centers, .25in above board center.
                    double boardX=h.color==0?-.323637681017095:.324062318982907;
                    double[] printedOffsets={-6.5,-2.75,2.75,6.5};
                    double x=boardX-h.side*printedOffsets[i]*Physics.IN;
                    double y=h.side*(.327341117433+.25*Physics.IN*Math.sqrt(3)/2),z=1.261519154795+.25*Physics.IN*.5;
                    double camX=model.robotX+cfg.cameraRight*Math.cos(model.heading)-cfg.cameraForward*Math.sin(model.heading);
                    double camY=model.robotY+cfg.cameraRight*Math.sin(model.heading)+cfg.cameraForward*Math.cos(model.heading);
                    // Upward CELL tags are on the underside: CAD normal (0, side*.5, -sqrt(3)/2).
                    double towardCamera=(camY-y)*h.side*.5-(cfg.cameraHeight-z)*Math.sqrt(3)/2;
                    double[] c=camera(x,y,z);double[] r=robot(x,y,z);
                    if(visible(c)&&towardCamera>0){
                        double size=3.25*Physics.IN*focal()/c[2];
                        // Projected face area with a conservative 2px minimum detection size.
                        double incidence=Math.min(1,towardCamera/Math.sqrt(sq(camX-x)+sq(camY-y)+sq(cfg.cameraHeight-z)));
                        if(size<2)continue;
                        if(size*incidence<2)continue; // Edge-on tags cannot supply resolvable corners.
                        double area=Math.min(100,size*size*incidence/(640*480)*100);
                        tags.add(new LLResultTypes.FiducialResult(first+i,pose(c[0],c[1],c[2],0),pose(r[0],r[1],r[2],0),tx(c),ty(c),area));
                    }
                }
                }tags.sort(Comparator.comparingDouble(LLResultTypes.FiducialResult::getTargetArea).reversed());
            }else if(pipeline==0){for(Physics.Ball b:model.balls){
                if(!b.state.equals("floor")||b.color>=0)continue;
                double[] c=camera(b.x,b.y,b.z);
                if(visible(c)){
                    double size=2*b.r*focal()/c[2];if(size<2)continue;
                    double cx=320+focal()*c[0]/c[2],cy=240+focal()*c[1]/c[2];
                    colors.add(new LLResultTypes.ColorResult(tx(c),ty(c),Math.min(100,Math.PI*size*size/4/(640*480)*100),cx,cy,size));
                }
                }colors.sort(Comparator.comparingDouble(LLResultTypes.ColorResult::getTargetArea).reversed());}
        }
        Pose3D field=tags.isEmpty()?null:pose(model.robotX,model.robotY,0,Math.toDegrees(model.heading)+90);
        Pose3D mt2=field==null||!Double.isFinite(robotYaw)?null:pose(model.robotX,model.robotY,0,robotYaw);
        return new LLResult(pipeline,model.time,tags,colors,field,mt2,cfg.visionLatency,()->model.time);
    }
    private static double sq(double n){return n*n;}
    private double focal(){return 320/Math.tan(Math.toRadians(cfg.cameraFov/2));}
    private boolean visible(double[] c){return c[2]>.05&&Math.abs(focal()*c[0]/c[2])<320&&Math.abs(focal()*c[1]/c[2])<240;}
    private double tx(double[] c){return Math.toDegrees(Math.atan2(c[0],c[2]));}
    private double ty(double[] c){return -Math.toDegrees(Math.atan2(c[1],c[2]));}
    // Robot frame: right, forward, up. Camera legacy optical frame: right, down, forward.
    private double[] robot(double x,double y,double z){double dx=x-model.robotX,dy=y-model.robotY;
        return new double[]{dx*Math.cos(model.heading)+dy*Math.sin(model.heading),-dx*Math.sin(model.heading)+dy*Math.cos(model.heading),z};}
    private double[] camera(double x,double y,double z){
        double[] r=robot(x,y,z);double right=r[0]-cfg.cameraRight,forward=r[1]-cfg.cameraForward,up=r[2]-cfg.cameraHeight;
        double yaw=Math.toRadians(cfg.cameraYaw),pitch=Math.toRadians(cfg.cameraPitch),roll=Math.toRadians(cfg.cameraRoll);
        double cr=right*Math.cos(yaw)+forward*Math.sin(yaw),cf=-right*Math.sin(yaw)+forward*Math.cos(yaw);
        double down=cf*Math.sin(pitch)-up*Math.cos(pitch),depth=cf*Math.cos(pitch)+up*Math.sin(pitch);
        return new double[]{cr*Math.cos(roll)+down*Math.sin(roll),-cr*Math.sin(roll)+down*Math.cos(roll),depth};
    }
    private Pose3D pose(double x,double y,double z,double yaw){long t=(long)(model.time*1e9);return new Pose3D(new Position(DistanceUnit.METER,x,y,z,t),new YawPitchRollAngles(AngleUnit.DEGREES,yaw,0,0,t));}
    public synchronized Map<String,Object> diagnostics(){
        Map<String,Object> d=new LinkedHashMap<>();d.put("running",running);d.put("pipeline",pipeline);d.put("fps",cfg.cameraFps);d.put("pollRateHz",pollHz);
        d.put("model","Geometric pinhole; official CAD + printed tag geometry; no image processing or occlusion");
        d.put("valid",running&&result!=null&&result.isValid());d.put("latencyMs",cfg.visionLatency*1000);
        d.put("framePipeline",result==null?-1:result.getPipelineIndex());d.put("ageMs",result==null?-1:result.getStaleness());
        d.put("tx",result==null?0:result.getTx());d.put("ty",result==null?0:result.getTy());d.put("ta",result==null?0:result.getTa());
        d.put("tagIds",result==null?List.of():result.tags.stream().map(LLResultTypes.FiducialResult::getFiducialId).toList());
        d.put("corners",result==null?List.of():result.colors.stream().map(LLResultTypes.ColorResult::getTargetCorners).toList());return d;
    }
    public Manufacturer getManufacturer(){return Manufacturer.Other;}public String getDeviceName(){return "Simulated Limelight 3A (geometric)";}
    public String getConnectionInfo(){return "simulation";}public int getVersion(){return 2;}public void resetDeviceConfigurationForOpMode(){stop();robotYaw=Double.NaN;}public void close(){stop();}
}
