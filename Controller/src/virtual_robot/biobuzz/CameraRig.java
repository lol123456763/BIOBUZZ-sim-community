package virtual_robot.biobuzz;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Affine;
public final class CameraRig {
    /** Eye positions are within the left/right alliance-area footprint; height remains adjustable. */
    public static double[] driver(int alliance,double height,double along){
        return new double[]{(alliance==0?-1:1)*(Physics.HALF+.68),Math.max(-1.0,Math.min(1.0,along)),height};
    }
    public static void aim(PerspectiveCamera c,double ex,double ey,double ez,double tx,double ty,double tz){
        Point3D eye=new Point3D(ex*Art.S,-ez*Art.S,ey*Art.S),target=new Point3D(tx*Art.S,-tz*Art.S,ty*Art.S);
        Point3D f=target.subtract(eye).normalize(),up=Math.abs(f.getY())>.98?new Point3D(0,0,1):new Point3D(0,-1,0);
        Point3D right=f.crossProduct(up).normalize(),down=f.crossProduct(right).normalize();
        c.getTransforms().setAll(new Affine(right.getX(),down.getX(),f.getX(),eye.getX(),right.getY(),down.getY(),f.getY(),eye.getY(),right.getZ(),down.getZ(),f.getZ(),eye.getZ()));
    }
}
