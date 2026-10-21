package virtual_robot.biobuzz;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;

/** Validated profiles are written atomically and are applied while stopped. */
public final class ProfileStore {
    public static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String,double[]> LIMITS=new LinkedHashMap<>();
    static {
        bound("comX",-.4,.4);bound("comY",-.4,.4);bound("yawInertia",0,10);bound("robotMass",2,50);bound("robotWidth",.15,.8);bound("robotLength",.15,.8);bound("robotHeight",.1,1.2);
        bound("driveTrackWidth",.1,.75);bound("driveWheelbase",.1,.75);bound("driveWheelDiameter",.04,.2);bound("driveRpm",50,1500);bound("driveTorque",.05,10);bound("driveGearRatio",.1,10);
        bound("batteryVoltage",8,14);bound("brakeStrength",0,2);bound("coastDrag",0,.3);bound("gripForward",.1,2);bound("gripStrafe",.1,2);bound("driveDamping",0,2);
        bound("angleDeg",5,85);bound("exitHeight",.05,1);bound("wheelDiameter",.02,.2);bound("speedFactor",.1,1.5);
        bound("flywheelInertia",.00001,.01);bound("flywheelRecovery",.02,2);bound("shooterMaxTicks",100,20000);bound("shooterTicksPerRev",1,3000);
        bound("capacity",1,12);bound("intakeCaptureTime",.01,1);bound("feedInterval",.05,3);bound("intakeRate",.1,3);
        bound("pollenMass",.005,.2);bound("nectarMass",.005,.3);bound("dragCd",0,2);bound("airDensity",.1,2);
        bound("restitution",0,.95);bound("rollingMu",0,1);bound("backspinRpm",-5000,5000);bound("magnusSlope",0,1);bound("floorImpactMu",0,1);
        bound("cameraForward",-.8,.8);bound("cameraRight",-.8,.8);bound("cameraYaw",-180,180);bound("cameraRoll",-180,180);bound("cameraFps",1,120);
        bound("cameraHeight",.05,1);bound("cameraPitch",-80,80);bound("cameraFov",10,150);bound("visionLatency",0,.5);bound("alliance",0,1);
    }
    private static void bound(String key,double a,double b){LIMITS.put(key,new double[]{a,b});}
    public static JsonObject values(Settings s){JsonObject o=new JsonObject();LIMITS.forEach((key,range)->{try{o.addProperty(key,((Number)Settings.class.getField(key).get(s)).doubleValue());}catch(Exception e){throw new IllegalStateException(e);}});return o;}
    public static JsonObject limits(){return JSON.toJsonTree(LIMITS).getAsJsonObject();}
    public static Settings validate(JsonObject value){
        Settings staged=new Settings();
        for(String key:value.keySet()){
            if(!LIMITS.containsKey(key))throw new IllegalArgumentException("Unknown setting: "+key);
            double v=value.get(key).getAsDouble();double[] range=LIMITS.get(key);
            if(!Double.isFinite(v)||v<range[0]||v>range[1])throw new IllegalArgumentException(key+" must be between "+range[0]+" and "+range[1]);
            try{Field f=Settings.class.getField(key);if(f.getType()==int.class){if(v!=Math.rint(v))throw new IllegalArgumentException(key+" must be an integer");f.setInt(staged,(int)v);}else f.setDouble(staged,v);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        }
        return staged;
    }
    public static void apply(JsonObject values){Settings candidate=validate(values);for(String key:values.keySet())try{Settings.class.getField(key).set(Settings.S,Settings.class.getField(key).get(candidate));}catch(Exception e){throw new IllegalStateException(e);}}
    public static Path file(String name){if(name==null||!name.matches("[A-Za-z0-9 _-]{1,48}"))throw new IllegalArgumentException("Use 1-48 letters, numbers, spaces, dashes or underscores for the profile name");return Path.of("profiles",name+".json");}
    public static void save(String name,JsonObject data)throws Exception{
        validate(data.getAsJsonObject("robot"));Path target=file(name);Files.createDirectories(target.getParent());
        Path temp=Files.createTempFile(target.getParent(),"save-",".tmp");Files.writeString(temp,JSON.toJson(data));
        try{Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}
    }
    public static JsonObject read(String name)throws Exception{return JsonParser.parseString(Files.readString(file(name))).getAsJsonObject();}
    public static List<String> list()throws Exception{Files.createDirectories(Path.of("profiles"));try(var files=Files.list(Path.of("profiles"))){return files.filter(p->p.toString().endsWith(".json")).map(p->p.getFileName().toString().replaceFirst("\\.json$","")).sorted().toList();}}
}
