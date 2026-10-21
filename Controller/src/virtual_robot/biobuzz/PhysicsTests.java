package virtual_robot.biobuzz;
public class PhysicsTests {
    private static int n;
    private static void check(boolean condition,String name){n++;if(!condition)throw new AssertionError(name);}
    private static void near(double a,double b,double e,String name){check(Math.abs(a-b)<e,name+": "+a+" vs "+b);}
    public static void main(String[] args){
        Settings c=new Settings();c.dragCd=0;c.spinLift=0;Physics.Ball b=new Physics.Ball(-1,c);b.z=2;b.vx=3;b.vz=5;
        for(int i=0;i<480;i++)Physics.integrate(b,1.0/480,c);
        near(b.x,3,1e-8,"vacuum horizontal displacement");near(b.z,2+5-.5*Physics.G,1e-8,"vacuum trajectory");near(b.vz,5-Physics.G,1e-8,"gravity velocity");
        c.dragCd=.47;Physics.Ball drag=new Physics.Ball(-1,c);drag.z=10;drag.vx=10;Physics.integrate(drag,.01,c);check(drag.vx<10,"quadratic drag slows ball");
        Physics p=new Physics(c);p.balls.clear();p.inventory.clear();p.floor(-1,0,-.26);p.step(.02,.8,0,.5);check(p.inventory.isEmpty(),"rear cannot intake");
        p.floor(-1,.26,0);p.step(.02,.8,0,.5);check(p.inventory.isEmpty(),"side cannot intake");
        p.floor(-1,0,.26);p.step(.02,.8,0,.5);check(p.inventory.isEmpty(),"front contact needs roller dwell");
        p.step(.1,.8,0,.5);check(p.inventory.size()==1,"front intakes after roller contact");
        p.preload();p.floor(-1,.05,.27);p.step(.02,.8,0,.5);check(p.inventory.size()==4,"capacity capped at four");
        p.balls.removeIf(a->a.state.equals("floor"));
        int count=p.inventory.size();p.step(.02,.8,2000,.5);check(p.inventory.size()==count,"closed gate blocks shot");
        p.step(.02,.8,2000,.27);check(p.shots==1&&p.inventory.size()==3,"open gate fires one");
        check(p.balls.stream().anyMatch(a->a.state.equals("flight")&&a.vz>0),"underhand launches upward");
        near(Physics.exitSpeed(28,c),Math.PI*.072*.75,1e-10,"ticks to exit speed");
        Physics.Ball floor=p.floor(-1,Physics.HALF-.02,0);floor.vx=2;p.step(.02,0,0,.5);check(floor.vx<=0,"wall reflects ball");
        check(p.hives[0].contents.size()==3&&p.hives[1].contents.size()==3,"official initial nectar");
        p.reset();check(p.balls.stream().filter(a->a.color<0).count()==28,"single robot starting pollen accounting");
        p.balls.removeIf(a->a.state.equals("floor"));
        Physics.Hive hive=p.hives[0];
        for(int i=0;i<3;i++){
            Physics.Ball entry=new Physics.Ball(-1,c);entry.state="flight";entry.x=hive.x;
            entry.y=hive.y()+hive.side*Math.sqrt(3)/2*.12;entry.z=hive.z()+.06;
            entry.vy=-hive.side*Math.sqrt(3)/2;entry.vz=-.5;p.balls.add(entry);p.step(.1,0,0,.5);
        }
        check(p.hits==3,"tilted pentagon opening catches crossing balls");
        check(hive.tips==1&&hive.side==1,"three nectar plus three pollen tips and swaps CELL");
        check(hive.contents.isEmpty(),"tip releases contents");
        check(p.balls.stream().filter(a->a.state.equals("flight")).count()>=6,"tip spills balls into flight");
        // Floor contact must dissipate slip energy, then roll without artificial spin jumps.
        Settings floorCfg=new Settings();floorCfg.rollingMu=0;
        Physics.Ball slide=new Physics.Ball(-1,floorCfg);slide.vx=2;
        Physics.integrateFloor(slide,.001,floorCfg);
        check(slide.wy>0&&slide.wy<slide.vx/slide.r,"sliding generates finite contact torque");
        Physics.integrateFloor(slide,1,floorCfg);
        near(slide.vx,10.0/7,1e-10,"solid sphere sliding settles to five sevenths speed");
        near(slide.wy*slide.r,slide.vx,1e-10,"rolling contact has zero slip");
        floorCfg.rollingMu=.1;
        Physics.Ball roll=new Physics.Ball(-1,floorCfg);roll.vx=1;roll.wy=1/roll.r;
        Physics.integrateFloor(roll,2,floorCfg);
        near(roll.x,1/(2*.1*Physics.G),1e-10,"analytic rolling stopping distance");
        near(roll.vx,0,1e-10,"rolling resistance cannot reverse velocity");
        Physics slow=new Physics(c),fast=new Physics(c);slow.balls.clear();slow.inventory.clear();fast.balls.clear();fast.inventory.clear();
        Physics.Ball sb=slow.floor(-1,1,1),fb=fast.floor(-1,1,1);sb.vx=fb.vx=.5;
        slow.step(.25,0,0,.5);for(int i=0;i<5;i++)fast.step(.05,0,0,.5);
        near(slow.time,.25,1e-10,"long frame preserves elapsed simulation time");
        near(sb.x,fb.x,1e-10,"floor integration invariant to frame batching");
        Settings shotCfg=new Settings();shotCfg.dragCd=0;shotCfg.magnusSlope=0;
        Physics spinShot=new Physics(shotCfg);spinShot.robotOmega=3;spinShot.robotVx=.7;spinShot.robotVy=-.2;
        spinShot.step(.00001,1,2000,.2);
        Physics.Ball shot=spinShot.balls.stream().filter(a->a.state.equals("flight")).findFirst().orElseThrow();
        near(shot.vx,.7-3*.20,1e-10,"launch inherits tangential muzzle velocity");
        near(shot.vy,Physics.exitSpeed(2000,shotCfg)*Math.cos(Math.toRadians(shotCfg.angleDeg))-.2,1e-10,"launch inherits translation");
        double tractionMax=c.robotMass*Physics.G;
        double[] bounded=DriveMath.traction(tractionMax,0,tractionMax*.2,c.robotMass,.2,1,1);
        near(Math.pow(bounded[0]/tractionMax,2)+Math.pow(bounded[2]/(tractionMax*.2),2),1,1e-10,"turning and translation share traction");
        near(DriveMotorModel.torque(1,c.driveRpm/c.driveGearRatio,false,c),0,1e-10,"motor torque vanishes at free speed");
        near(DriveMotorModel.torque(1,0,false,c),c.driveTorque*c.driveGearRatio,1e-10,"motor stall torque");
        check(DriveMotorModel.torque(0,100,true,c)<DriveMotorModel.torque(0,100,false,c),"brake dissipates more than coast");
        Settings geared=new Settings();geared.driveGearRatio=2;
        near(DriveMotorModel.torque(1,0,false,geared),2*c.driveTorque,1e-10,"two to one reduction doubles output stall torque");
        near(DriveMotorModel.torque(1,c.driveRpm/2,false,geared),0,1e-10,"two to one reduction halves free wheel speed");
        double directPower=DriveMotorModel.torque(1,c.driveRpm/2,false,c)*c.driveRpm/2;
        double gearedPower=DriveMotorModel.torque(1,c.driveRpm/4,false,geared)*c.driveRpm/4;
        near(directPower,gearedPower,1e-10,"ideal reduction conserves mechanical power");
        double[] gentle=DriveMath.traction(1,2,.03,c.robotMass,.2,c.gripStrafe,c.gripForward);
        near(gentle[0],1,1e-10,"sub-limit traction preserves requested force");
        near(gentle[2],.03,1e-10,"sub-limit traction preserves requested yaw");
        Physics.Ball frameBall=new Physics.Ball(-1,c);frameBall.state="flight";
        frameBall.x=.00018832+(.60352122+.32347276)/2+.04;frameBall.y=(.47146033+.01285169)/2;frameBall.z=(.01497348+1.04208082)/2;frameBall.vx=-1;
        double frameBefore=frameBall.x;Physics.frameContacts(frameBall,c);
        check(frameBall.x>frameBefore,"CAD tapered leg contacts at actual midpoint");
        Physics.Ball oldFrame=new Physics.Ball(-1,c);oldFrame.x=24.73*Physics.IN;oldFrame.y=frameBall.y;oldFrame.z=(.01497348+1.04208082)/2;
        Physics.frameContacts(oldFrame,c);near(oldFrame.x,24.73*Physics.IN,1e-10,"old constant-x phantom strut removed");
        near(p.flowers[0].x,-.5942,1e-10,"FLOWER center follows CAD placement");
        near(Physics.HALF,1.792859,1e-10,"collision perimeter follows CAD rail envelope");
        Physics wallModel=new Physics(c);wallModel.balls.clear();wallModel.inventory.clear();
        Physics.Ball wallBall=wallModel.floor(-1,Physics.HALF-.02,0);wallBall.vx=1;
        wallModel.step(.01,0,0,.5);
        check(wallBall.x+wallBall.r<=Physics.HALF+1e-9&&wallBall.vx<0,"ball stays inside CAD perimeter");
        System.out.println("PASS: "+n+" independent physics and mechanism checks");
    }
}
