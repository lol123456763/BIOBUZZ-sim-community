package virtual_robot.biobuzz;
public final class ContactMath {
    public static double flywheelAfterShot(double ticks,double mass,double speed,double inertia){
        return flywheelAfterShot(ticks,mass,speed,inertia,28);
    }
    public static double flywheelAfterShot(double ticks,double mass,double speed,double inertia,double ticksPerRev){
        double omega=ticks/ticksPerRev*2*Math.PI;
        // Rotor energy supplies the ball's kinetic energy; 80% transfer is a configurable-model assumption.
        return Math.copySign(Math.sqrt(Math.max(0,omega*omega-mass*speed*speed/(.8*inertia))),ticks)*ticksPerRev/(2*Math.PI);
    }
    public static void floorImpact(Physics.Ball b,Settings c){
        double normalImpulse=Math.max(0,-(1+c.restitution)*b.vz*b.m);
        b.vz=Math.max(0,-b.vz*c.restitution);b.z=b.r;
        double ux=b.vx-b.wy*b.r,uy=b.vy+b.wx*b.r,speed=Math.hypot(ux,uy);
        if(speed>1e-9){double impulse=Math.min(speed*b.m/3.5,c.floorImpactMu*normalImpulse),jx=-impulse*ux/speed,jy=-impulse*uy/speed;
            b.vx+=jx/b.m;b.vy+=jy/b.m;double inertia=.4*b.m*b.r*b.r;b.wx+=b.r*jy/inertia;b.wy-=b.r*jx/inertia;}
    }
    public static boolean spherePair(Physics.Ball a,Physics.Ball b,double restitution){
        double dx=b.x-a.x,dy=b.y-a.y,dz=b.z-a.z,d=Math.sqrt(dx*dx+dy*dy+dz*dz),r=a.r+b.r;
        if(d>=r)return false;
        double over=r-d;
        if(d<1e-9){dx=1;dy=dz=0;d=1;}
        double nx=dx/d,ny=dy/d,nz=dz/d,ia=1/a.m,ib=1/b.m;
        a.x-=nx*over*ia/(ia+ib);a.y-=ny*over*ia/(ia+ib);a.z-=nz*over*ia/(ia+ib);
        b.x+=nx*over*ib/(ia+ib);b.y+=ny*over*ib/(ia+ib);b.z+=nz*over*ib/(ia+ib);
        double rel=(b.vx-a.vx)*nx+(b.vy-a.vy)*ny+(b.vz-a.vz)*nz;
        if(rel<0){double imp=-(1+restitution)*rel/(ia+ib);
            a.vx-=imp*ia*nx;a.vy-=imp*ia*ny;a.vz-=imp*ia*nz;b.vx+=imp*ib*nx;b.vy+=imp*ib*ny;b.vz+=imp*ib*nz;
            if(a.vz>.25)a.state="flight";if(b.vz>.25)b.state="flight";}
        a.z=Math.max(a.r,a.z);b.z=Math.max(b.r,b.z);return true;
    }
    /** Ball against a finite cylinder/capsule (frame strut or HIVE lip). */
    public static boolean capsule(Physics.Ball b,double ax,double ay,double az,double ex,double ey,double ez,double radius,double e){
        double dx=ex-ax,dy=ey-ay,dz=ez-az,l2=dx*dx+dy*dy+dz*dz;
        double t=l2>0?Math.max(0,Math.min(1,((b.x-ax)*dx+(b.y-ay)*dy+(b.z-az)*dz)/l2)):0;
        double nx=b.x-ax-t*dx,ny=b.y-ay-t*dy,nz=b.z-az-t*dz,d=Math.sqrt(nx*nx+ny*ny+nz*nz),r=b.r+radius;
        if(d>=r||d<1e-9)return false;nx/=d;ny/=d;nz/=d;b.x+=nx*(r-d);b.y+=ny*(r-d);b.z+=nz*(r-d);
        double vn=b.vx*nx+b.vy*ny+b.vz*nz;if(vn<0){b.vx-=(1+e)*vn*nx;b.vy-=(1+e)*vn*ny;b.vz-=(1+e)*vn*nz;}return true;
    }
}
