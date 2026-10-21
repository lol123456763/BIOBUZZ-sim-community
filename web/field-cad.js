import * as T from 'three';
import {gunzipSync} from './vendor/fflate.js';
import {addOfficialTagBoards} from './field-tags.js';

// Offline tessellation of FIRST's published STEP. Coordinates remain meters;
// the manifest records the rigid transform and source digest used by the converter.
export async function loadOfficialField(detail='medium') {
 const response=await fetch(detail==='high'?'./assets/field/field-full.json.gz':'./assets/field/field.json.gz');
 if(!response.ok)throw new Error(`Field asset unavailable (${response.status})`);
 const data=JSON.parse(new TextDecoder().decode(gunzipSync(new Uint8Array(await response.arrayBuffer()))));
 const group=new T.Group();group.name='FIRST official BIOBUZZ field CAD';
 const canvas=document.createElement('canvas');canvas.width=canvas.height=128;const ctx=canvas.getContext('2d'),pixels=ctx.createImageData(128,128);let seed=41;for(let i=0;i<pixels.data.length;i+=4){seed=(seed*1664525+1013904223)>>>0;let v=180+seed%65;pixels.data.set([v,v,v,255],i);}ctx.putImageData(pixels,0,0);const texture=new T.CanvasTexture(canvas);texture.wrapS=texture.wrapT=T.RepeatWrapping;texture.repeat.set(12,12);
 const materials=new Map();const lanes={};for(const name of ['red','blue']){const lane=new T.Group();lane.position.y=data.manifest.pivotHeight;lanes[name]=lane;group.add(lane);}group.userData.hives=lanes;
 for(const part of data.parts){
  const key=part.surface+part.color.join(',');let material=materials.get(key);
  if(!material){
   const [r,g,b]=part.color,neutral=Math.max(r,g,b)-Math.min(r,g,b)<.10;
   material=new T.MeshStandardMaterial({color:new T.Color(r,g,b),metalness:neutral?.45:.05,roughness:part.surface==='foam'?.98:neutral?.43:.65,side:T.DoubleSide,transparent:part.surface==='plastic',opacity:part.surface==='plastic'?.3:1,depthWrite:part.surface!=='plastic'});if(part.surface==='foam'){material.map=texture;material.bumpMap=texture;material.bumpScale=.0005;}materials.set(key,material);
  }
  const geometry=new T.BufferGeometry();geometry.setAttribute('position',new T.Float32BufferAttribute(part.positions,3));geometry.setIndex(part.indices);if(part.surface==='foam'){const uv=[];for(let i=0;i<part.positions.length;i+=3)uv.push(part.positions[i],part.positions[i+2]);geometry.setAttribute('uv',new T.Float32BufferAttribute(uv,2));}if(part.normals)geometry.setAttribute('normal',new T.Float32BufferAttribute(part.normals,3));else geometry.computeVertexNormals();
  const mesh=new T.Mesh(geometry,material);mesh.name=part.name;mesh.castShadow=true;mesh.receiveShadow=true;if(lanes[part.lane]){mesh.position.y=-data.manifest.pivotHeight;lanes[part.lane].add(mesh);}else group.add(mesh);
 }
 group.rotation.set(...data.rotation);group.position.set(...data.translation);
 try{await addOfficialTagBoards(group,data.manifest.pivotHeight);data.manifest.officialTagArtwork=true;}catch(error){data.manifest.artworkError=error.message;console.warn(error.message);}
 return {group,manifest:data.manifest};
}
