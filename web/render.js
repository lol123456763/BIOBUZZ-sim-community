import * as T from 'three';
import {OrbitControls} from './vendor/OrbitControls.js';
import {loadOfficialField} from './field-cad.js';
const IN=.0254,H=72*IN;
const mat=(color,metalness=0,roughness=.65,extra={})=>new T.MeshStandardMaterial({color,metalness,roughness,...extra});
const steel=mat('#a8b7c0',.85,.28),dark=mat('#151c22',.4,.5),rubber=mat('#171c20',0,.9),lime=mat('#bed965',.1,.55);
function mesh(g,geo,m,x=0,y=0,z=0){let o=new T.Mesh(geo,m);o.position.set(x,y,z);o.castShadow=true;o.receiveShadow=true;g.add(o);return o;}
function box(g,x,y,z,w,h,d,m){return mesh(g,new T.BoxGeometry(w,h,d),m,x,y,z);}
function rod(g,a,b,r,m){a=new T.Vector3(...a);b=new T.Vector3(...b);let o=mesh(g,new T.CylinderGeometry(r,r,a.distanceTo(b),10),m);o.position.copy(a).add(b).multiplyScalar(.5);o.quaternion.setFromUnitVectors(new T.Vector3(0,1,0),b.sub(a).normalize());return o;}
function text(g,label,w,h,x,y,z,color='#d9e6e9',bg='#172127'){
 const c=document.createElement('canvas');c.width=1024;c.height=256;let ctx=c.getContext('2d');ctx.fillStyle=bg;ctx.fillRect(0,0,1024,256);ctx.fillStyle=color;ctx.font='bold 110px Segoe UI';ctx.textAlign='center';ctx.textBaseline='middle';ctx.fillText(label,512,128);let tex=new T.CanvasTexture(c);tex.colorSpace=T.SRGBColorSpace;
 return mesh(g,new T.PlaneGeometry(w,h),new T.MeshStandardMaterial({map:tex,roughness:.65,side:T.DoubleSide}),x,y,z);
}
function foam(){const c=document.createElement('canvas');c.width=c.height=256;let ctx=c.getContext('2d'),im=ctx.createImageData(256,256),s=73;for(let i=0;i<im.data.length;i+=4){s=(s*1664525+1013904223)>>>0;let q=132+(s%37);im.data[i]=q;im.data[i+1]=q+4;im.data[i+2]=q+5;im.data[i+3]=255;}ctx.putImageData(im,0,0);let t=new T.CanvasTexture(c);t.wrapS=t.wrapT=T.RepeatWrapping;t.repeat.set(4,4);return t;}
function ballMap(color){const c=document.createElement('canvas');c.width=512;c.height=256;let x=c.getContext('2d');x.fillStyle=color;x.fillRect(0,0,512,256);for(let r=0;r<6;r++)for(let k=0;k<12;k++){const px=(k+.5*(r%2))*43,py=r*45+12;x.fillStyle='#0005';x.beginPath();x.ellipse(px,py,7,10,0,0,7);x.fill();x.strokeStyle='#ffffff38';x.stroke();}let t=new T.CanvasTexture(c);t.colorSpace=T.SRGBColorSpace;return t;}
export class FieldView{
 constructor(canvas){
  this.canvas=canvas;this.renderer=new T.WebGLRenderer({canvas,antialias:true,powerPreference:'high-performance'});this.renderer.outputColorSpace=T.SRGBColorSpace;this.renderer.toneMapping=T.ACESFilmicToneMapping;this.renderer.toneMappingExposure=1.0;this.renderer.shadowMap.enabled=true;this.renderer.shadowMap.type=T.PCFSoftShadowMap;
  this.scene=new T.Scene();this.scene.background=new T.Color('#26343c');this.scene.fog=new T.Fog('#26343c',9,28);this.camera=new T.PerspectiveCamera(65,1,.03,70);this.camera.position.set(-H-.68,1.6,0);this.controls=new OrbitControls(this.camera,canvas);this.controls.enableDamping=false;this.controls.target.set(0,.4,0);this.controls.minDistance=.15;this.controls.maxDistance=40;this.controls.maxPolarAngle=Math.PI-.05;this.controls.screenSpacePanning=true;
  this.cameraMode='red';this.eye=1.6;this.station=0;this.lookYaw=0;this.lookPitch=0;this.match=false;this.fpsLimit=60;this.lastDraw=0;this.frameCount=0;this.fps=0;this.fpsStart=performance.now();this.state=null;this.arrival=0;this.wheelGroups=[];
  const ambient=new T.HemisphereLight('#d8e6ff','#43483a',1.2);this.scene.add(ambient);this.sun=new T.DirectionalLight('#fff3da',2.6);this.sun.position.set(-4,7,-3);this.sun.castShadow=true;this.sun.shadow.mapSize.set(2048,2048);Object.assign(this.sun.shadow.camera,{left:-5,right:5,top:5,bottom:-5,near:.1,far:16});this.sun.shadow.bias=-.00015;this.sun.shadow.normalBias=.012;this.scene.add(this.sun);
  const env=new T.Scene();env.background=new T.Color('#9caeb9');for(let k=0;k<6;k++)box(env,(k%3-1)*5,5,Math.floor(k/3)*8-4,3,.1,2,new T.MeshBasicMaterial({color:0xffffff}));const pm=new T.PMREMGenerator(this.renderer);this.scene.environment=pm.fromScene(env,.04).texture;pm.dispose();
  this.buildField();this.robot=new T.Group();this.scene.add(this.robot);this.cad=null;this.ballGroup=new T.Group();this.scene.add(this.ballGroup);this.ballMeshes=[];this.ballMaterials=['#e6be35','#e36568','#508ece'].map(c=>mat(c,.05,.4,{map:ballMap(c)}));this.hives=new T.Group();this.scene.add(this.hives);this.hiveKey='';this.configureRobot({robotWidth:.4572,robotLength:.4572,robotHeight:.32,driveWheelDiameter:.1016,wheelDiameter:.072,exitHeight:.254,angleDeg:55});
  this.pointer=null;canvas.addEventListener('pointerdown',e=>{if(this.cameraMode!=='orbit'){this.pointer={x:e.clientX,y:e.clientY};canvas.setPointerCapture(e.pointerId);}});canvas.addEventListener('pointermove',e=>{if(!this.pointer)return;this.lookYaw+=(e.clientX-this.pointer.x)*.004;this.lookPitch=T.MathUtils.clamp(this.lookPitch+(e.clientY-this.pointer.y)*.004,-.65,.65);this.pointer={x:e.clientX,y:e.clientY};});canvas.addEventListener('pointerup',()=>this.pointer=null);canvas.addEventListener('wheel',e=>{if(this.cameraMode!=='orbit'){this.camera.fov=T.MathUtils.clamp(this.camera.fov+e.deltaY*.025,35,90);this.camera.updateProjectionMatrix();}},{passive:true});
  new ResizeObserver(()=>this.resize()).observe(canvas.parentElement);this.quality('medium');this.setCamera('red');this.resize();this.loadFieldCad();
 }
 disposeField(group){
  const geometries=new Set(),materials=new Set(),textures=new Set();
  group.traverse(o=>{if(o.geometry)geometries.add(o.geometry);for(const m of (Array.isArray(o.material)?o.material:[o.material]))if(m){materials.add(m);for(const value of Object.values(m))if(value?.isTexture)textures.add(value);}});
  geometries.forEach(g=>g.dispose());materials.forEach(m=>m.dispose());textures.forEach(t=>t.dispose());
 }
 async loadFieldCad(){
  const request=this.fieldLoadRequest=(this.fieldLoadRequest||0)+1,detail=this.qualityPreset;
  const report=(state,label,detail='')=>{this.fieldCadStatus={state,label,detail};this.canvas.dispatchEvent(new CustomEvent('fieldcadstatus',{detail:this.fieldCadStatus}));};
  report('loading','Loading official FIRST field CAD');
  try{
   const {group,manifest}=await loadOfficialField(detail);
   if(request!==this.fieldLoadRequest){this.disposeField(group);return;}
   if(this.officialField){this.scene.remove(this.officialField);this.disposeField(this.officialField);}
   this.officialField=group;this.fieldManifest=manifest;this.scene.add(group);this.fallbackField.visible=false;this.hives.visible=false;
   report('ready','Official FIRST CAD • v26-27.2',detail==='high'?'Original 1 mm tessellation. Physics uses simplified collisions.':'Optimized official CAD. High quality retains original 1 mm tessellation. Physics uses simplified collisions.');
  }catch(error){
   if(request!==this.fieldLoadRequest)return;
   if(this.officialField)report('ready','Official FIRST CAD • previous detail retained','Could not change field detail: '+error.message);
   else report('error','Simplified field • CAD unavailable',error.message);
   console.error('Official field CAD:',error);
  }
 }
 resize(){let w=this.canvas.clientWidth,h=this.canvas.clientHeight;this.renderer.setSize(w,h,false);this.camera.aspect=w/h;this.camera.updateProjectionMatrix();}
 quality(q){const changed=this.qualityPreset&&this.qualityPreset!==q;this.qualityPreset=q;if(changed)this.loadFieldCad();this.renderer.setPixelRatio(Math.min(devicePixelRatio,q==='high'?1.7:q==='medium'?1.25:1));this.renderer.shadowMap.enabled=q!=='low';this.sun.shadow.mapSize.set(q==='high'?2048:1024,q==='high'?2048:1024);if(this.sun.shadow.map){this.sun.shadow.map.dispose();this.sun.shadow.map=null;}this.resize();}
 buildField(){
  const ground=mat('#303a3e',.15,.85);box(this.scene,0,-.065,0,30,.1,30,ground);
  this.fallbackField=new T.Group();this.scene.add(this.fallbackField);
  let texture=foam();for(let x=0;x<6;x++)for(let z=0;z<6;z++)box(this.fallbackField,(x-2.5)*.6096,-.012,(z-2.5)*.6096,.607,.025,.607,mat((x+z)%2?'#a2a8a5':'#b1b6b3',0,.96,{map:texture,bumpMap:texture,bumpScale:.001}));
  const red=mat('#963c45'),blue=mat('#315e88');for(let s of [-1,1]){box(this.fallbackField,s*(H+.686),-.015,0,1.3716,.02,2.4638,s<0?red:blue);let t=text(this.fallbackField,s<0?'RED ALLIANCE':'BLUE ALLIANCE',1.3,.23,s*(H+.8),.003,0);t.rotation.x=-Math.PI/2;t.rotation.z=s*Math.PI/2;}
  const glass=mat('#b8d8df',.05,.15,{transparent:true,opacity:.15,depthWrite:false});
  for(let s of [-1,1]){box(this.fallbackField,s*H,.15,0,.012,.30,H*2,glass);box(this.fallbackField,0,.15,s*H,H*2,.30,.012,glass);for(let y of [.018,.30]){rod(this.fallbackField,[s*H,y,-H],[s*H,y,H],.012,steel);rod(this.fallbackField,[-H,y,s*H],[H,y,s*H],.012,steel);}for(let j=0;j<7;j++){rod(this.fallbackField,[s*H,0,(j-3)*.6096],[s*H,.30,(j-3)*.6096],.009,steel);rod(this.fallbackField,[(j-3)*.6096,0,s*H],[(j-3)*.6096,.30,s*H],.009,steel);}}
  for(let sx of [-1,1])for(let sz of [-1,1]){rod(this.fallbackField,[sx*24.73*IN,0,sz*19.475*IN],[sx*24.73*IN,43.95*IN,0],.018,steel);box(this.fallbackField,sx*24.73*IN,.012,sz*19.475*IN,.14,.02,.14,dark);}rod(this.fallbackField,[-24.73*IN,43.95*IN,0],[24.73*IN,43.95*IN,0],.024,steel);
  let sign=text(this.fallbackField,'BIOBUZZ',.43,.12,0,.93,-.03,'#202a31','#e5d373');sign.rotation.y=Math.PI;
  const tape=(x,z,w,d,c)=>box(this.fallbackField,x*IN,.003,-z*IN,w*IN,.002,d*IN,mat(c,0,.8));
  tape(-66.5,24,11,1.3,'#ed5c68');tape(-61,35.5,1.3,23,'#ed5c68');tape(-66.5,47,11,1.3,'#ed5c68');tape(66.5,-24,11,1.3,'#5b9be0');tape(61,-35.5,1.3,23,'#5b9be0');tape(66.5,-47,11,1.3,'#5b9be0');tape(-60.5,-71,23,1.3,'#ed5c68');tape(60.5,71,23,1.3,'#5b9be0');
  for(const [x,y] of [[-24,69.6],[69.6,24],[24,-69.6],[-69.6,-24]]){for(let i=0;i<4;i++){let a=Math.PI/4+i*Math.PI/2;rod(this.fallbackField,[x*IN+Math.cos(a)*.055,0,-y*IN+Math.sin(a)*.055],[x*IN+Math.cos(a)*.055,21.5*IN,-y*IN+Math.sin(a)*.055],.004,lime);}for(let h of [.02,.29,21.5*IN]){let r=mesh(this.fallbackField,new T.TorusGeometry(.051,.004,8,40),lime,x*IN,h,-y*IN);r.rotation.x=Math.PI/2;}}
  // A competition hall provides scale, real lighting references, and an uncluttered driver sightline.
  for(let z of [-7,7]){box(this.scene,0,2.5,z,18,5,.2,mat('#283c46',.2,.8));text(this.scene,'BIOBUZZ  •  ROBOTICS FIELD LAB',7,.55,0,3.4,z+ (z<0?.12:-.12),'#adc4d0','#20313a').rotation.y=z<0?0:Math.PI;for(let row=0;row<3;row++){box(this.scene,0,.2+row*.35,z-Math.sign(z)*(.9+row*.45),9,.16,.55,mat('#3f505b',.6,.4));for(let k=-8;k<=8;k++)box(this.scene,k*.48,.38+row*.35,z-Math.sign(z)*(.9+row*.45),.40,.20,.35,mat(k%3?'#344755':'#71574b',0,.8));}}
  for(let x of [-7,7])for(let z=-6;z<=6;z+=4)box(this.scene,x,2.8,z,.22,5.6,.22,steel);
  for(let z of [-4,0,4]){rod(this.scene,[-7,5.5,z],[7,5.5,z],.06,dark);for(let x of [-3,3])box(this.scene,x,5.4,z,1.6,.04,.4,new T.MeshStandardMaterial({color:'#ffffeb',emissive:'#f0f4ff',emissiveIntensity:3}));}
 }
 configureRobot(c){this.config=c;if(this.prototype)this.prototype.traverse(o=>{if(o.geometry)o.geometry.dispose();});this.robot.clear();this.wheelGroups=[];const w=c.robotWidth,l=c.robotLength,h=c.robotHeight,r=c.driveWheelDiameter/2;
  this.prototype=new T.Group();this.robot.add(this.prototype);let g=this.prototype;
  for(let x of [-w*.36,w*.36]){box(g,x,.11,0,.026,.04,l*.9,steel);for(let z=-l*.38;z<l*.42;z+=.033){let b=mesh(g,new T.CylinderGeometry(.005,.005,.042,8),dark,x,.11,z);b.rotation.z=Math.PI/2;}}
  for(let z of [-l*.32,l*.32])box(g,0,.11,z,w*.8,.04,.024,steel);box(g,0,.085,0,w*.65,.008,l*.68,mat('#303a3f',.6,.3));
  const tx=(c.driveTrackWidth||.38)/2,tz=(c.driveWheelbase||.30)/2;const wheelPositions=[[-tx,tz],[-tx,-tz],[tx,-tz],[tx,tz]];
  wheelPositions.forEach(([x,z],idx)=>{let wg=new T.Group();wg.position.set(x,r,z);g.add(wg);this.wheelGroups.push(wg);let tire=mesh(wg,new T.CylinderGeometry(r*.73,r*.73,r*.68,24),rubber);tire.rotation.z=Math.PI/2;let hub=mesh(wg,new T.CylinderGeometry(r*.62,r*.62,r*.8,24),steel);hub.rotation.z=Math.PI/2;
   for(let i=0;i<8;i++){let a=i*Math.PI/4;let roll=mesh(wg,new T.CapsuleGeometry(r*.22,r*.88,4,8),rubber,0,Math.cos(a)*r*.78,Math.sin(a)*r*.78);roll.rotation.x=a;roll.rotation.z=(idx===0||idx===2?1:-1)*Math.PI/4;}let cap=mesh(wg,new T.CylinderGeometry(r*.22,r*.22,r*.86,12),mat('#d8ad4a',.8,.3));cap.rotation.z=Math.PI/2;
  });
  box(g,0,.15,l*.13,.15,.07,.11,mat('#233c47',.5,.4));text(g,'REV',.06,.026,0,.188,l*.13).rotation.x=-Math.PI/2;box(g,w*.22,.145,l*.16,.06,.06,.12,mat('#be5d31',.2,.45));
  for(let x of [-w*.30,w*.30])rod(g,[x,.12,l*.25],[x,.12,-l*.34],.014,dark);
  this.intake=new T.Group();this.intake.position.set(0,.065,-l/2);g.add(this.intake);let roller=mesh(this.intake,new T.CylinderGeometry(.03,.03,w*.82,24),lime);roller.rotation.z=Math.PI/2;for(let i=0;i<14;i++){let tread=mesh(this.intake,new T.TorusGeometry(.032,.005,6,16),rubber,(i-6.5)*w*.055,0,0);tread.rotation.y=Math.PI/2;}
  for(let x of [-w*.44,w*.44])rod(g,[x,.09,-l*.28],[x,.055,-l*.55],.009,steel);
  for(let x of [-.075,.075]){rod(g,[x,.13,l*.13],[x,c.exitHeight,-l*.15],.012,steel);rod(g,[x,.12,l*.13],[x,c.exitHeight-.03,-l*.15],.006,lime);}
  this.flywheel=new T.Group();this.flywheel.position.set(0,c.exitHeight-c.wheelDiameter/2,-l*.18);g.add(this.flywheel);let fw=mesh(this.flywheel,new T.CylinderGeometry(c.wheelDiameter/2,c.wheelDiameter/2,.07,32),rubber);fw.rotation.z=Math.PI/2;let disk=mesh(this.flywheel,new T.CylinderGeometry(c.wheelDiameter*.3,c.wheelDiameter*.3,.074,16),mat('#e7bd50',.8,.25));disk.rotation.z=Math.PI/2;
  for(let side of [-1,1]){for(let k=0;k<9;k++){let a=-Math.PI*.7+k*.19,b=a+.19,rr=c.wheelDiameter*.65;rod(g,[side*.055,c.exitHeight+Math.sin(a)*rr,-l*.18+Math.cos(a)*rr],[side*.055,c.exitHeight+Math.sin(b)*rr,-l*.18+Math.cos(b)*rr],.006,steel);}}
  const wire=new T.CatmullRomCurve3([new T.Vector3(w*.22,.18,l*.16),new T.Vector3(w*.1,.23,l*.02),new T.Vector3(.03,.16,-l*.05)]);mesh(g,new T.TubeGeometry(wire,16,.003,6,false),mat('#da613c'));
  for(let side of [-1,1]){let t=text(g,'FTC',l*.49,.065,side*w*.405,.16,0,'#ffffff','#992f40');t.rotation.y=side*Math.PI/2;}
  if(this.cad)this.attachCad(this.cad);
 }
 attachCad(group){this.cad=group;this.prototype.visible=!group;if(group)this.robot.add(group);}
 moveCamera(x,y,z){
  if(this.match||this.cameraMode!=='orbit')return;
  const shift=new T.Vector3(x,y,z).applyQuaternion(this.camera.quaternion);
  shift.y=Math.max(.08-this.camera.position.y,shift.y);
  this.camera.position.add(shift);this.controls.target.add(shift);this.controls.update();
 }
 placeCamera(x,y,z){
  if(this.match||this.cameraMode!=='orbit'||![x,y,z].every(Number.isFinite))return;
  const position=new T.Vector3(x,Math.max(.08,y),z),shift=position.clone().sub(this.camera.position);
  this.camera.position.copy(position);this.controls.target.add(shift);this.controls.update();
 }
 focusRobot(){
  if(this.cameraMode==='orbit'){
   const target=this.robot.position.clone();target.y=.3;
   this.camera.position.add(target.clone().sub(this.controls.target));this.controls.target.copy(target);this.controls.update();
  }else this.setCamera(this.cameraMode);
 }
 setCamera(mode){this.cameraMode=mode;this.lookYaw=this.lookPitch=0;this.followTarget=null;this.controls.enabled=mode==='orbit';if(mode==='orbit'){this.camera.position.set(-3,2.5,3.4);this.controls.target.set(0,.45,0);this.controls.update();}if(mode==='overhead'){this.camera.position.set(0,5,.001);this.camera.lookAt(0,0,0);}}
 updateCamera(dt=.016){let m=this.cameraMode,p=this.robot.position;if(m==='orbit'){this.controls.update();return;}if(m==='overhead')return;let eye,target;
  if(m==='red'||m==='blue'){let s=m==='red'?-1:1;eye=new T.Vector3(s*(H+.68),this.eye,-this.station);target=new T.Vector3(p.x,.35,p.z);}
  else if(m==='chase'){let h=this.robot.rotation.y;eye=new T.Vector3(p.x+Math.sin(h)*1.1, .85,p.z+Math.cos(h)*1.1);target=new T.Vector3(p.x,.22,p.z);}
  else{eye=new T.Vector3(0,1.65,3.5);target=new T.Vector3(p.x,.35,p.z);}
  if(m==='red'||m==='blue'||m==='audience'){
   // Pan toward the robot's area while preserving the station viewpoint and zoom.
   const blend=1-Math.exp(-Math.max(0,dt)/.20);
   if(!this.followTarget)this.followTarget=target.clone();else this.followTarget.lerp(target,blend);
   target=this.followTarget.clone();
   if(!this.pointer){const settle=Math.exp(-Math.max(0,dt)/.45);this.lookYaw*=settle;this.lookPitch*=settle;}
  }
  const direction=target.sub(eye).applyAxisAngle(new T.Vector3(0,1,0),this.lookYaw);direction.y+=this.lookPitch*direction.length();this.camera.position.copy(eye);this.camera.lookAt(eye.clone().add(direction));
 }
 accept(state){this.state=state;this.arrival=performance.now();}
 draw(now){if(this.fpsLimit&&now-this.lastDraw<1000/this.fpsLimit-.8)return false;let dt=Math.min(.05,(now-this.lastDraw)/1000||.016);this.lastDraw=now;const s=this.state;
  if(s){let r=s.robot,advance=Math.min(.035,(now-this.arrival)/1000);this.robot.position.set(r[0]+r[3]*advance,0,-r[1]-r[4]*advance);this.robot.rotation.y=r[2]+r[5]*advance;
   this.wheelGroups.forEach((g,i)=>g.rotation.x=s.wheels[i]);this.intake.rotation.x+=s.intake*dt*22;this.flywheel.rotation.x+=s.flywheel/this.config.shooterTicksPerRev*Math.PI*2*dt||0;
   while(this.ballMeshes.length<s.balls.length)this.ballMeshes.push(mesh(this.ballGroup,new T.SphereGeometry(1,20,14),this.ballMaterials[0]));this.ballMeshes.forEach((b,i)=>{b.visible=i<s.balls.length;if(!b.visible)return;let a=s.balls[i];b.position.set(a[0],a[2],-a[1]);b.scale.setScalar(a[3]);b.material=this.ballMaterials[a[4]+1];b.rotation.x+=dt;});
   let key=s.hives.map(h=>h[0]).join(',');if(key!==this.hiveKey){this.hiveKey=key;this.buildHives(s.hives);}
  }
  if(s&&this.officialField){const lanes=this.officialField.userData.hives;lanes.red.rotation.x=(s.hives[0][0]+1)*Math.PI/6;lanes.blue.rotation.x=(s.hives[1][0]-1)*Math.PI/6;}
  this.updateCamera(dt);this.renderer.render(this.scene,this.camera);this.frameCount++;if(now-this.fpsStart>1000){this.fps=Math.round(this.frameCount*1000/(now-this.fpsStart));this.frameCount=0;this.fpsStart=now;}return true;
 }
 buildHives(hives){this.hives.traverse(o=>{if(o.geometry)o.geometry.dispose();});this.hives.clear();for(let color=0;color<2;color++)for(let up of [true,false]){let side=hives[color][0]*(up?1:-1),cx=(color===0?-12.75:12.75)*IN,cy=side*15.44*IN,cz=(up?59.55:36)*IN,sign=up?1:-1;
  const points=[[-10,-7],[10,-7],[10,.61],[0,7],[-10,.61]],front=points.map(([x,y])=>[cx+x*IN,cz+sign*Math.sqrt(3)/2*y*IN,-cy+side*.5*y*IN]),back=front.map(p=>[p[0],p[1]-sign*.152,p[2]+side*.264]);let rail=mat(color===0?'#db515d':'#447db8',.55,.35),panel=mat(color===0?'#eaafb1':'#a4cbe4',.15,.22,{transparent:true,opacity:.32,side:T.DoubleSide,depthWrite:false});
  for(let i=0;i<5;i++){let j=(i+1)%5;rod(this.hives,front[i],front[j],.008,rail);rod(this.hives,back[i],back[j],.006,steel);rod(this.hives,front[i],back[i],.005,steel);let a=front[i],b=front[j],c=back[j],d=back[i],geo=new T.BufferGeometry();geo.setAttribute('position',new T.Float32BufferAttribute([...a,...b,...c,...a,...c,...d],3));geo.computeVertexNormals();mesh(this.hives,geo,panel);}
 }}
}
