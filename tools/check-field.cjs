const fs=require('fs'),zlib=require('zlib');
for(const file of ['field.json.gz','field-full.json.gz']){
 const d=JSON.parse(zlib.gunzipSync(fs.readFileSync('web/assets/field/'+file)));let triangles=0,bounds=[[Infinity,-Infinity],[Infinity,-Infinity],[Infinity,-Infinity]];
 for(const p of d.parts){if(p.positions.length!==p.normals.length)throw Error('normal count');for(let i=0;i<p.positions.length;i++){const v=p.positions[i];if(!Number.isFinite(v))throw Error('nonfinite');const a=i%3;bounds[a][0]=Math.min(bounds[a][0],v);bounds[a][1]=Math.max(bounds[a][1],v);}for(const i of p.indices)if(i<0||i>=p.positions.length/3)throw Error('invalid index');triangles+=p.indices.length/3;}
 console.log(file,JSON.stringify({parts:d.parts.length,triangles,bounds,sourceSha256:d.manifest.sourceSha256}));
}
