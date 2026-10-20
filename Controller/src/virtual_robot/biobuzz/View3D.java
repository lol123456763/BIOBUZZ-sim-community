package virtual_robot.biobuzz;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import virtual_robot.robots.classes.BioBuzzBot;
import java.util.*;

/** Engineering view of the same SI physics state; decorative CAD detail is omitted. */
public final class View3D {
    private final Group world=new Group(),balls=new Group(),robot=new Group(),cups=new Group(),launcher=new Group();
    private final Rotate orbitX=new Rotate(55,Rotate.X_AXIS),orbitY=new Rotate(-25,Rotate.Y_AXIS);
    private final Map<Physics.Ball,Sphere> spheres=new IdentityHashMap<>();
    private double mouseX,mouseY;
    private int lastRed=0,lastBlue=0;
    private double lastHeight=-1,lastAngle=-1;
    private static final double SCALE=170;
    private static PhongMaterial mat(String color){return new PhongMaterial(Color.web(color));}
    public Stage open(){
        for(int x=0;x<6;x++)for(int y=0;y<6;y++)box(world,(x-2.5)*24*Physics.IN,(y-2.5)*24*Physics.IN,-.012,24*Physics.IN,24*Physics.IN,.02,(x+y)%2==0?"#73837d":"#62756f");
        for(int s:new int[]{-1,1}){box(world,s*Physics.HALF,0,.15,.02,144*Physics.IN,.30,"#304d5d");box(world,0,s*Physics.HALF,.15,144*Physics.IN,.02,.30,"#304d5d");}
        for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})line(world,sx*24.73*Physics.IN,sy*19.475*Physics.IN,0,sx*24.73*Physics.IN,0,43.95*Physics.IN,.018,"#b3c1c6");
        line(world,-24.73*Physics.IN,0,43.95*Physics.IN,24.73*Physics.IN,0,43.95*Physics.IN,.025,"#d4dcdc");
        for(Physics.Flower f:BioBuzzBot.MODEL.flowers){
            for(int i=0;i<4;i++){double a=Math.PI/4+i*Math.PI/2;line(world,f.x+Math.cos(a)*.055,f.y+Math.sin(a)*.055,0,f.x+Math.cos(a)*.055,f.y+Math.sin(a)*.055,21.5*Physics.IN,.006,"#afd45e");}
            ring(world,f.x,f.y,21.5*Physics.IN,2*Physics.IN,"#e9cf68");}
        box(robot,0,0,.11,.4572,.4572,.18,"#203746");
        for(int x:new int[]{-1,1})for(int y:new int[]{-1,1})box(robot,x*.215,y*.15,.06,.055,.11,.10,"#11191e");
        line(robot,-.16,.245,.05,.16,.245,.05,.025,"#b9e383");
        robot.getChildren().add(launcher);
        world.getChildren().addAll(cups,balls,robot,new AmbientLight(Color.web("#e4eeee")));
        Group orbit=new Group(world);orbit.getTransforms().addAll(orbitX,orbitY);
        Group sceneRoot=new Group(orbit);
        PerspectiveCamera camera=new PerspectiveCamera(true);camera.setNearClip(.1);camera.setFarClip(10000);camera.setTranslateZ(-1650);camera.setTranslateY(-80);
        SubScene sub=new SubScene(sceneRoot,980,650,true,SceneAntialiasing.BALANCED);sub.setFill(Color.web("#101c24"));sub.setCamera(camera);
        sub.setOnMousePressed(e->{mouseX=e.getSceneX();mouseY=e.getSceneY();});
        sub.setOnMouseDragged(e->{orbitY.setAngle(orbitY.getAngle()+(e.getSceneX()-mouseX)*.4);orbitX.setAngle(Math.max(-85,Math.min(85,orbitX.getAngle()-(e.getSceneY()-mouseY)*.4)));mouseX=e.getSceneX();mouseY=e.getSceneY();});
        sub.setOnScroll(e->camera.setTranslateZ(Math.max(-2300,Math.min(-350,camera.getTranslateZ()+e.getDeltaY()*2))));
        Label hint=new Label("3D engineering view • drag to orbit, scroll to zoom • run your code in the main window\nFront lime roller = intake; low gold wheel = underhand shooter. Robot properties are adjustable estimates.");hint.setStyle("-fx-text-fill:#deece7;-fx-padding:10px");
        BorderPane pane=new BorderPane(sub);pane.setBottom(hint);pane.setStyle("-fx-background-color:#101c24");
        sub.widthProperty().bind(pane.widthProperty());sub.heightProperty().bind(pane.heightProperty().subtract(60));
        Stage stage=new Stage();stage.setTitle("BIOBUZZ • 3D physics view");stage.setScene(new Scene(pane,980,710));
        AnimationTimer timer=new AnimationTimer(){public void handle(long now){update();}};stage.setOnHidden(e->timer.stop());timer.start();stage.show();return stage;
    }
    private void update(){Physics m=BioBuzzBot.MODEL;synchronized(m){
        if(lastHeight!=Settings.S.exitHeight||lastAngle!=Settings.S.angleDeg){
            launcher.getChildren().clear();line(launcher,-.065,.12,Settings.S.exitHeight,.065,.12,Settings.S.exitHeight,Settings.S.wheelDiameter/2,"#ffc764");
            double a=Math.toRadians(Settings.S.angleDeg);line(launcher,0,.17,Settings.S.exitHeight,0,.17+.12*Math.cos(a),Settings.S.exitHeight+.12*Math.sin(a),.012,"#f1df91");
            lastHeight=Settings.S.exitHeight;lastAngle=Settings.S.angleDeg;}
        robot.setTranslateX(m.robotX*SCALE);robot.setTranslateZ(-m.robotY*SCALE);robot.setRotationAxis(Rotate.Y_AXIS);robot.setRotate(Math.toDegrees(m.heading));
        Set<Physics.Ball> exists=Collections.newSetFromMap(new IdentityHashMap<>());exists.addAll(m.balls);
        spheres.entrySet().removeIf(e->{if(!exists.contains(e.getKey())){balls.getChildren().remove(e.getValue());return true;}return false;});
        for(Physics.Ball b:m.balls){Sphere s=spheres.get(b);if(s==null){s=new Sphere(b.r*SCALE,16);s.setMaterial(mat(b.color<0?"#f5d351":b.color==0?"#ef6969":"#62adf3"));spheres.put(b,s);balls.getChildren().add(s);}
            boolean show=b.state.equals("floor")||b.state.equals("flight")||b.state.equals("flower");s.setVisible(show);
            if(show){s.setTranslateX(b.x*SCALE);s.setTranslateZ(-b.y*SCALE);s.setTranslateY(-b.z*SCALE);}}
        if(lastRed!=m.hives[0].side||lastBlue!=m.hives[1].side){
            cups.getChildren().clear();for(Physics.Hive h:m.hives){cup(h,h.side,true);cup(h,-h.side,false);}
            lastRed=m.hives[0].side;lastBlue=m.hives[1].side;}
    }}
    private void cup(Physics.Hive h,int side,boolean up){
        double cx=h.x,cy=side*15.44*Physics.IN,cz=(up?59.55:36.0)*Physics.IN;
        double[][] p={{-10,-7},{10,-7},{10,.61},{0,7},{-10,.61}};String color=h.color==0?"#ee7373":"#6ab7ff";
        for(int i=0;i<p.length;i++){double[] a=p[i],b=p[(i+1)%p.length];
            double ay=cy-side*.5*a[1]*Physics.IN,az=cz+(up?1:-1)*Math.sqrt(3)/2*a[1]*Physics.IN;
            double by=cy-side*.5*b[1]*Physics.IN,bz=cz+(up?1:-1)*Math.sqrt(3)/2*b[1]*Physics.IN;
            line(cups,cx+a[0]*Physics.IN,ay,az,cx+b[0]*Physics.IN,by,bz,.009,color);
            line(cups,cx+a[0]*Physics.IN,ay,az,cx+a[0]*Physics.IN,ay-side*.264,az-(up?.152:-.152),.004,"#849fa9");}
    }
    private static void box(Group g,double x,double y,double z,double w,double d,double h,String color){Box b=new Box(w*SCALE,h*SCALE,d*SCALE);b.setMaterial(mat(color));b.setTranslateX(x*SCALE);b.setTranslateY(-z*SCALE);b.setTranslateZ(-y*SCALE);g.getChildren().add(b);}
    private static void ring(Group g,double x,double y,double z,double r,String color){for(int i=0;i<24;i++){double a=i*Math.PI/12,b=(i+1)*Math.PI/12;line(g,x+r*Math.cos(a),y+r*Math.sin(a),z,x+r*Math.cos(b),y+r*Math.sin(b),z,.005,color);}}
    private static void line(Group g,double x,double y,double z,double xx,double yy,double zz,double r,String color){
        javafx.geometry.Point3D a=new javafx.geometry.Point3D(x*SCALE,-z*SCALE,-y*SCALE),b=new javafx.geometry.Point3D(xx*SCALE,-zz*SCALE,-yy*SCALE);
        javafx.geometry.Point3D delta=b.subtract(a),axis=new javafx.geometry.Point3D(0,1,0).crossProduct(delta);
        Cylinder c=new Cylinder(r*SCALE,delta.magnitude(),10);c.setMaterial(mat(color));c.setTranslateX((a.getX()+b.getX())/2);c.setTranslateY((a.getY()+b.getY())/2);c.setTranslateZ((a.getZ()+b.getZ())/2);
        if(axis.magnitude()>1e-9)c.getTransforms().add(new Rotate(Math.toDegrees(Math.acos(delta.normalize().getY())),axis));g.getChildren().add(c);
    }
}
