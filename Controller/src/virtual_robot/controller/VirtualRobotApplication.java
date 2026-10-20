package virtual_robot.controller;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * For internal use only. Main class for the JavaFX application.
 */
public class VirtualRobotApplication extends Application {

    private static VirtualRobotController controllerHandle;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("virtual_robot.fxml"));
        Parent root = (BorderPane)loader.load();
        controllerHandle = loader.getController();
        primaryStage.setTitle("BIOBUZZ Community Code Lab");
        virtual_robot.biobuzz.Lab.install((BorderPane)root,primaryStage);
        javafx.scene.control.ScrollPane scroll=new javafx.scene.control.ScrollPane(root);
        scroll.setFitToWidth(true);
        Scene scene=new Scene(scroll,1120,980);
        scene.getStylesheets().add(getClass().getResource("/virtual_robot/biobuzz/lab.css").toExternalForm());
        primaryStage.setScene(scene);
        virtual_robot.biobuzz.DriverInput.install(scene,primaryStage);
        primaryStage.setResizable(true);
        primaryStage.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                controllerHandle.setConfig(null);
            }
        });
        primaryStage.show();
        if(getParameters().getRaw().contains("--studio")){
            new virtual_robot.biobuzz.StudioServer(controllerHandle);
            if(getParameters().getRaw().contains("--hidden"))primaryStage.setOpacity(0);else primaryStage.setIconified(true);
            if(!getParameters().getRaw().contains("--no-browser"))virtual_robot.biobuzz.StudioServer.openBrowser();
        }
        if(getParameters().getRaw().contains("--smoke")){
            primaryStage.setOpacity(0);
            javafx.animation.PauseTransition delay=new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            delay.setOnFinished(e->controllerHandle.smokeTest(primaryStage));delay.play();
        }
        if(getParameters().getRaw().contains("--smoke-v2")){
            primaryStage.setOpacity(0);
            javafx.animation.PauseTransition delay=new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            delay.setOnFinished(e->controllerHandle.smokeFeel(primaryStage));delay.play();
        }
        if(getParameters().getRaw().contains("--smoke3d")){
            System.out.println("3D support: "+javafx.application.Platform.isSupported(javafx.application.ConditionalFeature.SCENE3D));
            virtual_robot.biobuzz.DriverView renderer=new virtual_robot.biobuzz.DriverView();
            Stage view=renderer.open();view.setOpacity(0);renderer.saveViews(view);
            javafx.animation.PauseTransition snapshot=new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            snapshot.setOnFinished(e->{try{javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(view.getScene().getRoot().snapshot(null,null),null),"png",new java.io.File("preview-3d.png"));}catch(Exception ex){ex.printStackTrace();}});snapshot.play();
        }
    }

    @Override
    public void stop() {
        if(virtual_robot.biobuzz.StudioServer.current!=null)virtual_robot.biobuzz.StudioServer.current.close();
        if (controllerHandle.executorService != null && !controllerHandle.executorService.isShutdown()) {
            controllerHandle.executorService.shutdownNow();
        }
        if (controllerHandle.gamePadExecutorService != null && !controllerHandle.gamePadExecutorService.isShutdown()) {
            controllerHandle.gamePadExecutorService.shutdownNow();
        }
        controllerHandle.gamePadHelper.quit();
    }

    public static VirtualRobotController getControllerHandle(){return controllerHandle;}


    public static void main(String[] args) {
        virtual_robot.biobuzz.Settings.S.load();
        virtual_robot.biobuzz.StudioServer.loadProfile();
        launch(args);
    }
}
