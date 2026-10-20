package virtual_robot.biobuzz;

/** DC motor torque-speed curve at the gearbox output, in SI units. */
public final class DriveMotorModel {
    public static double torque(double voltageFraction,double shaftRpm,boolean brake,Settings s){
        double ratio=Math.max(.01,s.driveGearRatio),rpm=s.driveRpm/ratio;
        double normalized=shaftRpm/rpm;
        double torque=s.driveTorque*ratio;
        if(Math.abs(voltageFraction)<1e-6)
            return -Math.max(-1,Math.min(1,normalized))*torque*(brake?s.brakeStrength:s.coastDrag);
        return Math.max(-torque,Math.min(torque,(voltageFraction*s.batteryVoltage/12.0-normalized)*torque));
    }
}
