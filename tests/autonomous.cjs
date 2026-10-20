const assert=require('assert'),wait=ms=>new Promise(r=>setTimeout(r,ms));
(async()=>{
 const base='http://127.0.0.1:8765/api/',info=await fetch(base+'info').then(r=>r.json());
 async function api(path,data){const r=await fetch(base+path,{method:'POST',headers:{'Content-Type':'application/json','X-Sim-Token':info.token},body:JSON.stringify(data)}),j=await r.json();if(!r.ok)throw Error(j.error);return j;}
 const act=(action,extra={})=>api('action',{action,...extra});
 await act('stop');await act('mode',{mode:'practice'});await act('select',{name:'org.firstinspires.ftc.teamcode.SharedAuto'});await act('place',{x:0,y:-1,heading:0});await act('init');await act('start');await wait(1200);
 const state=await fetch(base+'state').then(r=>r.json());assert(Math.abs(state.robot[1]+1)>.02,'Generic timed autonomous moves');await act('stop');await act('select',{name:info.selected});
 console.log('PASS: generic OnBot LinearOpMode initializes, starts and moves');
})().catch(e=>{console.error(e);process.exit(1)});
