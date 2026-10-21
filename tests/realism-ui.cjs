const {chromium}=require('playwright'),assert=require('assert');
(async()=>{
 const browser=await chromium.launch({channel:'msedge',headless:true,args:['--enable-unsafe-swiftshader']});
 const page=await browser.newPage({viewport:{width:1600,height:1000}}),errors=[];
 page.on('pageerror',e=>errors.push(e.message));
 try{
  await page.goto('http://127.0.0.1:8765');
  await page.waitForFunction(()=>document.querySelector('#connection').textContent.includes('connected'));
  await page.waitForFunction(()=>document.querySelector('#fieldCadStatus').dataset.state==='ready',null,{timeout:120000});
  const initial=await page.evaluate(async()=>{const i=await fetch('/api/info').then(r=>r.json());return {token:i.token,robot:i.robot};});
  const post=async(path,data)=>page.evaluate(async({path,data,token})=>{const r=await fetch('/api/'+path,{method:'POST',headers:{'Content-Type':'application/json','X-Sim-Token':token},body:JSON.stringify(data)});const body=await r.json();if(!r.ok)throw Error(body.error);return body;},{path,data,token:initial.token});
  await post('action',{action:'stop'});
  await post('profile',{operation:'apply',data:{version:3,robot:{...initial.robot,cameraPitch:40}}});
  await page.reload();await page.waitForFunction(()=>document.querySelector('#connection').textContent.includes('connected'));
  await page.locator('#opMode').selectOption('org.firstinspires.ftc.teamcode.SharedVision');
  await page.locator('#init').click();await page.waitForFunction(()=>!document.querySelector('#start').disabled);
  await page.locator('#start').click();
  await page.waitForFunction(async()=>{const s=await fetch('/api/state').then(r=>r.json());return s.vision.running&&s.vision.valid&&s.vision.pipeline===1;});
  await page.getByText('Limelight diagnostics',{exact:true}).click();
  await page.waitForFunction(()=>document.querySelector('#visionStatus').textContent==='Target detected');
  assert((await page.locator('#visionTarget').textContent()).includes('AprilTag'));
  assert(await page.locator('#applyRobot').isDisabled(),'Cannot replace setup while running');
  await page.locator('#visionTiming').scrollIntoViewIfNeeded();
  await page.screenshot({path:'../output/community-vision-desktop.png'});
  await page.locator('#quickStop').click();await page.waitForFunction(()=>document.querySelector('#phase').textContent==='STOPPED');
  await post('profile',{operation:'apply',data:{version:3,robot:initial.robot}});
  await page.locator('#closeControls').click();await page.screenshot({path:'../output/community-field-desktop.png'});
  await page.setViewportSize({width:390,height:844});await page.screenshot({path:'../output/community-field-mobile.png'});
  assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'No mobile horizontal overflow');
  assert.deepEqual(errors,[]);console.log('PASS: official CAD loads offline; generic camera example produces AprilTag detections; UI shows live vision and locks setup; desktop/mobile layouts.');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exit(1)});
