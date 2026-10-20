package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.teamcode.helpers.DrivePower;

/** Generic simulator example, written for sharing; contains no competition code. */
@TeleOp(name="Example mecanum drive", group="Examples")
public class SharedDrive extends OpMode {
    private DcMotor frontLeft, frontRight, backLeft, backRight,exampleMotor;

    public void init() {
        frontLeft=hardwareMap.get(DcMotor.class,"front_left_motor");
        frontRight=hardwareMap.get(DcMotor.class,"front_right_motor");
        backLeft=hardwareMap.get(DcMotor.class,"back_left_motor");
        backRight=hardwareMap.get(DcMotor.class,"back_right_motor");
        exampleMotor=hardwareMap.get(DcMotor.class,"shooter");
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        for(DcMotor motor:new DcMotor[]{frontLeft,frontRight,backLeft,backRight})motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void loop() {
        double forward=-gamepad1.left_stick_y,strafe=gamepad1.left_stick_x,turn=gamepad1.right_stick_x;
        double scale=DrivePower.scale(forward,strafe,turn);
        frontLeft.setPower((forward+strafe+turn)*scale);
        frontRight.setPower((forward-strafe-turn)*scale);
        backLeft.setPower((forward-strafe+turn)*scale);
        backRight.setPower((forward+strafe-turn)*scale);
        exampleMotor.setPower(gamepad2.right_trigger);
        telemetry.addData("Example","Gamepad 1: left stick drives, right stick turns");
        telemetry.update();
    }
}
