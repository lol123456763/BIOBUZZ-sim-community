package virtual_robot.biobuzz;

import java.util.*;

/** SI units. Planar rigid-body drivetrain is handled by upstream dyn4j.
 * Balls use fixed substeps, quadratic drag, gravity, restitution and rolling friction.
 * HIVE shell/contact and tipping are a documented reduced-order model, not CAD rigid-body dynamics. */
public final class Physics {
    public static final double IN=.0254, HALF=72*IN, G=9.80665;
    public final Settings cfg;
    public final List<Ball> balls=new ArrayList<>();
    public final Deque<Ball> inventory=new ArrayDeque<>();
    public final Hive[] hives={new Hive(0,-12.75*IN,-1),new Hive(1,12.75*IN,1)};
    public final Flower[] flowers={new Flower(-24*IN,69.6*IN),new Flower(69.6*IN,24*IN),
            new Flower(24*IN,-69.6*IN),new Flower(-69.6*IN,-24*IN)};
    public double time, robotX,robotY,heading,robotVx,robotVy,robotOmega,lastShot=-10,lastExitSpeed,lastShotMass;
    public int shots,hits,capacityViolations,wrongNectar;
    public String event="Ready - physics values are estimates";
    public Physics(Settings cfg){this.cfg=cfg;reset();}
    public static class Ball {
        public double x,y,z,vx,vy,vz,r,m,wx,wy,wz,pickupTime;
        public int color; // -1 pollen, 0 red, 1 blue
        public String state="floor";
        public Ball(int color,Settings c){this.color=color;r=(color<0?1.4:1.8)*IN;m=color<0?c.pollenMass:c.nectarMass;z=r;}
    }
    public static class Hive {
        public final int color;public final double x;public int side,tips;
        public double tippingUntil;
        public final List<Ball> contents=new ArrayList<>();
        Hive(int color,double x,int side){this.color=color;this.x=x;this.side=side;}
        public double y(){return side*15.44*IN;}
        public double z(){return 59.55*IN;}
    }
    public static class Flower {
        public final double x,y;public final List<Ball> contents=new ArrayList<>();
        Flower(double x,double y){this.x=x;this.y=y;}
        public int owner(){int c=-1;for(Ball b:contents)if(b.color>=0)c=b.color;return c;}
    }
    public synchronized void reset(){
        balls.clear();inventory.clear();time=0;shots=hits=capacityViolations=wrongNectar=0;lastShot=-10;
        for(Hive h:hives){h.side=h.color==0?-1:1;h.tips=0;h.tippingUntil=0;h.contents.clear();
            for(int i=0;i<3;i++){Ball b=new Ball(h.color,cfg);b.state="hive";h.contents.add(b);balls.add(b);}arrange(h);}
        for(Flower f:flowers){f.contents.clear();for(int i=0;i<4;i++){Ball b=new Ball(-1,cfg);b.x=f.x;b.y=f.y;b.z=b.r+i*2*b.r;b.state="flower";f.contents.add(b);balls.add(b);}}
        for(int i=0;i<4;i++){floor(-1,-HALF+(1.4+2.8*i)*IN,-HALF+1.4*IN);floor(-1,HALF-(1.4+2.8*i)*IN,HALF-1.4*IN);}
        // Single-robot setup: 4 of the 16 robot preloads; the other 12 remain reserved off-field.
        preload();
        event="Official-style reset: 16 FLOWER + 8 GARDEN pollen; 4 robot preloads. Other robots absent.";
    }
    public synchronized void preload(){while(inventory.size()<cfg.capacity){Ball b=new Ball(-1,cfg);b.state="held";inventory.add(b);balls.add(b);}}
    public synchronized void practice(){reset();for(int i=0;i<12;i++)floor(-1,-1.1+(i%4)*.65,-.9+(i/4)*.7);event="Practice layout - extra pollen added";}
    public Ball floor(int color,double x,double y){Ball b=new Ball(color,cfg);b.x=x;b.y=y;balls.add(b);return b;}
    public synchronized void humanNectar(){
        int on=0;for(Ball b:balls)if(b.color==cfg.alliance)on++;
        int allowance=time>=90?8:3+hives[cfg.alliance].tips;
        if(on>=Math.min(8,allowance)){event="No NECTAR released yet: tip your HIVE or reach final minute";return;}
        floor(cfg.alliance,cfg.alliance==0?-HALF+2*IN:HALF-2*IN,cfg.alliance==0?36*IN:-36*IN);
    }
    public synchronized void step(double dt,double intakePower,double shooterTicks,double gate){
        if(!Double.isFinite(dt)||dt<=0)return;
        for(double remaining=Math.min(dt,.1);remaining>1e-9;){double h=Math.min(remaining,1.0/480);substep(h,intakePower,shooterTicks,gate);remaining-=h;}
    }
    private void substep(double dt,double intakePower,double shooterTicks,double gate){
        time+=dt;
        double fx=-Math.sin(heading),fy=Math.cos(heading),rx=Math.cos(heading),ry=Math.sin(heading);
        if(intakePower>.1){
            for(Ball b:balls){
                if(!b.state.equals("floor"))continue;
                double dx=b.x-robotX,dy=b.y-robotY,forward=dx*fx+dy*fy,right=dx*rx+dy*ry;
                // Intake is ONLY on the front edge; rear and sides cannot acquire.
                if(forward>=cfg.robotLength/2-.04&&forward<cfg.robotLength/2+.10&&Math.abs(right)<cfg.robotWidth*.4){
                    if(inventory.size()>=cfg.capacity)continue;
                    b.pickupTime+=dt*Math.abs(intakePower);
                    if(b.pickupTime<cfg.intakeCaptureTime)continue;
                    if(b.color>=0&&b.color!=cfg.alliance){wrongNectar++;event="Warning: opposing NECTAR acquired";}
                    b.state="held";inventory.add(b);
                }else b.pickupTime=0;
            }
            for(Flower f:flowers)if(!f.contents.isEmpty()&&inventory.size()<cfg.capacity){Ball b=f.contents.get(0);
                double dx=f.x-robotX,dy=f.y-robotY;
                if(b.color<0&&dx*fx+dy*fy>.15&&dx*fx+dy*fy<.34&&Math.abs(dx*rx+dy*ry)<.17){f.contents.remove(0);inventory.add(b);b.state="held";}}
        }
        if(!inventory.isEmpty()&&time-lastShot>=cfg.feedInterval){
            if(intakePower<-.1){Ball b=inventory.removeFirst();b.state="floor";b.x=robotX+fx*.31;b.y=robotY+fy*.31;b.z=b.r;b.vx=fx*.7;b.vy=fy*.7;lastShot=time;}
            else if(intakePower>.1&&gate<.4&&shooterTicks>100){
                Ball b=inventory.removeFirst();double speed=exitSpeed(shooterTicks,cfg);double a=Math.toRadians(cfg.angleDeg);
                b.state="flight";b.x=robotX+fx*.20;b.y=robotY+fy*.20;b.z=cfg.exitHeight;
                b.vx=fx*speed*Math.cos(a)+robotVx;b.vy=fy*speed*Math.cos(a)+robotVy;b.vz=speed*Math.sin(a);
                b.wx=rx*cfg.backspinRpm*Math.PI/30;b.wy=ry*cfg.backspinRpm*Math.PI/30;b.wz=0;
                lastShot=time;lastExitSpeed=speed;lastShotMass=b.m;shots++;event="Underhand launch: "+String.format(java.util.Locale.US,"%.2f m/s",speed);
            }
        }
        for(Ball b:balls){
            if(!b.state.equals("floor")&&!b.state.equals("flight"))continue;
            double px=b.x,py=b.y,pz=b.z;
            if(b.state.equals("flight")){
                integrate(b,dt,cfg);
                boolean caught=false;
                for(Hive h:hives){
                    if(time<h.tippingUntil)continue;
                    double ny=h.side*Math.sqrt(3)/2,nz=.5;
                    double d0=(py-h.y())*ny+(pz-h.z())*nz;
                    double d1=(b.y-h.y())*ny+(b.z-h.z())*nz;
                    if(d0>b.r&&d1<=b.r){
                        double t=(d0-b.r)/(d0-d1),ix=px+(b.x-px)*t,iy=py+(b.y-py)*t,iz=pz+(b.z-pz)*t;
                        double u=ix-h.x,v=(iy-h.y())*(-h.side*.5)+(iz-h.z())*Math.sqrt(3)/2;
                        // Pentagon opening: 20in wide, lower 7.61in rectangle, roof to 14in.
                        double width=10*IN;if(v>.61*IN)width=10*IN*(7*IN-v)/(6.39*IN);
                        if(Math.abs(u)<width-b.r&&v>-7*IN+b.r&&v<7*IN-b.r){
                            b.state="hive";h.contents.add(b);arrange(h);hits++;caught=true;event="HIVE entry";checkTip(h);break;
                        }
                    }
                }
                if(caught)continue;
                for(Flower f:flowers)if(pz>21.5*IN+b.r&&b.z<=21.5*IN+b.r&&b.vz<0){
                    double t=(pz-(21.5*IN+b.r))/(pz-b.z),ix=px+(b.x-px)*t,iy=py+(b.y-py)*t;
                    if(Math.hypot(ix-f.x,iy-f.y)<2*IN-b.r){b.state="flower";f.contents.add(b);caught=true;event="FLOWER entry";break;}}
                if(caught)continue;
                // Rim and sloping frame contacts share geometry with the visible 3D model.
                rimContacts(b);
                for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})ContactMath.capsule(b,sx*24.73*IN,sy*19.475*IN,0,sx*24.73*IN,0,43.95*IN,.018,cfg.restitution);
                if(b.z<=b.r){ContactMath.floorImpact(b,cfg);if(b.vz<.25){b.vz=0;b.state="floor";}}
            }else{
                double speed=Math.hypot(b.vx,b.vy),decel=cfg.rollingMu*G*dt;
                double factor=speed>0?Math.max(0,1-decel/speed):0;b.vx*=factor;b.vy*=factor;b.x+=b.vx*dt;b.y+=b.vy*dt;
                b.wx=-b.vy/b.r;b.wy=b.vx/b.r;b.wz*=Math.exp(-dt*2);
                // Chassis ball contact; circle-rectangle projection, impulse from moving robot.
                double dx=b.x-robotX,dy=b.y-robotY,r=dx*rx+dy*ry,f=dx*fx+dy*fy;
                double hw=cfg.robotWidth/2,hl=cfg.robotLength/2;
                double cr=Math.max(-hw,Math.min(hw,r)),cf=Math.max(-hl,Math.min(hl,f));
                double er=r-cr,ef=f-cf,dist=Math.hypot(er,ef);
                if(dist<1e-9&&Math.abs(r)<hw&&Math.abs(f)<hl){
                    if(hw-Math.abs(r)<hl-Math.abs(f)){er=Math.copySign(1,r);ef=0;dist=-hw+Math.abs(r);}
                    else{er=0;ef=Math.copySign(1,f);dist=-hl+Math.abs(f);}
                }
                if(dist<b.r){double len=Math.hypot(er,ef);if(len<1e-9)continue;double nx=(er*rx+ef*fx)/len,ny=(er*ry+ef*fy)/len;
                    b.x+=nx*(b.r-dist);b.y+=ny*(b.r-dist);double surfaceVx=robotVx-robotOmega*dy,surfaceVy=robotVy+robotOmega*dx;
                    double vn=(b.vx-surfaceVx)*nx+(b.vy-surfaceVy)*ny;
                    if(vn<0){b.vx-=(1+cfg.restitution)*vn*nx;b.vy-=(1+cfg.restitution)*vn*ny;}}
            }
            // Wall height 12in: airborne balls above it leave the field.
            if(b.z-b.r>12*IN&&(Math.abs(b.x)>HALF+b.r||Math.abs(b.y)>HALF+b.r)){b.state="out";continue;}
            if(b.z-b.r<=12*IN){if(Math.abs(b.x)>HALF-b.r){b.x=Math.copySign(HALF-b.r,b.x);b.vx=-b.vx*cfg.restitution;}
                if(Math.abs(b.y)>HALF-b.r){b.y=Math.copySign(HALF-b.r,b.y);b.vy=-b.vy*cfg.restitution;}}
        }
        for(int i=0;i<balls.size();i++){Ball a=balls.get(i);if(!free(a))continue;
            for(int j=i+1;j<balls.size();j++){Ball b=balls.get(j);if(free(b))ContactMath.spherePair(a,b,cfg.restitution);}}
    }
    private boolean free(Ball b){return b.state.equals("floor")||b.state.equals("flight");}
    private void arrange(Hive h){int i=0;for(Ball b:h.contents){b.x=h.x+(i%3-1)*.08;b.y=h.y()-h.side*.13;b.z=h.z()-.12+(i/3)*.075;i++;}}
    private void rimContacts(Ball b){
        double[][] p={{-10,-7},{10,-7},{10,.61},{0,7},{-10,.61}};
        for(Hive h:hives){if(time<h.tippingUntil)continue;
            for(int i=0;i<5;i++){double[] a=p[i],e=p[(i+1)%5];ContactMath.capsule(b,
                h.x+a[0]*IN,h.y()-h.side*.5*a[1]*IN,h.z()+Math.sqrt(3)/2*a[1]*IN,
                h.x+e[0]*IN,h.y()-h.side*.5*e[1]*IN,h.z()+Math.sqrt(3)/2*e[1]*IN,.009,cfg.restitution);}}
    }
    private void checkTip(Hive h){
        double load=0;for(Ball b:h.contents)load+=b.color<0?1:5.0/3;
        // Preferred official calibration: 8 pollen OR 3 nectar + 3 pollen.
        if(load<8-1e-6)return;
        h.tips++;h.tippingUntil=time+1.0;int old=h.side;h.side=-h.side;
        int i=0;for(Ball b:h.contents){b.state="flight";b.x=h.x+(i++%3-1)*.07;b.y=old*.40;b.z=32*IN;b.vx=(i%3-1)*.2;b.vy=old*.4;b.vz=0;}
        h.contents.clear();event=(h.color==0?"RED":"BLUE")+" HIVE TIP +20 (calibration model)";
    }
    public static double exitSpeed(double ticks,Settings c){return ticks/c.shooterTicksPerRev*Math.PI*c.wheelDiameter*c.speedFactor;}
    public static void integrate(Ball b,double dt,Settings c){
        // RK4 integrates velocity-dependent aerodynamic forces without frame-rate dependent drift.
        double[] a=accel(b,b.vx,b.vy,b.vz,c),v2={b.vx+a[0]*dt/2,b.vy+a[1]*dt/2,b.vz+a[2]*dt/2};
        double[] d=accel(b,v2[0],v2[1],v2[2],c),v3={b.vx+d[0]*dt/2,b.vy+d[1]*dt/2,b.vz+d[2]*dt/2};
        double[] e=accel(b,v3[0],v3[1],v3[2],c),v4={b.vx+e[0]*dt,b.vy+e[1]*dt,b.vz+e[2]*dt};
        double[] f=accel(b,v4[0],v4[1],v4[2],c);
        b.x+=dt/6*(b.vx+2*v2[0]+2*v3[0]+v4[0]);b.y+=dt/6*(b.vy+2*v2[1]+2*v3[1]+v4[1]);b.z+=dt/6*(b.vz+2*v2[2]+2*v3[2]+v4[2]);
        b.vx+=dt/6*(a[0]+2*d[0]+2*e[0]+f[0]);b.vy+=dt/6*(a[1]+2*d[1]+2*e[1]+f[1]);b.vz+=dt/6*(a[2]+2*d[2]+2*e[2]+f[2]);
        double decay=Math.exp(-dt*.18);b.wx*=decay;b.wy*=decay;b.wz*=decay;
    }
    private static double[] accel(Ball b,double vx,double vy,double vz,Settings c){
        double s=Math.sqrt(vx*vx+vy*vy+vz*vz),q=.5*c.airDensity*Math.PI*b.r*b.r/b.m;
        double ax=-q*c.dragCd*s*vx,ay=-q*c.dragCd*s*vy,az=-G-q*c.dragCd*s*vz+c.spinLift;
        double w=Math.sqrt(b.wx*b.wx+b.wy*b.wy+b.wz*b.wz),cx=b.wy*vz-b.wz*vy,cy=b.wz*vx-b.wx*vz,cz=b.wx*vy-b.wy*vx;
        double length=Math.sqrt(cx*cx+cy*cy+cz*cz);
        if(length>1e-9&&s>1e-9){double cl=Math.min(.35,c.magnusSlope*w*b.r/s),lift=q*cl*s*s/length;ax+=cx*lift;ay+=cy*lift;az+=cz*lift;}
        return new double[]{ax,ay,az};
    }
    public synchronized int score(int color){int n=20*hives[color].tips+2*hives[color].contents.size();
        for(Flower f:flowers){if(f.owner()==color)n+=2*Math.max(0,f.contents.size()-1);for(Ball b:f.contents)if(b.color>=0){if(b.color==color)n+=5;break;}}
        for(Ball b:balls)if(b.state.equals("floor")){if(color==0&&b.x<-48*IN&&b.y-b.r<-70*IN)n++;if(color==1&&b.x>48*IN&&b.y+b.r>70*IN)n++;}
        return n;}
}
