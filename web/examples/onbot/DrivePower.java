package org.firstinspires.ftc.teamcode.helpers;

/** Generic example of an OnBot Java helper in a subpackage. */
public final class DrivePower {
    private DrivePower() {}
    public static double scale(double forward,double strafe,double turn) {
        return 0.6/Math.max(1,Math.abs(forward)+Math.abs(strafe)+Math.abs(turn));
    }
}
