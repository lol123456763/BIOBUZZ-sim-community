package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/** Generic timed movement example, not a competition autonomous routine. */
@Autonomous(name="Example timed forward",group="Examples")
public class SharedAuto extends LinearOpMode {
    public void runOpMode() throws InterruptedException {
        DcMotor fl=hardwareMap.get(DcMotor.class,"front_left_motor"),fr=hardwareMap.get(DcMotor.class,"front_right_motor");
        DcMotor bl=hardwareMap.get(DcMotor.class,"back_left_motor"),br=hardwareMap.get(DcMotor.class,"back_right_motor");
        fl.setDirection(DcMotor.Direction.REVERSE);bl.setDirection(DcMotor.Direction.REVERSE);
        waitForStart();
        if(isStopRequested())return;
        for(DcMotor motor:new DcMotor[]{fl,fr,bl,br})motor.setPower(.3);
        sleep(600);
        for(DcMotor motor:new DcMotor[]{fl,fr,bl,br})motor.setPower(0);
        while(opModeIsActive()){telemetry.addData("Example","Movement complete");telemetry.update();idle();}
    }
}
