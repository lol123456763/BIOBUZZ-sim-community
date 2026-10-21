package virtual_robot.biobuzz;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/** Robot values are editable estimates, never claimed to be measured team hardware. */
public final class Settings {
    public static final Settings S = new Settings();
    public double angleDeg=55, exitHeight=.254, wheelDiameter=.072, speedFactor=.75;
    public double pollenMass=.026, nectarMass=.045, dragCd=.47, airDensity=1.225;
    public double restitution=.35, rollingMu=.055, spinLift=0, feedInterval=.35;
    public double cameraHeight=.305, cameraPitch=-20, cameraFov=85, visionLatency=.06;
    public double cameraForward=0, cameraRight=0, cameraYaw=0, cameraRoll=0, cameraFps=30;
    public boolean cameraEnabled=true, frozenCamera=false;
    public int alliance=0;
    public double robotMass=13.5, gripForward=.95, gripStrafe=.72, driveDamping=.12;
    public double flywheelInertia=.00012, flywheelRecovery=.22, backspinRpm=1000, magnusSlope=.16;
    public double robotWidth=.4572, robotLength=.4572, robotHeight=.32;
    public double driveTrackWidth=.38, driveWheelbase=.30;
    public double driveWheelDiameter=.1016, driveRpm=312, driveTorque=1.8, driveGearRatio=1;
    public double batteryVoltage=12, brakeStrength=1, coastDrag=.018;
    public double shooterMaxTicks=2240, shooterTicksPerRev=28, intakeRate=1;
    public int capacity=4;
    public double comX=0, comY=0, yawInertia=0;
    public double floorImpactMu=.25, intakeCaptureTime=.075;
    public Settings() { }
    public void load() {
        try (Reader r=Files.newBufferedReader(Path.of("robot.properties"))) {
            Properties p=new Properties();p.load(r);
            angleDeg=read(p,"shooter.angle.deg",55,5,85);exitHeight=read(p,"shooter.exit.m",.254,.05,1);
            wheelDiameter=read(p,"flywheel.diameter.m",.072,.02,.2);speedFactor=read(p,"ball.speed.factor",.75,.1,1.5);
            pollenMass=read(p,"pollen.mass.kg",.026,.005,.2);nectarMass=read(p,"nectar.mass.kg",.045,.005,.3);
            dragCd=read(p,"ball.drag.cd",.47,0,2);restitution=read(p,"ball.restitution",.35,0,.95);
            rollingMu=read(p,"floor.rolling.mu",.055,0,1);spinLift=read(p,"ball.lift.accel",0,-10,10);
            cameraHeight=read(p,"camera.height.m",.305,.05,1);cameraPitch=read(p,"camera.pitch.deg",-20,-80,80);
            visionLatency=read(p,"camera.latency.s",.06,0,.5);
            cameraFov=read(p,"camera.fov.deg",85,10,150);cameraFps=read(p,"camera.fps",30,1,120);
            cameraForward=read(p,"camera.forward.m",0,-.8,.8);cameraRight=read(p,"camera.right.m",0,-.8,.8);
            cameraYaw=read(p,"camera.yaw.deg",0,-180,180);cameraRoll=read(p,"camera.roll.deg",0,-180,180);
            robotMass=read(p,"robot.mass.kg",13.5,5,30);gripForward=read(p,"drive.grip.forward",.95,.1,2);
            gripStrafe=read(p,"drive.grip.strafe",.72,.1,2);driveDamping=read(p,"drive.damping",.12,0,2);
            flywheelInertia=read(p,"flywheel.inertia.kgm2",.00012,.00001,.01);flywheelRecovery=read(p,"flywheel.recovery.s",.22,.02,1);
            backspinRpm=read(p,"ball.backspin.rpm",1000,-5000,5000);magnusSlope=read(p,"ball.magnus.slope",.16,0,1);
            floorImpactMu=read(p,"floor.impact.mu",.25,0,1);intakeCaptureTime=read(p,"intake.capture.s",.075,.01,.4);
        } catch(IOException ignored) { }
    }
    private double read(Properties p,String k,double d,double lo,double hi) {
        try {double v=Double.parseDouble(p.getProperty(k));return Double.isFinite(v)?Math.max(lo,Math.min(hi,v)):d;}
        catch(Exception e){return d;}
    }
}
