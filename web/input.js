export const axes=['left_stick_x','left_stick_y','right_stick_x','right_stick_y','left_trigger','right_trigger'];
export const buttons=['a','b','x','y','left_bumper','right_bumper','back','start','guide','left_stick_button','right_stick_button','dpad_up','dpad_down','dpad_left','dpad_right'];
export function standard(){return Object.fromEntries([...axes.map((k,i)=>[k,i<4?'a'+i:'b'+(i+2)]),...buttons.map((k,i)=>[k,'b'+[0,1,2,3,4,5,8,9,16,10,11,12,13,14,15][i]])]);}
export function direct(p){let map=standard();Object.assign(map,{a:'b1',b:'b2',x:'b0',y:'b3',left_trigger:'b6',right_trigger:'b7',guide:'',back:'b8',start:'b9'});if(p?.axes?.length>=10)Object.assign(map,{dpad_up:'h9U',dpad_right:'h9R',dpad_down:'h9D',dpad_left:'h9L'});else Object.assign(map,{dpad_up:'a5-',dpad_down:'a5+',dpad_left:'a4-',dpad_right:'a4+'});return map;}
export function binding(p,code){
 if(!p||!code)return 0;let m=/^(-?)([abt])(\d+)([+-]?)$/.exec(code);
 if(m){let v=m[2]==='b'?(p.buttons[+m[3]]?.value||0):(p.axes[+m[3]]||0);if(m[2]==='t')v=(v+1)/2;if(m[1])v=-v;if(m[4])v=Math.max(0,m[4]==='+'?v:-v);return Number.isFinite(v)?Math.max(-1,Math.min(1,v)):0;}
 m=/^h(\d+)([URDL])$/.exec(code);if(m){let v=p.axes[+m[1]];if(v==null||v>1.05||v< -1.05)return 0;let sector=Math.round((v+1)*3.5);return {U:[0,1,7],R:[1,2,3],D:[3,4,5],L:[5,6,7]}[m[2]].includes(sector)?1:0;}return 0;
}
export function deadzone(v,d){return Math.abs(v)<=d?0:Math.sign(v)*(Math.abs(v)-d)/(1-d);}
export function mapped(p,c){let out={};let map=c.preset==='standard'?standard():c.preset==='direct'?direct(p):c.preset==='custom'?c.map:p?.mapping==='standard'?standard():direct(p);
 for(let k of axes){let v=binding(p,map[k]);out[k]=k.endsWith('trigger')?Math.max(0,v):deadzone(v,c.deadzone);}
 for(let k of buttons)out[k]=binding(p,map[k])>.5;return out;
}
export class Inputs{
 constructor(){this.config=[0,1].map(()=>({device:'auto',preset:'auto',deadzone:.08,map:standard()}));this.keys=new Set();this.keyboardPad=0;this.focus=true;
  window.addEventListener('keydown',e=>{if(/INPUT|TEXTAREA|SELECT/.test(e.target.tagName))return;this.keys.add(e.code);if(['ArrowUp','ArrowDown','ArrowLeft','ArrowRight','Space'].includes(e.code))e.preventDefault();});window.addEventListener('keyup',e=>this.keys.delete(e.code));
  window.addEventListener('blur',()=>{this.focus=false;this.keys.clear();this.onRelease?.();});window.addEventListener('focus',()=>this.focus=true);document.addEventListener('visibilitychange',()=>{if(document.hidden){this.keys.clear();this.onRelease?.();}});
 }
 devices(){return [...(navigator.getGamepads?.()||[])].filter(Boolean);}
 poll(){let pads=this.devices();let result=this.config.map((c,i)=>{let p=c.device==='auto'?pads[i]:pads.find(p=>p.index===+c.device);return mapped(this.focus&&!document.hidden?p:null,c);});
  if(!this.focus||document.hidden)return result.map(()=>mapped(null,{preset:'standard',deadzone:0}));
  const key=(...k)=>k.some(v=>this.keys.has(v)),p=key('ShiftLeft','ShiftRight')?.35:1,g=result[0],mech=result[this.keyboardPad];
  if(key('KeyW','ArrowUp'))g.left_stick_y=-p;if(key('KeyS','ArrowDown'))g.left_stick_y=p;if(key('KeyA','ArrowLeft'))g.left_stick_x=-p;if(key('KeyD','ArrowRight'))g.left_stick_x=p;if(key('KeyQ'))g.right_stick_x=-p;if(key('KeyE'))g.right_stick_x=p;
  if(key('KeyI'))mech.left_trigger=1;if(key('KeyF'))mech.right_trigger=1;if(key('Space'))mech.right_bumper=true;if(key('KeyR'))mech.back=true;if(key('KeyX'))mech.x=true;if(key('KeyY'))mech.y=true;if(key('Digit1'))mech.dpad_up=true;if(key('Digit2'))g.dpad_right=true;if(key('KeyL')){if(this.keyboardPad===0)g.a=true;else g.left_trigger=1;}return result;
 }
 mount(root){root.replaceChildren();this.config.forEach((c,i)=>{
  let node=document.createElement('div');node.className='pad';node.innerHTML=`<h3>Gamepad ${i+1}</h3><p class="live"></p><label>Device<select class="device"><option value="auto">Auto / connected pad ${i+1}</option>${[0,1,2,3].map(n=>`<option value="${n}">Device index ${n}</option>`).join('')}</select></label><div class="row"><label>Layout<select class="preset"><option value="auto">Auto detect</option><option value="standard">X / standard</option><option value="direct">D / raw F310</option><option value="custom">Custom</option></select></label><label>Dead zone<input class="dead" type="number" min="0" max=".5" step=".01"></label></div><details><summary>Remap axes & buttons</summary><p class="hint">a0 = axis 0 · b0 = button 0 · -a1 = inverted axis · a5+ / a5− = half axis · t2 = full-range trigger · h9U = hat up. Live raw values appear above.</p><button class="copy">Copy detected layout to custom</button><div class="mapping"></div></details>`;
  node.querySelector('.device').value=c.device;node.querySelector('.preset').value=c.preset;node.querySelector('.dead').value=c.deadzone;
  node.querySelector('.device').onchange=e=>c.device=e.target.value;node.querySelector('.preset').onchange=e=>c.preset=e.target.value;node.querySelector('.dead').onchange=e=>{c.deadzone=Math.max(0,Math.min(.5,+e.target.value||0));e.target.value=c.deadzone;};
  const fields=node.querySelector('.mapping');for(let key of [...axes,...buttons]){let label=document.createElement('label');label.textContent=key.replaceAll('_',' ');let input=document.createElement('input');input.value=c.map[key]||'';input.dataset.key=key;input.onchange=()=>{c.map[key]=input.value.trim();c.preset='custom';node.querySelector('.preset').value='custom';};label.append(input);fields.append(label);}
  node.querySelector('.copy').onclick=()=>{let pads=this.devices(),p=c.device==='auto'?pads[i]:pads.find(p=>p.index===+c.device);c.map=(p?.mapping==='standard'||c.preset==='standard')?standard():direct(p);c.preset='custom';this.mount(root);};root.append(node);
 });this.root=root;}
 live(){if(!this.root)return;let pads=this.devices();this.root.querySelectorAll('.live').forEach((el,i)=>{let c=this.config[i],p=c.device==='auto'?pads[i]:pads.find(p=>p.index===+c.device);el.textContent=p?`${p.index}: ${p.id}\n${p.mapping||'raw'} · axes ${p.axes.map((x,n)=>n+':'+x.toFixed(2)).join(' ')} · buttons ${p.buttons.map((b,n)=>b.pressed?n:null).filter(v=>v!==null).join(',')||'—'}`:'Not connected · press a button to discover';el.style.whiteSpace='pre-wrap';});}
}
