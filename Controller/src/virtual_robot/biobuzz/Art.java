package virtual_robot.biobuzz;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.image.*;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import java.util.Random;

/** Native 3D mechanical geometry. Every length is meters; no opaque sprite planes. */
public final class Art {
    public static final double S=170;
    public static PhongMaterial material(String color){PhongMaterial m=new PhongMaterial(Color.web(color));m.setSpecularColor(Color.web("#71818c"));m.setSpecularPower(45);return m;}
    public static Box box(Group g,double x,double y,double z,double w,double d,double h,String color){
        Box b=new Box(w*S,h*S,d*S);b.setMaterial(material(color));place(b,x,y,z);g.getChildren().add(b);return b;}
    public static void place(Node n,double x,double y,double z){n.setTranslateX(x*S);n.setTranslateY(-z*S);n.setTranslateZ(y*S);}
    public static Cylinder line(Group g,double x,double y,double z,double xx,double yy,double zz,double radius,String color){
        Point3D a=new Point3D(x*S,-z*S,y*S),b=new Point3D(xx*S,-zz*S,yy*S),v=b.subtract(a),axis=new Point3D(0,1,0).crossProduct(v);
        Cylinder c=new Cylinder(radius*S,v.magnitude(),12);c.setMaterial(material(color));Point3D mid=a.midpoint(b);c.setTranslateX(mid.getX());c.setTranslateY(mid.getY());c.setTranslateZ(mid.getZ());
        if(axis.magnitude()>1e-8)c.getTransforms().add(new Rotate(Math.toDegrees(Math.acos(v.normalize().getY())),axis));
        g.getChildren().add(c);return c;
    }
    public static void ring(Group g,double x,double y,double z,double r,String color){for(int i=0;i<28;i++){double a=i*Math.PI/14,b=(i+1)*Math.PI/14;line(g,x+r*Math.cos(a),y+r*Math.sin(a),z,x+r*Math.cos(b),y+r*Math.sin(b),z,.004,color);}}
    public static void panel(Group g,double[][] p,String color,double opacity){
        TriangleMesh m=new TriangleMesh();for(double[] a:p)m.getPoints().addAll((float)(a[0]*S),(float)(-a[2]*S),(float)(a[1]*S));m.getTexCoords().addAll(0,0);
        for(int i=1;i<p.length-1;i++)m.getFaces().addAll(0,0,i,0,i+1,0);
        MeshView view=new MeshView(m);view.setCullFace(CullFace.NONE);PhongMaterial glass=material(color);glass.setDiffuseColor(Color.web(color,opacity));glass.setSpecularColor(Color.web("#263944"));view.setMaterial(glass);g.getChildren().add(view);
    }
    public static WritableImage foam(int seed){WritableImage image=new WritableImage(96,96);Random r=new Random(seed);
        for(int y=0;y<96;y++)for(int x=0;x<96;x++){double v=.43+r.nextDouble()*.09;if(r.nextDouble()<.06)v-=.09;image.getPixelWriter().setColor(x,y,Color.color(v,v*1.06,v*1.04));}return image;}
    public static PhongMaterial ball(int color){PhongMaterial m=material(color<0?"#efd23e":color==0?"#df3d43":"#347bc9");
        WritableImage map=new WritableImage(192,96);Color base=color<0?Color.web("#e7cc36"):color==0?Color.web("#dc3542"):Color.web("#2a77c7");
        for(int y=0;y<96;y++)for(int x=0;x<192;x++){double dx=(x+((y/16)%2)*8)%24-12,dy=y%16-8;boolean hole=dx*dx+dy*dy<8;
            map.getPixelWriter().setColor(x,y,hole?base.darker().darker():base.interpolate(Color.WHITE,.06*Math.sin(x)));}
        m.setDiffuseMap(map);m.setSpecularPower(14);return m;
    }
}
