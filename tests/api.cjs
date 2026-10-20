const fs=require('fs'),path=require('path'),assert=require('assert');
const wait=ms=>new Promise(r=>setTimeout(r,ms));
(async()=>{
 let info=await fetch('http://127.0.0.1:8765/api/info').then(r=>r.json()),token=info.token;
 async function api(endpoint,data){let r=await fetch('http://127.0.0.1:8765/api/'+endpoint,data?{method:'POST',headers:{'Content-Type':'application/json','X-Sim-Token':token},body:JSON.stringify(data)}:{});let j=await r.json();if(!r.ok)throw Error(j.error);return j;}
 const action=(action,extra={})=>api('action',{action,...extra});
 if(process.argv.includes('--restart')){await api('restart',{});console.log('Restart requested');return;}
 await action('stop');await action('mode',{mode:'practice'});
 assert(info.opModes.length>0,'Enabled team programs are listed');
 assert(!info.opModes.some(n=>/Demo|tuning|StudioDriveProbe/.test(n)),'Bundled demos, diagnostics and tuners are excluded');
 await assert.rejects(action('select',{name:'org.firstinspires.ftc.teamcode.StudioDriveProbe'}),/OpMode not found/);
 await action('select',{name:'org.firstinspires.ftc.teamcode.SharedDrive'});
 async function run(brake){
  await action('place',{x:0,y:-1.3,heading:0});await action('init');await wait(120);await action('start');let pads=[{left_stick_y:-.65,a:brake},{}],done=false;
  let pump=(async()=>{while(!done){await api('input',{pads});await wait(8);}})();
  await wait(50);let early=await api('state');await wait(350);let powered=await api('state');pads=[{a:brake},{}];await wait(300);let release=await api('state');done=true;await pump;
  await wait(300);let stale=await api('state');assert(stale.motorPowers.every(p=>p===0),'Stale inputs release drive');await action('stop');
  return{early:Math.hypot(early.robot[3],early.robot[4]),powered:Math.hypot(powered.robot[3],powered.robot[4]),release:Math.hypot(release.robot[3],release.robot[4])};
 }
 const brake=await run(false);assert(brake.early<brake.powered,'Speed ramps over time');assert(brake.release<brake.powered,'Team TeleOp BRAKE decelerates');console.log('PASS: team-only selection, rejected bundled diagnostic, real team TeleOp acceleration, BRAKE and stale release',JSON.stringify(brake));
 await action('select',{name:info.selected});
 if(process.argv.includes('--drive-only'))return;
 let active=fs.readFileSync('build/active-build.txt','utf8');let failed=await api('import',{files:[{name:'Broken.java',content:'package org.firstinspires.ftc.teamcode; public class Broken { missing compile error }'}]});assert.equal(failed.ok,false);assert.equal(fs.readFileSync('build/active-build.txt','utf8'),active);console.log('PASS: failed Java import preserves previous build');
 let files=[];function walk(dir){for(let e of fs.readdirSync(dir,{withFileTypes:true})){let p=path.join(dir,e.name);if(e.isDirectory())walk(p);else if(p.endsWith('.java'))files.push({name:e.name,content:fs.readFileSync(p,'utf8')});}}walk('ImportedTeamCode');
 let compiled=await api('import',{files});assert(compiled.ok,compiled.log);console.log('PASS: in-app import compiles all existing competition sources, preserves adapters, and prepares restart');
 await api('restart',{});await wait(3000);for(let i=0;i<20;i++){try{info=await fetch('http://127.0.0.1:8765/api/info').then(r=>r.json());if(info.token!==token)break;}catch{}await wait(500);}assert.notEqual(info.token,token);assert(info.opModes.some(n=>n.includes('SharedDrive')));console.log('PASS: restarted runner discovers compiled Java OpModes');
})().catch(e=>{console.error(e);process.exit(1)});
