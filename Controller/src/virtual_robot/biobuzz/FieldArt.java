package virtual_robot.biobuzz;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/** Nominal plan geometry from official arena and field setup drawings; inches -> pixels. */
public final class FieldArt {
    public static Image image(){
        Canvas c=new Canvas(504,504);GraphicsContext g=c.getGraphicsContext2D();double s=3.5;
        g.setFill(Color.web("#76827f"));g.fillRect(0,0,504,504);
        for(int y=0;y<6;y++)for(int x=0;x<6;x++){
            g.setFill(Color.web((x+y)%2==0?"#7e8986":"#77827f"));g.fillRect(x*84,y*84,84,84);
            g.setStroke(Color.web("#465752"));g.setLineWidth(1);g.strokeRect(x*84+.5,y*84+.5,83,83);}
        g.setStroke(Color.web("#1d2b30"));g.setLineWidth(5);g.strokeRect(1,1,502,502);
        // Gardens A1 and F6, and loading zones A5 and F2.
        g.setStroke(Color.web("#eb6a65"));g.setLineWidth(2*s);g.strokeLine(0,503,23*s,503);
        g.strokeRect(0,24*s,11*s,23*s);
        g.setStroke(Color.web("#62a9ea"));g.strokeLine(121*s,1,144*s,1);g.strokeRect(133*s,97*s,11*s,23*s);
        // Four flowers are at perimeter joins: x=-24/top, y=+24/right, x=+24/bottom, y=-24/left.
        double[][] flowers={{48,2.4},{141.6,48},{96,141.6},{2.4,96}};
        for(double[] f:flowers){g.setFill(Color.web("#506837"));g.fillOval((f[0]-2.5)*s,(f[1]-2.5)*s,5*s,5*s);
            g.setStroke(Color.web("#d7d96b"));g.setLineWidth(2);g.strokeOval((f[0]-2)*s,(f[1]-2)*s,4*s,4*s);}
        // Open A-frame legs and base strips; no solid obstacle under the bridge.
        g.setStroke(Color.web("#c1cbc5"));g.setLineWidth(3);
        double l=(72-49.46/2)*s,r=(72+49.46/2)*s,t=(72-38.95/2)*s,b=(72+38.95/2)*s;
        g.strokeLine(l,t,r,t);g.strokeLine(l,b,r,b);g.strokeLine(l,t,252,252);g.strokeLine(r,t,252,252);g.strokeLine(l,b,252,252);g.strokeLine(r,b,252,252);
        for(double x:new double[]{59.25,84.75})for(double y:new double[]{56.56,87.44}){
            g.setFill(Color.color(x<72?.8:.2,.25,x<72?.25:.8,.16));g.fillRect((x-10)*s,(y-3.5)*s,20*s,7*s);
            g.setStroke(x<72?Color.web("#cf6c68"):Color.web("#5b9dcb"));g.setLineWidth(1);g.strokeRect((x-10)*s,(y-3.5)*s,20*s,7*s);}
        g.setFill(Color.web("#263b36"));g.setFont(javafx.scene.text.Font.font(10));
        g.fillText("SCORING SIDE",211,16);g.fillText("AUDIENCE SIDE",207,492);
        return c.snapshot(null,null);
    }
}
