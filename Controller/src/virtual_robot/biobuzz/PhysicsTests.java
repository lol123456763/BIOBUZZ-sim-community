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
        System.out.println("PASS: "+n+" independent physics and mechanism checks");
    }
}
