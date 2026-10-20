package com.qualcomm.hardware.limelightvision;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import java.util.*;
import virtual_robot.biobuzz.*;
import org.firstinspires.ftc.robotcore.external.navigation.*;
/** Idealized geometric detections, not image rendering or a Limelight firmware emulator. */
public class Limelight3A implements HardwareDevice {
    private final Physics model;private int pipeline;private boolean running;private LLResult result;
    private double lastFrame=-1;private final Deque<LLResult> pending=new ArrayDeque<>();
    public Limelight3A(Physics model){this.model=model;}
    public void pipelineSwitch(int n){pipeline=n;}public int getPipelineIndex(){return pipeline;}
    public void start(){running=true;}public void stop(){running=false;pending.clear();}
    public void setPollRateHz(int hz){}public void updateRobotOrientation(double deg){}
    public synchronized LLResult getLatestResult(){return running?result:null;}
    public synchronized void sample(){
        if(model.time<lastFrame){pending.clear();result=null;lastFrame=-1;}
        if(!running||Settings.S.frozenCamera||model.time-lastFrame<1.0/30)return;
        lastFrame=model.time;List<LLResultTypes.FiducialResult> tags=new ArrayList<>();List<LLResultTypes.ColorResult> colors=new ArrayList<>();
        if(Settings.S.cameraEnabled){
            if(pipeline==1){for(Physics.Hive h:model.hives){
                if(model.time<h.tippingUntil)continue;
                // Only the exposed upward CELL's underside cluster is synthesized.
                int first=h.color==0?(h.side<0?34:30):(h.side<0?38:42);
                for(int i=0;i<4;i++){double x=h.x+(i-1.5)*3.6*Physics.IN,y=h.y(),z=51.94*Physics.IN;
                    double[] c=camera(x,y,z);double tx=Math.toDegrees(Math.atan2(c[0],c[2])),ty=-Math.toDegrees(Math.atan2(c[1],c[2]));
                    // Face normal points toward the same end of the field as the CELL.
                    if(c[2]>.05&&Math.abs(tx)<Settings.S.cameraFov/2&&Math.abs(ty)<35&&(model.robotY-y)*h.side>0){
                        tags.add(new LLResultTypes.FiducialResult(first+i,pose(c[0],c[1],c[2],0),pose(c[0],c[2],-c[1],0),tx,ty));}}}
            }else if(pipeline==0){for(Physics.Ball b:model.balls){if(!b.state.equals("floor")||b.color>=0)continue;
                double[] c=camera(b.x,b.y,b.z);double tx=Math.toDegrees(Math.atan2(c[0],c[2])),ty=-Math.toDegrees(Math.atan2(c[1],c[2]));
                if(c[2]>.05&&Math.abs(tx)<Settings.S.cameraFov/2&&Math.abs(ty)<35){double focal=320/Math.tan(Math.toRadians(Settings.S.cameraFov/2)),size=2*b.r*focal/c[2];
                    colors.add(new LLResultTypes.ColorResult(tx,ty,size*size/(640*480)*100,320+focal*c[0]/c[2],240+focal*c[1]/c[2],size));}}
                colors.sort(Comparator.comparingDouble(LLResultTypes.ColorResult::getTargetArea).reversed());}
        }
        LLResult frame=new LLResult(pipeline,model.time,tags,colors,pose(model.robotX,model.robotY,0,Math.toDegrees(model.heading)));
        pending.add(frame);while(!pending.isEmpty()&&model.time-pending.peek().timestamp>=Settings.S.visionLatency)result=pending.remove();
    }
    private double[] camera(double x,double y,double z){double dx=x-model.robotX,dy=y-model.robotY;
        double right=dx*Math.cos(model.heading)+dy*Math.sin(model.heading),forward=-dx*Math.sin(model.heading)+dy*Math.cos(model.heading),up=z-Settings.S.cameraHeight;
        double p=Math.toRadians(Settings.S.cameraPitch);return new double[]{right,forward*Math.sin(p)-up*Math.cos(p),forward*Math.cos(p)+up*Math.sin(p)};}
    private Pose3D pose(double x,double y,double z,double yaw){return new Pose3D(new Position(DistanceUnit.METER,x,y,z,System.nanoTime()),new YawPitchRollAngles(AngleUnit.DEGREES,yaw,0,0,System.nanoTime()));}
    public Manufacturer getManufacturer(){return Manufacturer.Other;}public String getDeviceName(){return "Simulated Limelight 3A (geometric)";}
    public String getConnectionInfo(){return "simulation";}public int getVersion(){return 1;}public void resetDeviceConfigurationForOpMode(){}public void close(){stop();}
}
