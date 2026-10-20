package virtual_robot.biobuzz;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import virtual_robot.robots.classes.BioBuzzBot;
import virtual_robot.controller.VirtualRobotApplication;
import java.util.*;
import static virtual_robot.biobuzz.Art.*;

/** Native mechanical geometry and physical alliance-area cameras; shared live physics state. */
public final class DriverView {
    private final Group world=new Group(),balls=new Group(),cups=new Group();
    private final RobotArt robot=new RobotArt();
    private final Rotate orbitX=new Rotate(55,Rotate.X_AXIS),orbitY=new Rotate(-25,Rotate.Y_AXIS);
    private final Map<Physics.Ball,Sphere> spheres=new IdentityHashMap<>();
    private final PhongMaterial[] ballMaterials={Art.ball(-1),Art.ball(0),Art.ball(1)};
    private final PerspectiveCamera camera=new PerspectiveCamera(true);
    private final ComboBox<String> mode=new ComboBox<>();
    private double mouseX,mouseY,distance=1650,eyeHeight=1.60,along=0,lookY=0;
    private int lastRed=0,lastBlue=0;
    private Group orbit;private Label telemetry;
    public Stage open(){
        field();world.getChildren().addAll(cups,balls,robot.root);
        AmbientLight ambient=new AmbientLight(Color.web("#788897"));PointLight light=new PointLight(Color.web("#bdc5bd"));place(light,-1.2,-.8,4);world.getChildren().addAll(ambient,light);
        orbit=new Group(world);Group sceneRoot=new Group(orbit);camera.setNearClip(.3);camera.setFarClip(15000);
        SubScene sub=new SubScene(sceneRoot,1100,680,true,SceneAntialiasing.BALANCED);sub.setFill(Color.web("#18232e"));sub.setCamera(camera);
        mode.getItems().addAll("Driver - Red","Driver - Blue","Audience","Overhead","Chase robot","Free orbit");mode.setValue(Settings.S.alliance==0?"Driver - Red":"Driver - Blue");
        mode.setOnAction(e->{lookY=0;camera.setFieldOfView(65);updateCamera();});
        Spinner<Double> height=new Spinner<>(1.2,1.9,eyeHeight,.05);height.setPrefWidth(85);height.valueProperty().addListener((a,b,c)->eyeHeight=c);
        Spinner<Double> station=new Spinner<>(-1,1,0,.1);station.setPrefWidth(85);station.valueProperty().addListener((a,b,c)->along=c);
        Button run=new Button("INIT / START / STOP"),load=new Button("Load pollen");run.setOnAction(e->VirtualRobotApplication.getControllerHandle().toggleRun());load.setOnAction(e->BioBuzzBot.MODEL.preload());
        HBox bar=new HBox(9,new Label("View"),mode,new Label("Eye height m"),height,new Label("Station offset m"),station,run,load);bar.setStyle("-fx-padding:10px");
        telemetry=new Label();telemetry.setStyle("-fx-text-fill:#bedd9e;-fx-font-family:Consolas;-fx-padding:7px");
        Label hint=new Label("WASD / arrows: drive | Q/E: turn | I: intake | F: fire | Space: stop | Shift: precision | 2: drive speed | 1: shot speed\nDrag: look around / orbit. Scroll: field of view / orbit distance. Driver cameras stay inside their alliance area.");hint.setStyle("-fx-text-fill:#d8e3e8;-fx-padding:8px");
        BorderPane pane=new BorderPane(sub);pane.setTop(new VBox(bar,telemetry));pane.setBottom(hint);pane.setStyle("-fx-background-color:#18232e");
        sub.widthProperty().bind(pane.widthProperty());sub.heightProperty().bind(pane.heightProperty().subtract(143));
        sub.setOnMousePressed(e->{mouseX=e.getSceneX();mouseY=e.getSceneY();sub.requestFocus();});
        sub.setOnMouseDragged(e->{if(mode.getValue().equals("Free orbit")){orbitY.setAngle(orbitY.getAngle()+(e.getSceneX()-mouseX)*.35);orbitX.setAngle(Math.max(5,Math.min(85,orbitX.getAngle()-(e.getSceneY()-mouseY)*.35)));}
            else lookY=Math.max(-1.5,Math.min(1.5,lookY+(e.getSceneX()-mouseX)*.006));mouseX=e.getSceneX();mouseY=e.getSceneY();});
        sub.setOnScroll(e->{if(mode.getValue().equals("Free orbit"))distance=Math.max(900,Math.min(2600,distance-e.getDeltaY()*2));else camera.setFieldOfView(Math.max(35,Math.min(90,camera.getFieldOfView()-e.getDeltaY()*.08)));});
        Stage stage=new Stage();stage.setTitle("BIOBUZZ Community - driver station & 3D field");Scene scene=new Scene(pane,1160,850);scene.getStylesheets().add(getClass().getResource("lab.css").toExternalForm());
        stage.setScene(scene);DriverInput.install(scene,stage);
        AnimationTimer timer=new AnimationTimer(){long last;public void handle(long now){if(now-last<16_000_000)return;last=now;update();}};
        stage.setOnHidden(e->{timer.stop();DriverInput.clear();});timer.start();camera.setFieldOfView(65);update();stage.show();return stage;
    }
    private void field(){
        for(int x=0;x<6;x++)for(int y=0;y<6;y++){Box tile=box(world,(x-2.5)*24*Physics.IN,(y-2.5)*24*Physics.IN,-.009,24*Physics.IN-.002,24*Physics.IN-.002,.018,"#818b85");
            PhongMaterial m=material("#c1c8c2");m.setDiffuseMap(foam((x+y)%2));m.setSpecularColor(Color.web("#252c29"));tile.setMaterial(m);}
        for(int side:new int[]{-1,1})box(world,side*(Physics.HALF+.686),0,-.025,54*Physics.IN,97*Physics.IN,.025,side<0?"#5a262d":"#243d5d");
        for(int s:new int[]{-1,1}){
            Box a=box(world,s*Physics.HALF,0,.15,.013,144*Physics.IN,.30,"#dbe8ed"),b=box(world,0,s*Physics.HALF,.15,144*Physics.IN,.013,.30,"#dbe8ed");a.setOpacity(.18);b.setOpacity(.18);
            for(double h:new double[]{.015,.30}){line(world,s*Physics.HALF,-Physics.HALF,h,s*Physics.HALF,Physics.HALF,h,.009,"#7c8b99");line(world,-Physics.HALF,s*Physics.HALF,h,Physics.HALF,s*Physics.HALF,h,.009,"#7c8b99");}}
        for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})line(world,sx*24.73*Physics.IN,sy*19.475*Physics.IN,0,sx*24.73*Physics.IN,0,43.95*Physics.IN,.018,"#a8b8c0");
        line(world,-24.73*Physics.IN,0,43.95*Physics.IN,24.73*Physics.IN,0,43.95*Physics.IN,.023,"#b5c5cd");box(world,0,0,.92,.58,.018,.16,"#d3bc5b");
        tape(-72,24,-61,24,"#dd424d");tape(-61,24,-61,47,"#dd424d");tape(-61,47,-72,47,"#dd424d");
        tape(72,-24,61,-24,"#4586da");tape(61,-24,61,-47,"#4586da");tape(61,-47,72,-47,"#4586da");tape(-72,-71,-49,-71,"#dd424d");tape(49,71,72,71,"#4586da");
        for(Physics.Flower f:BioBuzzBot.MODEL.flowers){for(int i=0;i<4;i++){double a=Math.PI/4+i*Math.PI/2;line(world,f.x+Math.cos(a)*.055,f.y+Math.sin(a)*.055,0,f.x+Math.cos(a)*.055,f.y+Math.sin(a)*.055,21.5*Physics.IN,.006,"#92c84b");}
            for(double h:new double[]{.01,.29,21.5*Physics.IN})ring(world,f.x,f.y,h,2*Physics.IN,h<.05?"#444c54":"#e3c849");}
    }
    private void tape(double x,double y,double xx,double yy,String color){box(world,(x+xx)/2*Physics.IN,(y+yy)/2*Physics.IN,.002,Math.max(1.3,Math.abs(xx-x))*Physics.IN,Math.max(1.3,Math.abs(yy-y))*Physics.IN,.001,color);}
    private void updateCamera(){String m=mode.getValue();orbit.getTransforms().clear();camera.setTranslateX(0);camera.setTranslateY(0);camera.setTranslateZ(0);
        if(m.equals("Free orbit")){camera.setFieldOfView(38);orbit.getTransforms().setAll(orbitX,orbitY);camera.getTransforms().clear();camera.setTranslateZ(-distance);camera.setTranslateY(-80);return;}
        if(m.startsWith("Driver")){double[] p=CameraRig.driver(m.endsWith("Red")?0:1,eyeHeight,along);CameraRig.aim(camera,p[0],p[1],p[2],0,lookY,.55);}
        else if(m.equals("Audience"))CameraRig.aim(camera,0,-3.4,1.65,lookY,0,.4);
        else if(m.equals("Overhead"))CameraRig.aim(camera,0,0,4.6,0,0,0);
        else {Physics p=BioBuzzBot.MODEL;double fx=-Math.sin(p.heading),fy=Math.cos(p.heading);CameraRig.aim(camera,p.robotX-fx*1.15,p.robotY-fy*1.15,1.05,p.robotX+fx*.5,p.robotY+fy*.5,.2);}
    }
    private void update(){Physics m=BioBuzzBot.MODEL;synchronized(m){robot.update(m);updateCamera();
        Set<Physics.Ball> exists=Collections.newSetFromMap(new IdentityHashMap<>());exists.addAll(m.balls);
        spheres.entrySet().removeIf(e->{if(!exists.contains(e.getKey())){balls.getChildren().remove(e.getValue());return true;}return false;});
        for(Physics.Ball b:m.balls){Sphere s=spheres.get(b);if(s==null){s=new Sphere(b.r*S,20);s.setMaterial(ballMaterials[b.color+1]);spheres.put(b,s);balls.getChildren().add(s);}
            boolean show=b.state.equals("floor")||b.state.equals("flight")||b.state.equals("flower")||b.state.equals("hive");s.setVisible(show);
            if(show){place(s,b.x,b.y,b.z);s.setRotationAxis(Rotate.X_AXIS);s.setRotate(Math.toDegrees(m.time*b.wx));}}
        if(lastRed!=m.hives[0].side||lastBlue!=m.hives[1].side){cups.getChildren().clear();for(Physics.Hive h:m.hives){cup(h,h.side,true);cup(h,-h.side,false);}lastRed=m.hives[0].side;lastBlue=m.hives[1].side;}
        telemetry.setText(String.format(java.util.Locale.US,"HELD %d/4   SHOTS %d   SPEED %.2f m/s   FLYWHEEL %.0f ticks/s   %s",m.inventory.size(),m.shots,Math.hypot(m.robotVx,m.robotVy),BioBuzzBot.current==null?0:BioBuzzBot.current.speed(),m.event));
    }}
    private void cup(Physics.Hive h,int side,boolean up){double cx=h.x,cy=side*15.44*Physics.IN,cz=(up?59.55:36)*Physics.IN,sign=up?1:-1;
        double[][] p={{-10,-7},{10,-7},{10,.61},{0,7},{-10,.61}},front=new double[5][3],back=new double[5][3];String color=h.color==0?"#e6535b":"#478dd9";
        for(int i=0;i<5;i++){front[i]=new double[]{cx+p[i][0]*Physics.IN,cy-side*.5*p[i][1]*Physics.IN,cz+sign*Math.sqrt(3)/2*p[i][1]*Physics.IN};back[i]=new double[]{front[i][0],front[i][1]-side*.264,front[i][2]-sign*.152};}
        for(int i=0;i<5;i++){int j=(i+1)%5;double[] a=front[i],b=front[j],d=back[i],e=back[j];line(cups,a[0],a[1],a[2],b[0],b[1],b[2],.009,color);line(cups,d[0],d[1],d[2],e[0],e[1],e[2],.007,color);
            line(cups,a[0],a[1],a[2],d[0],d[1],d[2],.004,"#9bacb5");panel(cups,new double[][]{a,b,e,d},"#b4d1db",.19);}panel(cups,back,color,.25);
    }
    public void saveViews(Stage stage){String[] choices={"Driver - Red","Driver - Blue","Audience","Overhead","Chase robot","Free orbit"};
        for(int i=0;i<choices.length;i++){final int j=i;PauseTransition wait=new PauseTransition(Duration.seconds(.25+i*.30));wait.setOnFinished(e->{mode.setValue(choices[j]);update();try{javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(stage.getScene().getRoot().snapshot(null,null),null),"png",new java.io.File("preview-v2-"+j+".png"));}catch(Exception ex){throw new RuntimeException(ex);}});wait.play();}
    }
}
