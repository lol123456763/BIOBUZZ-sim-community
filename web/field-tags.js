import * as T from 'three';

// Official 17 x 5 inch production-art cutouts on the four STEP backing plates.
// Board centers are CAD bounding-box midpoints; axes follow the assembled plate
// faces (the largest source triangles confirm their 60 degree inclination).
export async function addOfficialTagBoards(group,pivotHeight){
 const boards=[
  {page:1,lane:'red',right:-1,upZ:1,center:[-.323637681,.905435044,-.289414654]},
  {page:2,lane:'red',right:1,upZ:1,center:[-.323637681,1.261519155,.327341117]},
  {page:3,lane:'blue',right:1,upZ:-1,center:[.324062319,.905435044,.289414654]},
  {page:4,lane:'blue',right:-1,upZ:-1,center:[.324062319,1.261519155,-.327341117]}
 ];
 const loader=new T.TextureLoader();
 const loaded=await Promise.allSettled(boards.map(b=>loader.loadAsync(`./assets/field/apriltag-board-${b.page}.png`)));
 if(loaded.some(r=>r.status==='rejected')){loaded.forEach(r=>{if(r.status==='fulfilled')r.value.dispose();});throw Error('Official AprilTag artwork could not be loaded');}
 for(let i=0;i<boards.length;i++){
  const b=boards[i],texture=loaded[i].value;texture.colorSpace=T.SRGBColorSpace;texture.anisotropy=4;
  const right=new T.Vector3(b.right,0,0),up=new T.Vector3(0,.5,b.upZ*Math.sqrt(3)/2),normal=right.clone().cross(up);
  const mesh=new T.Mesh(new T.PlaneGeometry(.4318,.127),new T.MeshStandardMaterial({map:texture,roughness:.9,metalness:0,side:T.FrontSide}));
  mesh.name=`Official FIRST AprilTag board ${b.page}`;mesh.position.fromArray(b.center).addScaledVector(normal,.0008);mesh.position.y-=pivotHeight;
  mesh.quaternion.setFromRotationMatrix(new T.Matrix4().makeBasis(right,up,normal));mesh.receiveShadow=true;
  group.userData.hives[b.lane].add(mesh);
 }
}
