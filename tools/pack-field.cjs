const fs=require('fs'),zlib=require('zlib'),crypto=require('crypto');
const r=JSON.parse(fs.readFileSync('artifacts/field/meshes.json'));const groups=new Map(),owners=new Map();
function walk(n){for(const i of n.meshes||[])owners.set(i,n.name);for(const c of n.children||[])walk(c);}walk(r.root);
const manifest={source:'https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step',sourceIndex:'https://ftc-resources.firstinspires.org/ftc/archive/2027/field',version:'v26-27.2',retrieved:'2026-10-20',sourceSha256:crypto.createHash('sha256').update(fs.readFileSync('artifacts/field/source/field-cad-step.step')).digest('hex'),sourceZipSha256:crypto.createHash('sha256').update(fs.readFileSync('artifacts/field/official-field.zip')).digest('hex'),units:'meter',rotation:[0,0,0],translation:[0,0,0],linearDeflectionMeters:.001,angularDeflectionRadians:.35,positionRoundingMeters:.00001,pivotHeight:1.11632238,excluded:'Pollen and Nectar CAD instances are replaced by simulated moving game pieces. No static fixtures excluded.',parts:[]};
for(let i=0;i<r.meshes.length;i++){
 const m=r.meshes[i],owner=owners.get(i)||m.name;if(/Pollen|Nectar/.test(owner))continue;
 const lane=/Blue Hive/.test(owner)?'blue':/Red Hive/.test(owner)?'red':'static';
 const surface=/Skin|Polycarb|Glass|Panel.*Clear|Wall Panel/i.test(m.name)?'plastic':/Tiles/.test(m.name)?'foam':/April Tag/.test(m.name)?'tag':'solid';
 const color=(m.color||[.5,.5,.5]).map(v=>Math.round(v*10000)/10000);
 const key=[lane,surface,color.join(',')].join('|');let p=groups.get(key);if(!p){p={name:key,lane,surface,color,positions:[],normals:[],indices:[]};groups.set(key,p);}
 const offset=p.positions.length/3;for(const v of m.attributes.position.array)p.positions.push(Math.round(v*100000)/100000);for(const v of m.attributes.normal.array)p.normals.push(Math.round(v*10000)/10000);for(const v of m.index.array)p.indices.push(v+offset);
 const bounds=[0,1,2].map(a=>{let lo=Infinity,hi=-Infinity;for(let j=a;j<m.attributes.position.array.length;j+=3){lo=Math.min(lo,m.attributes.position.array[j]);hi=Math.max(hi,m.attributes.position.array[j]);}return[lo,hi];});
 manifest.parts.push({index:i,name:m.name,assembly:owner,bounds,vertices:m.attributes.position.array.length/3,triangles:m.index.array.length/3});
}
const parts=[...groups.values()];manifest.vertices=parts.reduce((n,p)=>n+p.positions.length/3,0);manifest.triangles=parts.reduce((n,p)=>n+p.indices.length/3,0);manifest.drawCalls=parts.length;
fs.mkdirSync('web/assets/field',{recursive:true});fs.writeFileSync('web/assets/field/field.json.gz',zlib.gzipSync(JSON.stringify({parts,rotation:[0,0,0],translation:[0,0,0],manifest:{...manifest,parts:undefined}}),{level:9}));fs.writeFileSync('web/assets/field/manifest.json',JSON.stringify(manifest,null,2));console.log({vertices:manifest.vertices,triangles:manifest.triangles,groups:parts.length,bytes:fs.statSync('web/assets/field/field.json.gz').size});
