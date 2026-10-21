package virtual_robot.games;
import virtual_robot.controller.*;
import virtual_robot.biobuzz.*;
import virtual_robot.robots.classes.BioBuzzBot;
import org.dyn4j.dynamics.*;
import org.dyn4j.geometry.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;

public final class BioBuzz extends Game {
    private Canvas overlay;
    public void initialize(){super.initialize();
        // Official CAD Frame Foot A/B bounding rectangles; open underneath.
        for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1}){Body body=new Body();
            org.dyn4j.geometry.Rectangle r=new org.dyn4j.geometry.Rectangle(.047290,.057929);r.translate(.00018832+sx*.602303,sy*.4657255);
            BodyFixture f=body.addFixture(r);f.setFilter(Filters.WALL_FILTER);body.setMass(MassType.INFINITE);world.addBody(body);}
        for(Physics.Flower flower:BioBuzzBot.MODEL.flowers){Body b=new Body();Circle circle=new Circle(2.5*.0254);circle.translate(flower.x,flower.y);
            b.addFixture(circle).setFilter(Filters.WALL_FILTER);b.setMass(MassType.INFINITE);world.addBody(b);}
        overlay=new Canvas(VirtualField.FIELD_WIDTH,VirtualField.FIELD_WIDTH);overlay.setMouseTransparent(true);fieldPane.getChildren().add(overlay);
    }
    public void resetGameElements(){BioBuzzBot.MODEL.reset();}
    public boolean hasHumanPlayer(){return true;}public boolean isHumanPlayerAuto(){return false;}
    public void setHumanPlayerAuto(boolean selected){}public void updateHumanPlayerState(double ms){BioBuzzBot.MODEL.humanNectar();humanPlayerActionRequested=false;}
    public void updateDisplay(){
        GraphicsContext g=overlay.getGraphicsContext2D();double w=VirtualField.FIELD_WIDTH,k=VirtualField.PIXELS_PER_METER;
        g.clearRect(0,0,w,w);synchronized(BioBuzzBot.MODEL){
            for(Physics.Hive h:BioBuzzBot.MODEL.hives){g.setStroke(h.color==0?Color.web("#f46868"):Color.web("#65b4ff"));g.setLineWidth(3);
                g.strokeRect(w/2+(h.x-10*.0254)*k,w/2-(h.y()+3.5*.0254)*k,20*.0254*k,7*.0254*k);}
            for(Physics.Ball b:BioBuzzBot.MODEL.balls){if(b.state.equals("held")||b.state.equals("out")||b.state.equals("hive"))continue;
                double x=w/2+b.x*k,y=w/2-b.y*k,r=b.r*k;
                g.setFill(Color.color(0,0,0,.22));g.fillOval(x-r,y-r,r*2,r*2);
                g.setFill(b.color<0?Color.web("#f8d54d"):b.color==0?Color.web("#ef6565"):Color.web("#62a8f5"));
                double lift=b.state.equals("flight")?Math.min(20,b.z*9):0;g.fillOval(x-r,y-r-lift,r*2,r*2);g.setStroke(Color.web("#333333"));g.strokeOval(x-r,y-r-lift,r*2,r*2);
                if(lift>0){g.setStroke(Color.color(1,1,1,.35));g.strokeLine(x,y,x,y-lift);}}
        }
    }
}
