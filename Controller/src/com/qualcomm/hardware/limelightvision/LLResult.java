package com.qualcomm.hardware.limelightvision;
import java.util.*;
import java.util.function.DoubleSupplier;
import org.firstinspires.ftc.robotcore.external.navigation.*;
public final class LLResult {
    public final int pipeline;public final long created=System.nanoTime();public final double timestamp,tx,ty,area,latencySeconds;
    public final List<LLResultTypes.FiducialResult> tags;public final List<LLResultTypes.ColorResult> colors;public final Pose3D fieldPose;
    private final Pose3D mt2;private final DoubleSupplier clock;private long receiptNanos=created;private double receiptTime;
    public LLResult(int pipeline,double timestamp,List<LLResultTypes.FiducialResult> tags,List<LLResultTypes.ColorResult> colors,Pose3D pose){
        this(pipeline,timestamp,tags,colors,pose,null,0,()->timestamp);
    }
    public LLResult(int pipeline,double timestamp,List<LLResultTypes.FiducialResult> tags,List<LLResultTypes.ColorResult> colors,Pose3D pose,Pose3D mt2,double latency,DoubleSupplier clock){
        this.pipeline=pipeline;this.timestamp=timestamp;this.tags=List.copyOf(tags);this.colors=List.copyOf(colors);fieldPose=tags.isEmpty()?null:pose;this.mt2=tags.isEmpty()?null:mt2;
        latencySeconds=latency;this.clock=clock;receiptTime=timestamp;
        tx=!tags.isEmpty()?tags.get(0).getTargetXDegrees():!colors.isEmpty()?colors.get(0).getTargetXDegrees():0;
        ty=!tags.isEmpty()?tags.get(0).getTargetYDegrees():!colors.isEmpty()?colors.get(0).getTargetYDegrees():0;
        area=!tags.isEmpty()?tags.get(0).getTargetArea():!colors.isEmpty()?colors.get(0).getTargetArea():0;
    }
    void received(double time){receiptNanos=System.nanoTime();receiptTime=time;}
    public boolean isValid(){return !tags.isEmpty()||!colors.isEmpty();}
    /** Time since receipt, distinct from capture/processing latency. Ages on pause/freeze too. */
    public long getStaleness(){return Math.max((System.nanoTime()-receiptNanos)/1_000_000,Math.max(0,(long)((clock.getAsDouble()-receiptTime)*1000)));}
    public double getTimestamp(){return timestamp;}public int getPipelineIndex(){return pipeline;}
    public double getCaptureLatency(){return 0;}public double getTargetingLatency(){return latencySeconds*1000;}public double getParseLatency(){return 0;}
    public double getTx(){return tx;}public double getTy(){return ty;}public double getTa(){return area;}
    public Pose3D getBotpose(){return fieldPose;}public Pose3D getBotpose_MT2(){return mt2;}
    public List<LLResultTypes.FiducialResult> getFiducialResults(){return tags;}public List<LLResultTypes.ColorResult> getColorResults(){return colors;}
}
