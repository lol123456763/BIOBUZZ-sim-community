package com.qualcomm.hardware.limelightvision;
import java.util.*;
import org.firstinspires.ftc.robotcore.external.navigation.*;
public final class LLResult {
    public final int pipeline; public final long created=System.nanoTime();public final double timestamp,tx,ty,area;
    public final List<LLResultTypes.FiducialResult> tags;public final List<LLResultTypes.ColorResult> colors;public final Pose3D fieldPose;
    public LLResult(int pipeline,double timestamp,List<LLResultTypes.FiducialResult> tags,List<LLResultTypes.ColorResult> colors,Pose3D pose){
        this.pipeline=pipeline;this.timestamp=timestamp;this.tags=List.copyOf(tags);this.colors=List.copyOf(colors);fieldPose=pose;
        tx=!tags.isEmpty()?tags.get(0).getTargetXDegrees():!colors.isEmpty()?colors.get(0).getTargetXDegrees():0;
        ty=!tags.isEmpty()?tags.get(0).getTargetYDegrees():!colors.isEmpty()?colors.get(0).getTargetYDegrees():0;
        area=!colors.isEmpty()?colors.get(0).getTargetArea():tags.size();}
    public boolean isValid(){return !tags.isEmpty()||!colors.isEmpty();}public long getStaleness(){return (System.nanoTime()-created)/1_000_000;}
    public double getTimestamp(){return timestamp;}public int getPipelineIndex(){return pipeline;}
    public double getTx(){return tx;}public double getTy(){return ty;}public double getTa(){return area;}
    public Pose3D getBotpose(){return fieldPose;}public Pose3D getBotpose_MT2(){return fieldPose;}
    public List<LLResultTypes.FiducialResult> getFiducialResults(){return tags;}public List<LLResultTypes.ColorResult> getColorResults(){return colors;}
}
