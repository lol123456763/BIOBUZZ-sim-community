package virtual_robot.biobuzz;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.stage.Stage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** One thread-safe keyboard state shared by the plan and driver-view windows. */
public final class DriverInput {
    private static final Set<KeyCode> pressed=ConcurrentHashMap.newKeySet();
    private static final Set<KeyCode> driving=EnumSet.of(KeyCode.UP,KeyCode.DOWN,KeyCode.LEFT,KeyCode.RIGHT,
        KeyCode.W,KeyCode.A,KeyCode.S,KeyCode.D,KeyCode.Q,KeyCode.E,KeyCode.I,KeyCode.F,KeyCode.SPACE,
        KeyCode.R,KeyCode.L,KeyCode.X,KeyCode.Y,KeyCode.SHIFT,KeyCode.DIGIT1,KeyCode.DIGIT2);
    public static boolean down(KeyCode k){return pressed.contains(k);}
    public static void set(KeyCode k,boolean on){if(on)pressed.add(k);else pressed.remove(k);}
    public static void clear(){pressed.clear();}
    public static void install(Scene scene,Stage stage){
        scene.addEventFilter(KeyEvent.KEY_PRESSED,e->{if(driving.contains(e.getCode())){set(e.getCode(),true);e.consume();}});
        scene.addEventFilter(KeyEvent.KEY_RELEASED,e->{if(driving.contains(e.getCode())){set(e.getCode(),false);e.consume();}});
        stage.focusedProperty().addListener((a,b,c)->{if(!c)clear();});
    }
}
