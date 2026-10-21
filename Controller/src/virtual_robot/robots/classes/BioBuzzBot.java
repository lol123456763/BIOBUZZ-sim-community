package virtual_robot.robots.classes;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.configuration.MotorType;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import virtual_robot.controller.*;
import virtual_robot.biobuzz.*;

@BotConfig(name="BIOBUZZ Community - front intake / underhand",filename="biobuzz_bot")
public class BioBuzzBot extends DynamicMecanumBase {
    public static volatile BioBuzzBot current;
    public static final Physics MODEL=new Physics(Settings.S);
    private Flywheel flywheel;private DcMotorExImpl intake,windmill;private ServoImpl gate;private Limelight3A camera;
    public double[] wheelPhases=new double[4];public double intakePhase,shooterPhase;
    private double physicalGate=.5;
    protected void createHardwareMap(){
        super.createHardwareMap();hardwareMap.setActive(true);
        hardwareMap.put("frontLeftMotor",hardwareMap.get(DcMotorEx.class,"front_left_motor"));
        hardwareMap.put("frontRightMotor",hardwareMap.get(DcMotorEx.class,"front_right_motor"));
        hardwareMap.put("backLeftMotor",hardwareMap.get(DcMotorEx.class,"back_left_motor"));
        hardwareMap.put("backRightMotor",hardwareMap.get(DcMotorEx.class,"back_right_motor"));
        flywheel=new Flywheel();intake=new DcMotorExImpl(MotorType.Gobilda192,motorController1,1);
        windmill=new DcMotorExImpl(MotorType.Gobilda192,motorController1,2);gate=new ServoImpl();gate.setPosition(.5);
        hardwareMap.put("shooter",flywheel);hardwareMap.put("intake",intake);hardwareMap.put("windmill",windmill);hardwareMap.put("gate",gate);
        camera=new Limelight3A(MODEL);hardwareMap.put("limelight",camera);
        hardwareMap.put("pinpoint",new org.firstinspires.ftc.teamcode.odometry.GoBildaPinpointDriver());
        hardwareMap.setActive(false);current=this;
    }
    public synchronized void updateStateAndSensors(double ms){
        super.updateStateAndSensors(ms);flywheel.update(ms);intake.update(ms);windmill.update(ms);
        synchronized(MODEL){MODEL.robotX=chassisBody.getTransform().getTranslationX();MODEL.robotY=chassisBody.getTransform().getTranslationY();
            MODEL.heading=headingRadians;MODEL.robotVx=chassisBody.getLinearVelocity().x;MODEL.robotVy=chassisBody.getLinearVelocity().y;
            MODEL.robotOmega=chassisBody.getAngularVelocity();
            double vx=MODEL.robotVx*Math.cos(headingRadians)+MODEL.robotVy*Math.sin(headingRadians);
            double vy=-MODEL.robotVx*Math.sin(headingRadians)+MODEL.robotVy*Math.cos(headingRadians);
            double[] wheels=DriveMath.wheels(vx,vy,MODEL.robotOmega,(Settings.S.driveTrackWidth+Settings.S.driveWheelbase)/2);
            for(int i=0;i<4;i++)wheelPhases[i]+=wheels[i]/(Settings.S.driveWheelDiameter/2)*ms/1000;
            intakePhase+=intake.getPower()*ms*.02;shooterPhase+=flywheel.getVelocity()/28*Math.PI*2*ms/1000;
            physicalGate+=Math.max(-ms*.003,Math.min(ms*.003,gate.getPosition()-physicalGate));
            int old=MODEL.shots;MODEL.step(ms/1000,intake.getPower()*Settings.S.intakeRate,flywheel.getVelocity(),physicalGate);
            if(MODEL.shots>old)flywheel.droop(MODEL.lastShotMass,MODEL.lastExitSpeed);camera.sample();}
    }
    public void place(double x,double y,double heading){
        double limit=Physics.HALF-Math.hypot(Settings.S.robotWidth,Settings.S.robotLength)/2;
        chassisBody.getTransform().identity();chassisBody.getTransform().rotate(heading);
        chassisBody.getTransform().translate(Math.max(-limit,Math.min(limit,x)),Math.max(-limit,Math.min(limit,y)));
        chassisBody.setLinearVelocity(0,0);chassisBody.setAngularVelocity(0);
        MODEL.robotX=chassisBody.getTransform().getTranslationX();MODEL.robotY=chassisBody.getTransform().getTranslationY();MODEL.heading=heading;
    }
    public java.util.Map<String,Object> visionDiagnostics(){return camera==null?java.util.Map.of("running",false):camera.diagnostics();}
    public double intakePower(){return intake==null?0:intake.getPower();}
    public double speed(){return flywheel==null?0:flywheel.getVelocity();}
    public void powerDownAndReset(){super.powerDownAndReset();if(flywheel!=null)flywheel.stopAndReset();if(intake!=null)intake.stopAndReset();if(windmill!=null)windmill.stopAndReset();if(camera!=null)camera.stop();}
    public static class Flywheel extends DcMotorExImpl {
        private double requested,measured;
        public Flywheel(){super(MotorType.RevUltraPlanetaryOneToOne,new DcMotorControllerImpl(),0);}
        public synchronized void setVelocity(double ticks){requested=Math.max(-Settings.S.shooterMaxTicks,Math.min(Settings.S.shooterMaxTicks,ticks));}
        public synchronized void setPower(double power){super.setPower(power);requested=Math.max(-1,Math.min(1,power))*Settings.S.shooterMaxTicks;}
        public synchronized double getVelocity(){return measured;}
        public synchronized double update(double ms){measured+=(requested-measured)*(1-Math.exp(-ms/(Settings.S.flywheelRecovery*1000)));return 0;}
        public synchronized void droop(double mass,double exitSpeed){
            measured=virtual_robot.biobuzz.ContactMath.flywheelAfterShot(measured,mass,exitSpeed,Settings.S.flywheelInertia,Settings.S.shooterTicksPerRev);
        }
        public synchronized void stopAndReset(){super.stopAndReset();requested=measured=0;}
    }
}
