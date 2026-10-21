const fs=require('fs'),zlib=require('zlib'),assert=require('assert');
const manifest=JSON.parse(fs.readFileSync('web/assets/field/manifest.json'));
assert.equal(manifest.version,'v26-27.2');assert.equal(manifest.units,'meter');
assert.deepEqual(manifest.rotation,[0,0,0]);assert.deepEqual(manifest.translation,[0,0,0]);
let counts=[];
for(const file of ['field.json.gz','field-full.json.gz']){
 const data=JSON.parse(zlib.gunzipSync(fs.readFileSync('web/assets/field/'+file)));
 const lanes=new Set(),bounds=[[Infinity,-Infinity],[Infinity,-Infinity],[Infinity,-Infinity]];let triangles=0;
 for(const p of data.parts){
  lanes.add(p.lane);assert.equal(p.positions.length%3,0);assert.equal(p.normals.length,p.positions.length);assert.equal(p.indices.length%3,0);
  for(let i=0;i<p.positions.length;i++){assert(Number.isFinite(p.positions[i]));const a=i%3;bounds[a][0]=Math.min(bounds[a][0],p.positions[i]);bounds[a][1]=Math.max(bounds[a][1],p.positions[i]);}
  for(const n of p.normals)assert(Number.isFinite(n));
  for(const i of p.indices)assert(Number.isInteger(i)&&i>=0&&i<p.positions.length/3);
  triangles+=p.indices.length/3;
 }
 assert(lanes.has('red')&&lanes.has('blue')&&lanes.has('static'));
 assert(bounds[0][0]<-1.79&&bounds[0][1]>1.79&&bounds[2][0]<-1.79&&bounds[2][1]>1.79,'Full-size field, without scale-to-fit');
 assert(bounds[1][1]>1.6&&bounds[1][1]<2,'HIVE has correct meter scale');
 assert(Math.abs(data.manifest.pivotHeight-1.11632238)<1e-8);
 counts.push(triangles);console.log(file,triangles,'triangles; finite geometry, indices, bounds, HIVE groups and pivot passed');
}
assert(counts[0]<counts[1],'Balanced keeps an optimized version and High retains original tessellation');
console.log('PASS: official field asset integrity and detail levels');
