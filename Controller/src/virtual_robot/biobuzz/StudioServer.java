package virtual_robot.biobuzz;

import com.google.gson.*;
import com.sun.net.httpserver.*;
import virtual_robot.controller.*;
import virtual_robot.robots.classes.BioBuzzBot;
import javafx.application.Platform;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/** Local-only graphics/input bridge. The existing JVM runs the actual Java OpMode. */
public final class StudioServer {
    public static StudioServer current;
    private final VirtualRobotController controller;
    private final HttpServer server;
    private final String token=UUID.randomUUID().toString();
    private final ExecutorService requests=Executors.newFixedThreadPool(8,r->{Thread t=new Thread(r,"studio-http");t.setDaemon(true);return t;});
    private final ScheduledExecutorService timer=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"studio-clock");t.setDaemon(true);return t;});
    private final Path web=Path.of("web").toAbsolutePath().normalize();
    private volatile String telemetry="",mode="practice";
    private volatile long matchStart=0;
    private volatile boolean importing=false;
    private JsonObject session=new JsonObject();
    public StudioServer(VirtualRobotController c)throws Exception{
        controller=c;BioBuzzBot.current.place(0,-1.3,0);server=HttpServer.create(new InetSocketAddress("127.0.0.1",8765),32);server.setExecutor(requests);
        if(Files.exists(ProfileStore.file("Current")))session=ProfileStore.read("Current");
        server.createContext("/",this::handle);server.start();current=this;
        timer.scheduleAtFixedRate(()->Platform.runLater(()->{
            telemetry=controller.studioTelemetry();
            if(!mode.equals("practice")&&matchStart>0&&elapsed()>duration()){controller.studioStop();matchStart=0;}
        }),0,100,TimeUnit.MILLISECONDS);
        System.out.println("BIOBUZZ STUDIO http://127.0.0.1:8765");
    }
    public static void loadProfile(){try{if(Files.exists(ProfileStore.file("Current")))ProfileStore.apply(ProfileStore.read("Current").getAsJsonObject("robot"));}catch(Exception e){System.err.println("Profile not loaded: "+e.getMessage());}}
    public static void openBrowser(){try{new ProcessBuilder("rundll32","url.dll,FileProtocolHandler","http://127.0.0.1:8765").start();}catch(Exception e){System.err.println("Open http://127.0.0.1:8765 in Edge or Chrome");}}
    public void close(){StudioInput.clear();server.stop(0);timer.shutdownNow();requests.shutdownNow();}
    private double elapsed(){return matchStart==0?0:(System.nanoTime()-matchStart)/1e9;}
    private int duration(){return mode.equals("match-auto")?30:120;}
    private <T>T fx(Callable<T> task)throws Exception{CompletableFuture<T> f=new CompletableFuture<>();Platform.runLater(()->{try{f.complete(task.call());}catch(Throwable e){f.completeExceptionally(e);}});return f.get(5,TimeUnit.SECONDS);}
    private void stopped(){if(!controller.studioPhase().equals("stopped")||importing)throw new IllegalStateException("Stop the robot before changing this setting");}
    private void handle(HttpExchange e)throws IOException{
        try{
            String host=e.getRequestHeaders().getFirst("Host");
            if(!"127.0.0.1:8765".equals(host)&&!"localhost:8765".equals(host)){send(e,403,Map.of("error","Local simulator only"));return;}
            String path=e.getRequestURI().getPath();
            if(e.getRequestMethod().equals("POST")){
                String origin=e.getRequestHeaders().getFirst("Origin");
                if(!token.equals(e.getRequestHeaders().getFirst("X-Sim-Token")) || (origin!=null&&!origin.equals("http://"+host))){send(e,403,Map.of("error","Invalid local session"));return;}
                byte[] bytes=e.getRequestBody().readNBytes(32*1024*1024+1);
                if(bytes.length>32*1024*1024){send(e,413,Map.of("error","Upload exceeds 32 MB"));return;}
                JsonObject body=JsonParser.parseString(new String(bytes,StandardCharsets.UTF_8)).getAsJsonObject();
                Object result=switch(path){
                    case "/api/input" -> {StudioInput.accept(body.getAsJsonArray("pads"));yield Map.of("ok",true);}
                    case "/api/action" -> fx(()->action(body));
                    case "/api/profile" -> fx(()->profile(body));
                    case "/api/import" -> importCode(body);
                    case "/api/restart" -> {stopped();restart();yield Map.of("ok",true);}
                    default -> throw new IllegalArgumentException("Unknown endpoint");
                };send(e,200,result);return;
            }
            if(!e.getRequestMethod().equals("GET")){send(e,405,Map.of("error","Use GET or POST"));return;}
            switch(path){
                case "/api/info": send(e,200,fx(()->Map.of("token",token,"opModes",controller.studioOpModes(),"selected",controller.studioSelected(),"robot",ProfileStore.values(Settings.S),"defaults",ProfileStore.values(new Settings()),"limits",ProfileStore.limits(),"profiles",ProfileStore.list(),"session",session)));return;
                case "/api/state": send(e,200,state());return;
            }
            Path file=web.resolve(path.equals("/")?"index.html":path.substring(1)).normalize();
            if(!file.startsWith(web)||!Files.isRegularFile(file)){send(e,404,Map.of("error","File not found"));return;}
            String type=file.toString().endsWith(".js")?"text/javascript":file.toString().endsWith(".css")?"text/css":file.toString().endsWith(".wasm")?"application/wasm":file.toString().endsWith(".html")?"text/html":"application/octet-stream";
            e.getResponseHeaders().set("Content-Type",type+ (type.startsWith("text/")?"; charset=utf-8":""));
            e.getResponseHeaders().set("Cache-Control",file.startsWith(web.resolve("vendor"))?"public, max-age=86400":"no-store");
            e.sendResponseHeaders(200,Files.size(file));try(var out=e.getResponseBody()){Files.copy(file,out);}
        }catch(Exception ex){Throwable root=ex;while(root.getCause()!=null)root=root.getCause();send(e,400,Map.of("error",String.valueOf(root.getMessage())));}
        finally{e.close();}
    }
    private void send(HttpExchange e,int code,Object obj)throws IOException{byte[] b=ProfileStore.JSON.toJson(obj).getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");e.getResponseHeaders().set("Cache-Control","no-store");e.sendResponseHeaders(code,b.length);try(var o=e.getResponseBody()){o.write(b);}}
    private Object action(JsonObject b){
        String action=b.get("action").getAsString();
        switch(action){
            case "init": stopped();if(controller.studioSelected().isEmpty())throw new IllegalStateException("No enabled team OpModes. Import a @TeleOp or @Autonomous program without @Disabled, compile, then restart.");controller.toggleRun();break;
            case "start": if(!controller.studioPhase().equals("initialized"))throw new IllegalStateException("Initialize first");controller.toggleRun();matchStart=System.nanoTime();break;
            case "stop": controller.studioStop();matchStart=0;break;
            case "shutdown": controller.studioStop();timer.schedule(Platform::exit,300,TimeUnit.MILLISECONDS);break;
            case "select": stopped();controller.studioSelect(b.get("name").getAsString());break;
            case "mode": stopped();String m=b.get("mode").getAsString();if(!List.of("practice","match-auto","match-teleop").contains(m))throw new IllegalArgumentException("Unknown mode");mode=m;matchStart=0;break;
            case "reset": stopped();BioBuzzBot.MODEL.reset();break;
            case "practice": stopped();BioBuzzBot.MODEL.practice();break;
            case "preload": if(!mode.equals("practice"))stopped();BioBuzzBot.MODEL.preload();break;
            case "nectar": BioBuzzBot.MODEL.humanNectar();break;
            case "place": stopped();double x=b.get("x").getAsDouble(),y=b.get("y").getAsDouble(),h=b.get("heading").getAsDouble();if(!Double.isFinite(x+y+h))throw new IllegalArgumentException("Invalid pose");BioBuzzBot.current.place(x,y,h);break;
            default: throw new IllegalArgumentException("Unknown action");
        }
        return Map.of("phase",controller.studioPhase());
    }
    private Object profile(JsonObject b)throws Exception{
        stopped();String operation=b.get("operation").getAsString(),name=b.has("name")?b.get("name").getAsString():"Current";
        if(operation.equals("save")){ProfileStore.save(name,b.getAsJsonObject("data"));return Map.of("profiles",ProfileStore.list());}
        if(operation.equals("load"))return ProfileStore.read(name);
        if(!operation.equals("apply"))throw new IllegalArgumentException("Unknown profile operation");
        JsonObject candidate=b.getAsJsonObject("data");ProfileStore.validate(candidate.getAsJsonObject("robot"));
        ProfileStore.save("Current",candidate);ProfileStore.apply(candidate.getAsJsonObject("robot"));session=candidate.deepCopy();
        controller.setConfig(null);BioBuzzBot.MODEL.reset();BioBuzzBot.current.place(0,-1.3,0);return Map.of("robot",ProfileStore.values(Settings.S));
    }
    private Object state(){
        Physics m=BioBuzzBot.MODEL;Map<String,Object> data=new LinkedHashMap<>();
        synchronized(m){
            data.put("time",m.time);data.put("robot",new double[]{m.robotX,m.robotY,m.heading,m.robotVx,m.robotVy,m.robotOmega});
            data.put("balls",m.balls.stream().filter(b->!b.state.equals("out")&&!b.state.equals("held")).map(b->new Object[]{b.x,b.y,b.z,b.r,b.color,b.state}).toList());
            data.put("hives",Arrays.stream(m.hives).map(h->new int[]{h.side,h.tips,h.contents.size()}).toList());
            data.put("held",m.inventory.size());data.put("shots",m.shots);data.put("hits",m.hits);data.put("event",m.event);data.put("scores",new int[]{m.score(0),m.score(1)});
        }
        BioBuzzBot bot=BioBuzzBot.current;data.put("flywheel",bot==null?0:bot.speed());data.put("intake",bot==null?0:bot.intakePower());
        data.put("motorPowers",bot==null?new double[4]:bot.drivePowers());
        data.put("vision",bot==null?Map.of():bot.visionDiagnostics());
        data.put("wheels",bot==null?new double[4]:bot.wheelPhases.clone());data.put("phase",controller.studioPhase());
        data.put("mode",mode);data.put("remaining",Math.max(0,duration()-elapsed()));data.put("inputAge",StudioInput.ageMs());data.put("telemetry",telemetry);return data;
    }
    private synchronized Object importCode(JsonObject body)throws Exception{
        stopped();importing=true;
        try{
            Path run=Path.of("build","import-"+UUID.randomUUID()),stage=run.resolve("sources"),compiled=run.resolve("classes");Files.createDirectories(stage);Files.createDirectories(compiled);
            JsonArray files=body.getAsJsonArray("files");if(files.size()==0||files.size()>1000)throw new IllegalArgumentException("Select 1-1000 Java files");
            int count=0,skipped=0;
            for(JsonElement elem:files){JsonObject f=elem.getAsJsonObject();String text=f.get("content").getAsString(),name=f.get("name").getAsString();name=name.replace('\\','/');name=name.substring(name.lastIndexOf('/')+1);
                JavaSource source=JavaSource.parse(name,text);Path rel=source.relativePath();text=source.content();
                if(Files.exists(Path.of("Controller/src").resolve(rel))){skipped++;continue;}
                Path out=stage.resolve(rel).normalize();if(!out.startsWith(stage))throw new IllegalArgumentException("Invalid source path");if(Files.exists(out))throw new IllegalArgumentException("Duplicate source: "+rel);
                Files.createDirectories(out.getParent());Files.writeString(out,text);count++;
            }
            if(count==0)throw new IllegalArgumentException("No user Java sources selected");
            List<String> args=new ArrayList<>(List.of("--release","21","--module-path","vendor/javafx","--add-modules","javafx.controls,javafx.fxml,javafx.swing,jdk.httpserver","-encoding","UTF-8","-cp",System.getProperty("java.class.path").replaceAll("[^;]*build[^;]*;?",""),"-d",compiled.toString()));
            for(Path dir:List.of(Path.of("Controller/src"),Path.of("TeamCode/src"),stage))try(var stream=Files.walk(dir)){stream.filter(p->p.toString().endsWith(".java")).forEach(p->args.add(p.toString()));}
            var compiler=javax.tools.ToolProvider.getSystemJavaCompiler();if(compiler==null)throw new IllegalStateException("Use the included JDK to compile Java");
            ByteArrayOutputStream output=new ByteArrayOutputStream();int code=compiler.run(null,output,output,args.toArray(String[]::new));
            if(code!=0)return Map.of("ok",false,"log",output.toString(StandardCharsets.UTF_8),"message","Compilation failed. Your previous source and build are preserved.");
            Path imported=Path.of("ImportedTeamCode"),backup=Path.of("import-history",UUID.randomUUID().toString());Files.createDirectories(backup);
            if(Files.exists(imported))Files.move(imported,backup.resolve("ImportedTeamCode"));
            Files.move(stage,imported);Files.writeString(Path.of("build/active-build.txt"),compiled.toAbsolutePath().toString());Files.writeString(Path.of("build/last-build.txt"),java.time.Instant.now().toString());
            return Map.of("ok",true,"files",count,"skippedAdapters",skipped,"log",output.toString(StandardCharsets.UTF_8),"message","Compiled successfully. Restart the Java runner to load the new OpModes.");
        }finally{importing=false;}
    }
    private void restart(){
        timer.schedule(()->{
            try{
                String classes=Files.readString(Path.of("build/active-build.txt")).trim();
                List<String> command=List.of(Path.of("runtime/bin/java.exe").toAbsolutePath().toString(),"--module-path","vendor/javafx","--add-modules","javafx.controls,javafx.fxml,javafx.swing,jdk.httpserver","-cp",classes+";Controller/src;TeamCode/src;lib/*","virtual_robot.controller.VirtualRobotApplication","--studio","--no-browser");
                server.stop(0);new ProcessBuilder(command).redirectOutput(ProcessBuilder.Redirect.appendTo(new File("build/runner.log"))).redirectErrorStream(true).start();Platform.exit();
            }catch(Exception e){System.err.println("Restart failed: "+e);}
        },300,TimeUnit.MILLISECONDS);
    }
}
