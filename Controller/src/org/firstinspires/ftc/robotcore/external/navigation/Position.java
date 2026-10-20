package org.firstinspires.ftc.robotcore.external.navigation;
public class Position {
    public final DistanceUnit unit; public final double x,y,z;public final long acquisitionTime;
    public Position(DistanceUnit unit,double x,double y,double z,long time){this.unit=unit;this.x=x;this.y=y;this.z=z;acquisitionTime=time;}
    public Position toUnit(DistanceUnit u){return new Position(u,u.fromUnit(unit,x),u.fromUnit(unit,y),u.fromUnit(unit,z),acquisitionTime);}
}
