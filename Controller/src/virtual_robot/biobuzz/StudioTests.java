package virtual_robot.biobuzz;
import com.google.gson.*;
import com.qualcomm.robotcore.hardware.Gamepad;

public final class StudioTests {
    static int n;static void check(boolean b,String m){n++;if(!b)throw new AssertionError(m);}
    public static void main(String[] args)throws Exception{
        Settings c=new Settings();double stall=DriveMotorModel.torque(1,0,false,c),free=DriveMotorModel.torque(1,c.driveRpm,false,c);
        check(stall==c.driveTorque,"DC motor stall torque");check(Math.abs(free)<1e-10,"Zero torque at free speed");
        check(DriveMotorModel.torque(1,100,false,c)<stall,"Back EMF reduces accelerating torque");
        double coast=DriveMotorModel.torque(0,100,false,c),brake=DriveMotorModel.torque(0,100,true,c);check(coast<0&&Math.abs(brake)>Math.abs(coast)*10,"BRAKE dissipates much more energy than FLOAT");
        c.driveGearRatio=2;check(Math.abs(DriveMotorModel.torque(1,0,false,c)-2*stall)<1e-10,"Reduction multiplies torque");check(Math.abs(DriveMotorModel.torque(1,c.driveRpm/2,false,c))<1e-10,"Reduction halves free speed");
        double before=Settings.S.robotMass;JsonObject bad=new JsonObject();bad.addProperty("robotMass",-1);try{ProfileStore.apply(bad);throw new AssertionError("Accepted negative mass");}catch(IllegalArgumentException expected){}check(Settings.S.robotMass==before,"Rejected profile leaves settings intact");
        try{ProfileStore.file("../escape");throw new AssertionError("Profile path traversal");}catch(IllegalArgumentException expected){n++;}
        try{JsonObject v=new JsonObject();v.addProperty("capacity",2.5);ProfileStore.validate(v);throw new AssertionError("Fractional capacity");}catch(IllegalArgumentException expected){n++;}
        check(ProfileStore.values(new Settings()).get("coastDrag").getAsDouble()<.03,"Low-drag coast preset");
        Gamepad a=new Gamepad(),b=new Gamepad();JsonArray pads=JsonParser.parseString("[{\"left_stick_y\":-1,\"a\":true},{\"right_trigger\":0.75,\"x\":true}]").getAsJsonArray();StudioInput.accept(pads);StudioInput.apply(a,b);
        check(a.left_stick_y==-1&&a.a,"Primary gamepad axes and buttons");check(b.right_trigger==.75&&b.x,"Independent secondary gamepad");
        Thread.sleep(270);StudioInput.apply(a,b);check(a.left_stick_y==0&&!a.a&&b.right_trigger==0&&!b.x,"Stale input releases both pads");
        JsonArray clipped=JsonParser.parseString("[{\"left_stick_x\":12}]").getAsJsonArray();StudioInput.accept(clipped);StudioInput.apply(a,b);check(a.left_stick_x==1,"Analog input range bounded");StudioInput.clear();StudioInput.apply(a,b);check(a.left_stick_x==0,"Explicit input release");
        c=new Settings();c.capacity=6;Physics m=new Physics(c);check(m.inventory.size()==6,"Configurable ball capacity");
        double speed=Physics.exitSpeed(2000,c);c.shooterTicksPerRev=56;check(Physics.exitSpeed(2000,c)==speed/2,"Shooter encoder gearing changes launch speed");
        System.out.println("PASS: "+n+" studio motor/profile/controller checks");
    }
}
