const assert=require('assert');
(async()=>{
 const base='http://127.0.0.1:8765/api/';const info=await fetch(base+'info').then(r=>r.json());
 const action=async(action,extra={})=>{const r=await fetch(base+'action',{method:'POST',headers:{'Content-Type':'application/json','X-Sim-Token':info.token},body:JSON.stringify({action,...extra})});assert(r.ok,await r.text());};
 await action('stop');await action('select',{name:'org.firstinspires.ftc.teamcode.StudioDriveProbe'});await action('mode',{mode:'match-auto'});await action('init');await action('start');
 await new Promise(r=>setTimeout(r,31000));const s=await fetch(base+'state').then(r=>r.json());assert.equal(s.phase,'stopped');assert(s.motorPowers.every(p=>p===0));
 await action('mode',{mode:'practice'});await action('select',{name:info.selected});console.log('PASS: 30-second autonomous session automatically stops OpMode and motors');
})().catch(e=>{console.error(e);process.exit(1)});
