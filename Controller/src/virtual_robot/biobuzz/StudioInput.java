package virtual_robot.biobuzz;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.google.gson.*;

/** Latest-state mailbox: graphics never queues old stick positions. */
public final class StudioInput {
    private static volatile JsonArray pads=new JsonArray();
    private static volatile long received;
    public static volatile boolean connected;
    public static void accept(JsonArray values){pads=values;received=System.nanoTime();connected=true;}
    public static void clear(){pads=new JsonArray();received=0;}
    public static double ageMs(){return received==0?9999:(System.nanoTime()-received)/1e6;}
    public static void apply(Gamepad a,Gamepad b){
        if(!connected)return;
        a.resetValues();b.resetValues();
        if(ageMs()>250)return;
        JsonArray frame=pads;
        if(frame.size()>0)copy(frame.get(0).getAsJsonObject(),a);
        if(frame.size()>1)copy(frame.get(1).getAsJsonObject(),b);
    }
    private static float axis(JsonObject p,String n,double lo){
        try{double v=p.get(n).getAsDouble();return Double.isFinite(v)?(float)Math.max(lo,Math.min(1,v)):0;}catch(Exception e){return 0;}
    }
    private static boolean button(JsonObject p,String n){try{return p.get(n).getAsBoolean();}catch(Exception e){return false;}}
    private static void copy(JsonObject p,Gamepad g){
        g.left_stick_x=axis(p,"left_stick_x",-1);g.left_stick_y=axis(p,"left_stick_y",-1);
        g.right_stick_x=axis(p,"right_stick_x",-1);g.right_stick_y=axis(p,"right_stick_y",-1);
        g.left_trigger=axis(p,"left_trigger",0);g.right_trigger=axis(p,"right_trigger",0);
        g.a=button(p,"a");g.b=button(p,"b");g.x=button(p,"x");g.y=button(p,"y");
        g.left_bumper=button(p,"left_bumper");g.right_bumper=button(p,"right_bumper");
        g.dpad_up=button(p,"dpad_up");g.dpad_down=button(p,"dpad_down");g.dpad_left=button(p,"dpad_left");g.dpad_right=button(p,"dpad_right");
        g.start=button(p,"start");g.back=button(p,"back");g.guide=button(p,"guide");
        g.left_stick_button=button(p,"left_stick_button");g.right_stick_button=button(p,"right_stick_button");
        g.left_trigger_pressed=g.left_trigger>.5;g.right_trigger_pressed=g.right_trigger>.5;
    }
}
