const {chromium}=require('playwright'),assert=require('assert');
(async()=>{
 const browser=await chromium.launch({channel:'msedge',headless:true});
 try{
  const page=await browser.newPage();await page.goto('http://127.0.0.1:8765');
  const result=await page.evaluate(async()=>{
   const {FieldView}=await import('/render.js'),T=await import('three');
   function check(ok,label){if(!ok)throw Error(label);}
   function view(mode){return{cameraMode:mode,robot:new T.Group(),camera:new T.PerspectiveCamera(65,16/9,.03,70),eye:1.6,station:.3,lookYaw:0,lookPitch:0,pointer:null,followTarget:null,controls:{enabled:false,target:new T.Vector3(),update(){}}};}
   function tick(v,dt=1/60){FieldView.prototype.updateCamera.call(v,dt);}
   for(const mode of ['red','blue','audience']){
    const v=view(mode);tick(v);const eye=v.camera.position.clone();v.robot.position.set(1.4,0,-1.3);tick(v);
    check(v.followTarget.x>0&&v.followTarget.x<1.4,mode+' follows smoothly');
    for(let i=0;i<180;i++)tick(v);
    check(v.followTarget.distanceTo(new T.Vector3(1.4,.35,-1.3))<1e-5,mode+' tracks robot area');
    check(v.camera.position.distanceTo(eye)<1e-9,mode+' preserves viewpoint');
    check(v.camera.fov===65,mode+' preserves zoom');
    v.lookYaw=.4;v.lookPitch=.2;v.pointer={};tick(v);check(v.lookYaw===.4,mode+' permits manual look');
    v.pointer=null;for(let i=0;i<240;i++)tick(v);check(Math.abs(v.lookYaw)<.001&&Math.abs(v.lookPitch)<.001,mode+' resumes following');
    for(const [x,z]of [[-1.6,-1.6],[1.6,-1.6],[1.6,1.6],[-1.6,1.6]]){
     v.robot.position.set(x,0,z);for(let i=0;i<180;i++)tick(v);
     v.camera.updateMatrixWorld();const screen=new T.Vector3(x,.35,z).project(v.camera);
     check(Math.abs(screen.x)<.02&&Math.abs(screen.y)<.02,mode+' frames all field corners');
    }
   }
   const a=view('red'),b=view('red');tick(a);tick(b);a.robot.position.x=b.robot.position.x=1;
   for(let i=0;i<30;i++)tick(a,1/30);for(let i=0;i<120;i++)tick(b,1/120);
   check(a.followTarget.distanceTo(b.followTarget)<1e-10,'Frame-rate independent smoothing');
   for(const mode of ['orbit','overhead']){const v=view(mode);v.camera.position.set(3,4,5);const before=v.camera.position.clone();tick(v);check(v.camera.position.equals(before),mode+' remains free/fixed');}
   const chase=view('chase');chase.robot.position.set(1,0,2);tick(chase);check(chase.camera.position.x===1&&chase.camera.position.z===3.1,'Chase still follows');
   return 'PASS: red/blue/audience smooth tracking, fixed station/zoom, manual look recovery, field corners, frame-rate independence, orbit/overhead/chase';
  });
  assert(result.startsWith('PASS'));console.log(result);
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exit(1)});
