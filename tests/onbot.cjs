const fs=require('fs'),assert=require('assert'),{chromium}=require('playwright');
const wait=ms=>new Promise(r=>setTimeout(r,ms));
(async()=>{
 const browser=await chromium.launch({channel:'msedge',headless:true,args:['--enable-unsafe-swiftshader']});
 try{
  let info=await fetch('http://127.0.0.1:8765/api/info').then(r=>r.json());
  assert.deepEqual(info.opModes,[],'Clean community distribution has no imported programs');
  async function api(endpoint,data){const r=await fetch('http://127.0.0.1:8765/api/'+endpoint,{method:'POST',headers:{'Content-Type':'application/json','X-Sim-Token':info.token},body:JSON.stringify(data)}),j=await r.json();if(!r.ok)throw Error(j.error);return j;}
  const action=(action,extra={})=>api('action',{action,...extra});
  await assert.rejects(action('init'),/No enabled team OpModes/);
  const page=await browser.newPage({viewport:{width:1500,height:950}}),errors=[];
  page.on('pageerror',e=>errors.push(e.message));
  await page.goto('http://127.0.0.1:8765');await page.waitForFunction(()=>document.querySelector('#connection').textContent.includes('connected'));
  assert(await page.locator('#init').isDisabled(),'Empty state cannot initialize');
  await page.locator('[data-panel="code"]').click();await page.locator('#loadExample').click();
  await page.waitForFunction(()=>document.querySelectorAll('[data-source]').length===3);
  // Build an OnBot-shaped download ZIP in this test browser, including a BOM,
  // a helper subpackage and an annotated disabled class.
  const zip=await page.evaluate(async()=>{
   const {zipSync,strToU8}=await import('/vendor/fflate.js');const files={};
   for(const name of ['SharedDrive.java','SharedAuto.java','DrivePower.java'])files['OnBotDownload/src/'+name]=strToU8((name==='SharedDrive.java'?'\uFEFF':'')+await fetch('/examples/onbot/'+name).then(r=>r.text()));
   files['OnBotDownload/src/DisabledExample.java']=strToU8('package org.firstinspires.ftc.teamcode; import com.qualcomm.robotcore.eventloop.opmode.*; @Disabled @TeleOp public class DisabledExample extends OpMode { public void init(){} public void loop(){} }');
   return Array.from(zipSync(files));
  });
  await page.locator('#javaZip').setInputFiles({name:'OnBot-source.zip',mimeType:'application/zip',buffer:Buffer.from(zip)});
  await page.waitForFunction(()=>document.querySelectorAll('[data-source]').length===4);
  await page.locator('#compile').click();await page.waitForFunction(()=>document.querySelector('#compileLog').textContent.includes('Compiled successfully'),null,{timeout:90000});
  assert(fs.existsSync('ImportedTeamCode/org/firstinspires/ftc/teamcode/helpers/DrivePower.java'),'Helper reconstructed from package');
  const active=fs.readFileSync('build/active-build.txt','utf8');
  const failed=await api('import',{files:[{name:'Broken.java',content:'package org.firstinspires.ftc.teamcode; public class Broken { invalid invalid }'}]});
  assert.equal(failed.ok,false);assert.equal(fs.readFileSync('build/active-build.txt','utf8'),active,'Failed import preserves build');
  await assert.rejects(api('import',{files:[{name:'Bad.java',content:'package org.firstinspires.ftc.teamcodeevil; class Bad {}'}]}),/Expected package/);
  const old=info.token;await api('restart',{});
  for(let i=0;i<80;i++){await wait(250);try{const next=await fetch('http://127.0.0.1:8765/api/info').then(r=>r.json());if(next.token!==old){info=next;break;}}catch{}}
  assert.notEqual(info.token,old,'Runner restarted');
  assert.deepEqual(info.opModes.slice().sort(),['org.firstinspires.ftc.teamcode.SharedAuto','org.firstinspires.ftc.teamcode.SharedDrive']);
  await action('select',{name:'org.firstinspires.ftc.teamcode.SharedDrive'});await action('place',{x:0,y:-1,heading:0});await action('init');await action('start');
  for(let i=0;i<20;i++){await api('input',{pads:[{left_stick_y:-.5},{}]});await wait(20);}
  let state=await fetch('http://127.0.0.1:8765/api/state').then(r=>r.json());
  assert(Math.hypot(state.robot[3],state.robot[4])>.05,'Actual OnBot OpMode drives simulated motors');await action('stop');
  await action('select',{name:'org.firstinspires.ftc.teamcode.SharedAuto'});await action('place',{x:0,y:-1,heading:0});await action('init');await action('start');await wait(1100);
  state=await fetch('http://127.0.0.1:8765/api/state').then(r=>r.json());assert(Math.abs(state.robot[1]+1)>.02,'Actual OnBot LinearOpMode moves');await action('stop');await action('select',{name:'org.firstinspires.ftc.teamcode.SharedDrive'});
  assert.deepEqual(errors,[]);console.log('PASS: clean startup, generic example loader, OnBot ZIP/BOM/helper import, @Disabled filtering, package validation, failed import preservation, restart, TeleOp and LinearOpMode movement');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exit(1)});
