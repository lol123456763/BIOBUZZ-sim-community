const {chromium}=require(process.env.PLAYWRIGHT_PATH||'playwright');
const path=require('path');
const assert=require('assert');
(async()=>{
 const browser=await chromium.launch({channel:'msedge',headless:true,args:['--enable-unsafe-swiftshader']});
 const page=await browser.newPage({viewport:{width:1600,height:1000}}),errors=[];
 page.on('pageerror',e=>errors.push(e.message));
 page.on('console',m=>{if(m.type()==='error')errors.push(m.text());});
 await page.goto('http://127.0.0.1:8765');await page.waitForFunction(()=>document.querySelector('#connection').textContent.includes('connected'));
 await page.waitForTimeout(1200);await page.screenshot({path:'../output/studio-driver.png'});
 console.log('Initial UI:',await page.locator('#connection').textContent());
 console.log('Errors:',errors);
 if(process.argv.includes('--snapshot')){await browser.close();return;}
 if(!process.argv.includes('--setup-only')){
 await page.getByRole('button',{name:'Initialize',exact:true}).click();await page.waitForFunction(()=>document.querySelector('#phase').textContent==='INITIALIZED');await page.getByRole('button',{name:'Start',exact:true}).click();await page.waitForFunction(()=>document.querySelector('#phase').textContent==='RUNNING');
 await page.locator('#scene').focus();await page.keyboard.down('w');await page.waitForTimeout(800);let moving=await page.evaluate(()=>fetch('/api/state').then(r=>r.json()));await page.keyboard.up('w');await page.waitForTimeout(300);let released=await page.evaluate(()=>fetch('/api/state').then(r=>r.json()));
 assert(Math.hypot(...moving.robot.slice(3,5))>.05,'Actual Java TeleOp responds to W');console.log('Movement',moving.robot,'Released',released.robot);
 await page.locator('[data-panel=controllers]').click();await page.locator('#keyboardPad').selectOption('1');await page.locator('[data-panel=drive]').click();await page.locator('#scene').focus();await page.keyboard.down('f');await page.waitForTimeout(700);await page.keyboard.up('f');const mechanism=await page.evaluate(()=>fetch('/api/state').then(r=>r.json()));assert(mechanism.flywheel>20,'Generic gamepad 2 example motor responds');
 await page.getByRole('button',{name:'Stop robot'}).click();await page.waitForFunction(()=>document.querySelector('#phase').textContent==='STOPPED');
 }
 await page.locator('[data-panel="view"]').click();await page.locator('#camera').selectOption('orbit');await page.waitForTimeout(300);await page.screenshot({path:'../output/studio-orbit.png'});
 await page.locator('[data-panel="cad"]').click();await page.locator('#cadFiles').setInputFiles(path.resolve('web/test-fixtures/cube.stp'));await page.waitForFunction(()=>document.querySelector('#cadStatus').textContent.includes('parts ·'),null,{timeout:90000});console.log('CAD',await page.locator('#cadStatus').textContent());await page.screenshot({path:'../output/studio-cad.png'});
 await page.locator('#saveRig').click();await page.waitForFunction(()=>document.querySelector('#toast').textContent.includes('assignments saved'));await page.reload();await page.waitForFunction(()=>document.querySelector('#connection').textContent.includes('connected'));await page.locator('[data-panel="cad"]').click();await page.waitForFunction(()=>document.querySelector('#cadStatus').textContent.includes('Restored 1'));
 await page.locator('#cadFiles').setInputFiles(path.resolve('web/test-fixtures/known-box.stl'));await page.waitForFunction(()=>document.querySelector('#cadStatus').textContent.includes('100 × 80 × 50'));console.log('PASS: STL units and saved CAD restoration');await page.locator('#clearCad').click();await page.locator('[data-panel="robot"]').click();await page.locator('#profileName').fill('Verification');await page.locator('#saveProfile').click();await page.waitForFunction(()=>[...document.querySelector('#profiles').options].some(o=>o.value==='Verification'));await page.locator('#applyRobot').click();await page.waitForFunction(()=>document.querySelector('#toast').textContent.includes('profile applied'));
 await page.locator('[data-panel="drive"]').click();await page.locator('#mode').selectOption('match-teleop');await page.waitForFunction(()=>document.querySelector('#camera option[value=orbit]').disabled);assert(await page.locator('#camera option[value=orbit]').evaluate(e=>e.disabled),'Match locks free cameras');
 await page.locator('#mode').selectOption('practice');
 assert.deepEqual(errors,[],'No browser errors');console.log('PASS: browser → actual Java OpMode → physics → rendering; STEP import; profiles; match camera restriction.');await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
