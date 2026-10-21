package virtual_robot.biobuzz;
import javafx.scene.input.KeyCode;
public final class RealismTests {
    static int n;static void check(boolean b,String message){n++;if(!b)throw new AssertionError(message);}
    static void near(double a,double b,double e,String m){check(Math.abs(a-b)<e,m+": "+a+" vs "+b);}
    static double energy(Physics.Ball b){return .5*b.m*(b.vx*b.vx+b.vy*b.vy+b.vz*b.vz)+.2*b.m*b.r*b.r*(b.wx*b.wx+b.wy*b.wy+b.wz*b.wz);}
    public static void main(String[] args){
        double[] forward=DriveMath.wheels(0,1,0,.381),strafe=DriveMath.wheels(1,0,0,.381),turn=DriveMath.wheels(0,0,1,.381);
        check(forward[0]==forward[1]&&forward[2]==forward[3]&&forward[0]==-forward[2],"forward mecanum symmetry");
        check(strafe[0]==strafe[3]&&strafe[1]==strafe[2]&&strafe[0]==-strafe[1],"strafe uses X wheel pattern");
        check(turn[0]==turn[1]&&turn[1]==turn[2]&&turn[2]==turn[3],"pure rotation wheel symmetry");
        double[] diagonal=DriveMath.wheels(1,1,0,.381);check(diagonal[0]==0&&diagonal[2]==0,"diagonal uses opposite wheel pair");
        double[] traction=DriveMath.traction(1000,1000,1000,13.5,.381,.7,.95);
        near(Math.pow(traction[0]/(.7*13.5*Physics.G),2)+Math.pow(traction[1]/(.95*13.5*Physics.G),2)+Math.pow(traction[2]/(.7*13.5*Physics.G*.381),2),1,1e-9,"translation and yaw share friction ellipsoid");
        near(traction[0]/1000,traction[2]/1000,1e-10,"traction saturation preserves force/torque direction");
        double[] translationOnly=DriveMath.traction(1000,1000,0,13.5,.381,.7,.95);
        check(traction[0]<translationOnly[0],"yaw demand reduces available translation");
        check(Math.abs(traction[2])<=.7*13.5*Physics.G*.381,"yaw torque grip bound");
        Settings c=new Settings();Physics.Ball a=new Physics.Ball(-1,c),b=new Physics.Ball(-1,c);
        a.state=b.state="flight";a.x=-.025;b.x=.025;a.z=b.z=1;a.vx=1;b.vx=-1;
        double before=energy(a)+energy(b);ContactMath.spherePair(a,b,1);near(a.vx,-1,1e-9,"airborne impact swaps equal-mass velocity");
        near(b.vx,1,1e-9,"airborne impact opposite velocity");near(energy(a)+energy(b),before,1e-9,"elastic pair conserves kinetic energy");
        near(a.m*a.vx+b.m*b.vx,0,1e-9,"pair momentum conserved");
        a=new Physics.Ball(-1,c);a.vx=2;a.vy=1;a.vz=-3;before=energy(a);ContactMath.floorImpact(a,c);
        check(energy(a)<=before,"rough floor impact never adds energy");check(Math.abs(a.wx)+Math.abs(a.wy)>0,"rough floor exchanges translation and spin");
        a=new Physics.Ball(-1,c);a.state="flight";a.x=.04;a.z=1;a.vx=-2;
        check(ContactMath.capsule(a,0,-.2,1,0,.2,1,.015,.4),"finite HIVE rim/strut collision detected");check(a.vx>0,"rim reflects incident ball");
        double rotor=ContactMath.flywheelAfterShot(2000,.026,10,.00012);check(rotor>0&&rotor<2000,"ball draws flywheel energy");
        check(ContactMath.flywheelAfterShot(2000,.045,10,.00012)<rotor,"heavier ball draws more flywheel energy");
        a=new Physics.Ball(-1,c);b=new Physics.Ball(-1,c);a.z=b.z=3;a.vy=b.vy=6;b.wx=180;
        for(int i=0;i<240;i++){Physics.integrate(a,1.0/480,c);Physics.integrate(b,1.0/480,c);}
        check(b.z>a.z,"backspin changes arc through Magnus lift");
        double[] red=CameraRig.driver(0,1.6,0),blue=CameraRig.driver(1,1.6,0);
        check(red[0]<-Physics.HALF&&red[0]>-Physics.HALF-54*Physics.IN,"red eye stays in alliance area");
        check(blue[0]>Physics.HALF&&blue[0]<Physics.HALF+54*Physics.IN,"blue eye stays in alliance area");
        near(red[0],-blue[0],1e-9,"driver views opposite sides");near(red[2],1.6,1e-9,"driver is human eye height");
        DriverInput.set(KeyCode.W,true);check(DriverInput.down(KeyCode.W),"shared input key down");DriverInput.clear();check(!DriverInput.down(KeyCode.W),"focus loss releases controls");
        System.out.println("PASS: "+n+" v2 mecanum/contact/spin/driver-view checks");
    }
}
