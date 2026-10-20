package virtual_robot.biobuzz;

import javafx.animation.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import virtual_robot.robots.classes.BioBuzzBot;
import java.util.function.DoubleConsumer;
import java.nio.file.*;

public final class Lab {
    
    public static int virtualPad=1;
    private static Label status=new Label();
    public static void install(BorderPane root,Stage stage){
        Label title=new Label("BIOBUZZ COMMUNITY CODE LAB");title.setStyle("-fx-font-size:21px;-fx-font-weight:bold;-fx-text-fill:#c9ee8a");
        Label subtitle=new Label("Actual Java OpModes + Pedro v3  |  Official field dimensions  |  Adjustable robot estimates");
        Button reset=new Button("Reset field"),practice=new Button("Practice balls"),load=new Button("Load 4 pollen"),nectar=new Button("Human NECTAR");
        reset.setOnAction(e->BioBuzzBot.MODEL.reset());practice.setOnAction(e->BioBuzzBot.MODEL.practice());
        Button view3d=new Button("Open Driver Studio");view3d.setOnAction(e->{try{if(StudioServer.current==null)new StudioServer(virtual_robot.controller.VirtualRobotApplication.getControllerHandle());StudioServer.openBrowser();}catch(Exception ex){status.setText("Studio: "+ex.getMessage());}});
        load.setOnAction(e->BioBuzzBot.MODEL.preload());nectar.setOnAction(e->BioBuzzBot.MODEL.humanNectar());
        CheckBox camera=new CheckBox("Camera"),freeze=new CheckBox("Freeze frames");
        camera.setSelected(true);camera.setOnAction(e->Settings.S.cameraEnabled=camera.isSelected());freeze.setOnAction(e->Settings.S.frozenCamera=freeze.isSelected());

        ComboBox<String> alliance=new ComboBox<>();alliance.getItems().addAll("Red alliance","Blue alliance");alliance.setValue("Red alliance");alliance.setOnAction(e->Settings.S.alliance=alliance.getSelectionModel().getSelectedIndex());
        ComboBox<String> pad=new ComboBox<>();pad.getItems().addAll("Virtual pad 1","Virtual pad 2");pad.setValue("Virtual pad 1");pad.setOnAction(e->virtualPad=pad.getSelectionModel().getSelectedIndex()+1);
        VBox top=new VBox(7,title,subtitle,new HBox(9,reset,practice,load,nectar,alliance,pad,view3d),new HBox(14,camera,freeze));top.setPadding(new Insets(12));
        root.setTop(top);
        Canvas elevation=new Canvas(648,145);Node originalBottom=root.getBottom();
        FlowPane tuning=new FlowPane(12,5);tuning.setPadding(new Insets(6));
        tuning.getChildren().addAll(control("Launch deg",5,85,Settings.S.angleDeg,v->Settings.S.angleDeg=v),
            control("Exit inches",2,35,Settings.S.exitHeight/.0254,v->Settings.S.exitHeight=v*.0254),
            control("Speed factor",.1,1.2,Settings.S.speedFactor,v->Settings.S.speedFactor=v),
            control("Camera pitch",-60,60,Settings.S.cameraPitch,v->Settings.S.cameraPitch=v));
        Label controls=new Label("WASD/arrows drive; Q/E turn; Shift precision; I intake; F fire; Space stop; R reverse; L aim; X/Y gate.\n1 shot speed; 2 drive speed. Keys work in either window. Before INIT: left-click position; right-click face.");
        controls.setPadding(new Insets(5));status.setPadding(new Insets(6));status.setWrapText(true);
        VBox driving=new VBox(3,controls);if(originalBottom!=null)driving.getChildren().add(originalBottom);
        TabPane tabs=new TabPane();Tab driveTab=new Tab("Drive controls",driving),physicsTab=new Tab("Physics & trajectory",new VBox(4,elevation,tuning));
        driveTab.setClosable(false);physicsTab.setClosable(false);tabs.getTabs().addAll(driveTab,physicsTab);
        VBox bottom=new VBox(3,status,tabs);root.setBottom(bottom);
        root.setStyle("-fx-base:#202b33;-fx-background:#172129;-fx-control-inner-background:#111b22;-fx-font-size:12px;-fx-text-fill:#e8f1e8;");
        new AnimationTimer(){long last;public void handle(long now){if(now-last<50_000_000)return;last=now;draw(elevation);}}.start();
    }
    public static void applyKeyboard(virtual_robot.controller.VirtualRobotController c,com.qualcomm.robotcore.hardware.Gamepad g1,com.qualcomm.robotcore.hardware.Gamepad g2){
        com.qualcomm.robotcore.hardware.Gamepad g=virtualPad==1?g1:g2;
        float power=DriverInput.down(javafx.scene.input.KeyCode.SHIFT)?.35f:1;
        if(DriverInput.down(javafx.scene.input.KeyCode.UP)||DriverInput.down(javafx.scene.input.KeyCode.W))g1.left_stick_y=-power;
        if(DriverInput.down(javafx.scene.input.KeyCode.DOWN)||DriverInput.down(javafx.scene.input.KeyCode.S))g1.left_stick_y=power;
        if(DriverInput.down(javafx.scene.input.KeyCode.LEFT)||DriverInput.down(javafx.scene.input.KeyCode.A))g1.left_stick_x=-power;
        if(DriverInput.down(javafx.scene.input.KeyCode.RIGHT)||DriverInput.down(javafx.scene.input.KeyCode.D))g1.left_stick_x=power;
        if(DriverInput.down(javafx.scene.input.KeyCode.Q))g1.right_stick_x=-power;
        if(DriverInput.down(javafx.scene.input.KeyCode.E))g1.right_stick_x=power;
        if(DriverInput.down(javafx.scene.input.KeyCode.I))g.left_trigger=1;
        if(DriverInput.down(javafx.scene.input.KeyCode.F))g.right_trigger=1;
        if(DriverInput.down(javafx.scene.input.KeyCode.SPACE))g.right_bumper=true;
        if(DriverInput.down(javafx.scene.input.KeyCode.R))g.back=true;
        if(DriverInput.down(javafx.scene.input.KeyCode.L)){if(virtualPad==1)g.a=true;else g1.left_trigger=1;}
        if(DriverInput.down(javafx.scene.input.KeyCode.X))g.x=true;
        if(DriverInput.down(javafx.scene.input.KeyCode.Y))g.y=true;
        if(DriverInput.down(javafx.scene.input.KeyCode.DIGIT2))g1.dpad_right=true;
        if(DriverInput.down(javafx.scene.input.KeyCode.DIGIT1))g.dpad_up=true;
        g1.right_trigger_pressed=g1.right_trigger>.5;g1.left_trigger_pressed=g1.left_trigger>.5;
        g2.right_trigger_pressed=g2.right_trigger>.5;g2.left_trigger_pressed=g2.left_trigger>.5;
    }
    private static HBox control(String name,double min,double max,double initial,DoubleConsumer fn){
        Spinner<Double> s=new Spinner<>(min,max,initial,(max-min)/100);s.setPrefWidth(92);s.setEditable(false);s.valueProperty().addListener((a,b,c)->fn.accept(c));return new HBox(5,new Label(name),s);
    }
    private static void draw(Canvas c){Physics m=BioBuzzBot.MODEL;GraphicsContext g=c.getGraphicsContext2D();g.setFill(Color.web("#0e1921"));g.fillRect(0,0,648,145);
        synchronized(m){status.setText(String.format(java.util.Locale.US,"t %.1fs   HELD %d / 4   SHOTS %d   HIVE ENTRIES %d   TIPS R%d B%d   provisional score R%d B%d\n%s",m.time,m.inventory.size(),m.shots,m.hits,m.hives[0].tips,m.hives[1].tips,m.score(0),m.score(1),m.event));
            g.setStroke(Color.web("#344854"));g.setFill(Color.web("#879ba8"));g.fillText("SIDE ELEVATION  •  audience ↔ scoring side  •  both lanes projected",10,17);
            double scale=130/1.9,base=137;g.strokeLine(10,base,638,base);
            for(double z:new double[]{.5,1,1.5}){g.strokeLine(10,base-z*scale,638,base-z*scale);g.fillText(String.format("%.1fm",z),610,base-z*scale-3);}
            for(Physics.Hive h:m.hives){double x=324+h.y()*150;g.setStroke(h.color==0?Color.web("#ef7474"):Color.web("#67b2ff"));g.setLineWidth(3);
                g.strokeLine(x-h.side*3.5*.0254*150,base-65.6*.0254*scale,x+h.side*3.5*.0254*150,base-53.5*.0254*scale);}
            double bx=324+m.robotY*150;g.setFill(Color.web("#658d61"));g.fillRect(bx-20,base-16,40,16);
            g.setFill(Color.web("#ffd474"));g.fillOval(bx-4,base-Settings.S.exitHeight*scale-4,8,8);
            for(Physics.Ball b:m.balls)if(b.state.equals("flight")){g.setFill(b.color<0?Color.GOLD:b.color==0?Color.SALMON:Color.LIGHTBLUE);double r=Math.max(2,b.r*scale);g.fillOval(324+b.y*150-r,base-b.z*scale-r,2*r,2*r);}
            g.setFill(Color.web("#d9e5ed"));g.fillText("Underhand exit "+String.format(java.util.Locale.US,"%.1f in • %.1f° • last %.2f m/s",Settings.S.exitHeight/.0254,Settings.S.angleDeg,m.lastExitSpeed),10,35);
        }
    }
    /** Shared simulator does not modify fields in imported team code. */
    public static void prepare() {}
}
