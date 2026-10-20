package virtual_robot.biobuzz;
/** Robot-frame x=right, y=forward; wheel order BL, FL, FR, BR as the upstream drivetrain. */
public final class DriveMath {
    public static double[] wheels(double vx,double vy,double omega,double lever){
        return new double[]{vx-vy+omega*lever,-vx-vy+omega*lever,-vx+vy+omega*lever,vx+vy+omega*lever};
    }
    public static double[] traction(double x,double y,double torque,double mass,double lever,double muX,double muY){
        double maxX=muX*mass*Physics.G,maxY=muY*mass*Physics.G;
        double demand=Math.hypot(x/maxX,y/maxY),scale=Math.max(1,demand);
        return new double[]{x/scale,y/scale,Math.max(-Math.min(muX,muY)*mass*Physics.G*lever,
            Math.min(Math.min(muX,muY)*mass*Physics.G*lever,torque))};
    }
}
