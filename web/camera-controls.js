const $=id=>document.getElementById(id);
const names={orbit:'Practice / Free camera',chase:'Practice / Follow robot',overhead:'Practice / Top down',red:'Driver station / Red',blue:'Driver station / Blue',audience:'Practice / Audience'};

export class CameraControls {
 constructor(view,alliance){
  this.view=view;this.alliance=alliance;this.match=false;this.practiceCamera='orbit';this.savedPose=null;
  $('quickCamera').replaceChildren(...[...$('camera').options].map(o=>new Option(o.text,o.value)));
  for(const id of ['camera','quickCamera'])$(id).onchange=()=>this.select($(id).value);
  for(const id of ['focusRobot','focusRobotView'])$(id).onclick=()=>{view.focusRobot();this.syncPosition();};
  $('resetCamera').onclick=()=>{this.savedPose=null;this.select(this.match?this.driver():'orbit');};
  document.querySelectorAll('[data-camera-move]').forEach(b=>b.onclick=()=>{view.moveCamera(...b.dataset.cameraMove.split(',').map(Number));this.syncPosition();});
  $('placeCamera').onclick=()=>{
   const fields=['cameraX','cameraY','cameraZ'].map($);
   if(fields.some(n=>!n.reportValidity()))return;
   view.placeCamera(...fields.map(n=>+n.value));this.syncPosition();
  };
  view.controls.addEventListener('end',()=>this.syncPosition());
  this.select('orbit');
 }
 driver(){return this.alliance()===1?'blue':'red';}
 select(mode){
  if(!Object.hasOwn(names,mode))return;
  if(this.match)mode=this.driver();else this.practiceCamera=mode;
  this.view.setCamera(mode);this.sync();
 }
 session(match){
  if(match&&!this.match){
   this.practiceCamera=this.view.cameraMode;
   this.savedPose={position:this.view.camera.position.clone(),target:this.view.controls.target.clone()};
  }
  const returning=this.match&&!match;
  this.match=match;this.view.match=match;
  for(const id of ['camera','quickCamera'])for(const o of $(id).options)o.disabled=match&&o.value!==this.driver();
  this.select(match?this.driver():this.practiceCamera);
  if(returning&&this.practiceCamera==='orbit'&&this.savedPose){
   this.view.camera.position.copy(this.savedPose.position);this.view.controls.target.copy(this.savedPose.target);this.view.controls.update();this.syncPosition();
  }
 }
 syncPosition(){
  const p=this.view.camera.position;['cameraX','cameraY','cameraZ'].forEach((id,i)=>{if(document.activeElement!==$(id))$(id).value=[p.x,p.y,p.z][i].toFixed(2);});
 }
 sync(){
  const mode=this.view.cameraMode,free=mode==='orbit';
  $('camera').value=$('quickCamera').value=mode;$('cameraTitle').textContent=names[mode];
  $('freeCameraControls').hidden=!free;$('driverCameraControls').hidden=mode!=='red'&&mode!=='blue';
  $('focusRobot').disabled=$('focusRobotView').disabled=mode==='overhead';
  const hint=this.match?'Match: your alliance station automatically follows the robot.':free?'Drag to orbit · right-drag to move · scroll to zoom':'Choose Free camera to move anywhere in practice.';
  $('cameraHint').textContent=hint;$('cameraHelp').textContent=hint;
  $('cameraBadge').textContent=this.match?'MATCH VIEW':'PRACTICE CAMERA';this.syncPosition();
 }
}
