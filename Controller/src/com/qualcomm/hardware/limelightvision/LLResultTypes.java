package com.qualcomm.hardware.limelightvision;
import java.util.*;
import org.firstinspires.ftc.robotcore.external.navigation.*;
public final class LLResultTypes {
    public static class FiducialResult {
        private final int id;private final Pose3D camera,robot;private final double tx,ty;
        public FiducialResult(int id,Pose3D camera,Pose3D robot,double tx,double ty){this.id=id;this.camera=camera;this.robot=robot;this.tx=tx;this.ty=ty;}
        public int getFiducialId(){return id;}public Pose3D getTargetPoseCameraSpace(){return camera;}
        public Pose3D getTargetPoseRobotSpace(){return robot;}public double getTargetXDegrees(){return tx;}
        public double getTargetYDegrees(){return ty;}public double getTargetArea(){return 1;}
    }
    public static class ColorResult {
        private final double tx,ty,area;private final List<List<Double>> corners;
        public ColorResult(double tx,double ty,double area,double cx,double cy,double size){this.tx=tx;this.ty=ty;this.area=area;
            corners=List.of(List.of(cx-size/2,cy-size/2),List.of(cx+size/2,cy-size/2),List.of(cx+size/2,cy+size/2),List.of(cx-size/2,cy+size/2));}
        public double getTargetXDegrees(){return tx;}public double getTargetYDegrees(){return ty;}
        public double getTargetArea(){return area;}public List<List<Double>> getTargetCorners(){return corners;}
    }
}
