package org.firstinspires.ftc.robotcore.external.navigation;
public class Pose3D {
    private final Position p;private final YawPitchRollAngles o;
    public Pose3D(Position p,YawPitchRollAngles o){this.p=p;this.o=o;}
    public Position getPosition(){return p;}public YawPitchRollAngles getOrientation(){return o;}
}
