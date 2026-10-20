package virtual_robot.biobuzz;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import virtual_robot.robots.classes.BioBuzzBot;
import static virtual_robot.biobuzz.Art.*;

/** Open-channel chassis, four handed 45-degree roller wheels, front intake and underhand launcher. */
public final class RobotArt {
    public final Group root=new Group(),launcher=new Group(),intake=new Group();
    private final Group[] wheels=new Group[4];
    private double oldHeight=-1,oldAngle=-1;
    public RobotArt(){
        box(root,0,0,.075,.365,.385,.018,"#303b45");
        for(int s:new int[]{-1,1}){box(root,s*.168,0,.14,.025,.405,.07,"#b7c3c8");
            for(int i=0;i<13;i++){Sphere hole=new Sphere(.003*Art.S,5);hole.setMaterial(material("#26313a"));place(hole,s*.168,-.18+i*.03,.177);root.getChildren().add(hole);}}
        for(int s:new int[]{-1,1})box(root,0,s*.19,.13,.36,.024,.055,"#b7c3c8");
        box(root,0,-.08,.13,.13,.15,.06,"#141e25");box(root,.07,-.09,.185,.08,.10,.035,"#de6544");
        line(root,-.04,-.12,.16,.07,-.08,.205,.003,"#e36055");line(root,-.02,-.13,.16,.08,-.07,.205,.003,"#e9cb60");
        // BL, FL, FR, BR. Left and right rollers form the mecanum X arrangement.
        double[][] locations={{-.21,-.15},{-.21,.15},{.21,.15},{.21,-.15}};
        for(int i=0;i<4;i++){Group w=new Group();wheels[i]=w;place(w,locations[i][0],locations[i][1],.054);root.getChildren().add(w);
            line(w,-.024,0,0,.024,0,0,.035,"#d0b46a");line(w,-.031,0,0,.031,0,0,.013,"#26343b");
            int handed=(i==0||i==2)?1:-1;
            for(int j=0;j<10;j++){double a=j*Math.PI/5,cy=Math.sin(a)*.042,cz=Math.cos(a)*.042,ty=Math.cos(a)*handed,tz=-Math.sin(a)*handed;
                line(w,-.023,cy-ty*.023,cz-tz*.023,.023,cy+ty*.023,cz+tz*.023,.008,"#242b31");}
            w.setRotationAxis(Rotate.X_AXIS);
        }
        for(int s:new int[]{-1,1}){box(root,s*.147,.22,.066,.015,.08,.075,"#a9bac3");line(root,s*.146,.025,.09,s*.146,.255,.055,.007,"#d2dadd");}
        place(intake,0,.252,.055);root.getChildren().add(intake);line(intake,-.14,0,0,.14,0,0,.026,"#91c74d");
        for(int i=0;i<9;i++)box(intake,-.125+i*.031,0,0,.012,.057,.057,"#badc71");intake.setRotationAxis(Rotate.X_AXIS);
        // Visible conveyor guides and belt from the front roller to the low launcher.
        box(root,0,.115,.085,.19,.19,.012,"#202831");for(int i=0;i<6;i++)box(root,0,.025+i*.03,.096,.185,.008,.008,"#5e6c76");
        box(root,0,-.19,.16,.16,.008,.06,"#d94b4e");
        root.getChildren().add(launcher);
    }
    public void update(Physics m){
        place(root,m.robotX,m.robotY,0);root.setRotationAxis(Rotate.Y_AXIS);root.setRotate(-Math.toDegrees(m.heading));
        BioBuzzBot b=BioBuzzBot.current;if(b!=null){for(int i=0;i<4;i++)wheels[i].setRotate(Math.toDegrees(b.wheelPhases[i]));intake.setRotate(Math.toDegrees(b.intakePhase));}
        if(oldHeight!=Settings.S.exitHeight||oldAngle!=Settings.S.angleDeg){launcher.getChildren().clear();
            double z=Settings.S.exitHeight,a=Math.toRadians(Settings.S.angleDeg);
            for(int side:new int[]{-1,1}){box(launcher,side*.10,.10,z/2,.012,.13,z,"#8297a6");
                line(launcher,side*.085,.10,z-.035,side*.085,.10+.16*Math.cos(a),z-.035+.16*Math.sin(a),.007,"#ccd4d4");}
            line(launcher,-.085,.10,z,.085,.10,z,Settings.S.wheelDiameter/2,"#e2b85a");
            line(launcher,.09,.10,z,.14,.10,z,.019,"#303b48");
            // Curved underhand guide: incoming ball rises around the front of a low flywheel.
            for(int i=0;i<9;i++){double t0=-Math.PI/2+i*(a+Math.PI/2)/9,t1=-Math.PI/2+(i+1)*(a+Math.PI/2)/9;
                for(int side:new int[]{-1,1})line(launcher,side*.065,.10+.062*Math.cos(t0),z+.062*Math.sin(t0),side*.065,.10+.062*Math.cos(t1),z+.062*Math.sin(t1),.004,"#b7d9df");}
            oldHeight=z;oldAngle=Settings.S.angleDeg;}
    }
}
