package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.limelightvision.LLResult;

/** Generic read-only SDK example. It never commands a motor or aims the robot. */
@TeleOp(name="Example AprilTag camera", group="Examples")
public class SharedVision extends OpMode {
    private Limelight3A camera;
    public void init() {
        camera=hardwareMap.get(Limelight3A.class,"limelight");
        camera.setPollRateHz(100);
        camera.pipelineSwitch(1);
    }
    public void start() { camera.start(); }
    public void loop() {
        LLResult result=camera.getLatestResult();
        boolean usable=result!=null && result.isValid()
                && result.getPipelineIndex()==1 && result.getStaleness()<=200;
        telemetry.addData("Fresh tag frame",usable);
        if(usable) for(var tag:result.getFiducialResults()) {
            telemetry.addData("Tag "+tag.getFiducialId(),"tx="+tag.getTargetXDegrees()+" ty="+tag.getTargetYDegrees());
        }
        telemetry.update();
    }
    public void stop() { if(camera!=null)camera.stop(); }
}
