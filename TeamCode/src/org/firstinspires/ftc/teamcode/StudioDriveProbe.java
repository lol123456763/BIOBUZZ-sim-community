package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.*;
import com.qualcomm.robotcore.hardware.*;

/** Simulator diagnostic OpMode, independent of the team's imported competition source. */
@TeleOp(name="Studio drive calibration (A = BRAKE, otherwise FLOAT)",group="Studio")
public class StudioDriveProbe extends OpMode {
    DcMotor fl,fr,bl,br;
    public void init(){
        fl=hardwareMap.get(DcMotor.class,"frontLeftMotor");fr=hardwareMap.get(DcMotor.class,"frontRightMotor");
        bl=hardwareMap.get(DcMotor.class,"backLeftMotor");br=hardwareMap.get(DcMotor.class,"backRightMotor");
        fl.setDirection(DcMotor.Direction.REVERSE);bl.setDirection(DcMotor.Direction.REVERSE);
    }
    public void loop(){
        var zero=gamepad1.a?DcMotor.ZeroPowerBehavior.BRAKE:DcMotor.ZeroPowerBehavior.FLOAT;
        for(DcMotor m:new DcMotor[]{fl,fr,bl,br})m.setZeroPowerBehavior(zero);
        double y=-gamepad1.left_stick_y,x=gamepad1.left_stick_x,r=gamepad1.right_stick_x,d=Math.max(1,Math.abs(y)+Math.abs(x)+Math.abs(r));
        fl.setPower((y+x+r)/d);bl.setPower((y-x+r)/d);fr.setPower((y-x-r)/d);br.setPower((y+x-r)/d);
        telemetry.addData("Zero power behavior",zero);telemetry.addData("Secondary pad trigger",gamepad2.right_trigger);telemetry.update();
    }
    public void stop(){for(DcMotor m:new DcMotor[]{fl,fr,bl,br})m.setPower(0);}
}
