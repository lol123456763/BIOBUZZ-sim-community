const assert=require('assert');
(async()=>{const {standard,direct,mapped,binding,deadzone}=await import('../web/input.js');let count=0;const eq=(a,b)=>{assert.equal(a,b);count++;};let p={mapping:'standard',axes:[.55,-1,.2,.3],buttons:Array.from({length:17},()=>({value:0}))};p.buttons[0].value=1;p.buttons[7].value=.8;let c={preset:'auto',deadzone:.1,map:standard()},g=mapped(p,c);eq(g.a,true);eq(g.right_trigger,.8);eq(g.left_stick_y,-1);eq(Math.round(g.left_stick_x*10),5);eq(deadzone(.05,.1),0);eq(binding(p,'-a1'),1);eq(binding(p,'a1+'),0);eq(binding(p,'a1-'),1);
 p.mapping='';p.buttons[0].value=0;p.buttons[1].value=1;p.buttons[7].value=1;p.axes=[0,0,0,0,-1,-1];g=mapped(p,c);eq(g.a,true);eq(g.right_trigger,1);eq(g.dpad_up,true);eq(g.dpad_left,true);
 p.axes=Array(10).fill(0);p.axes[9]=-1;g=mapped(p,c);eq(g.dpad_up,true);eq(g.dpad_down,false);p.axes[9]=3.2857;eq(mapped(p,c).dpad_up,false);
 c.preset='custom';c.map=standard();c.map.left_trigger='t2';p.axes[2]=-1;eq(mapped(p,c).left_trigger,0);p.axes[2]=1;eq(mapped(p,c).left_trigger,1);eq(mapped(null,c).left_trigger,0);eq(mapped(null,c).a,false);
 console.log(`PASS: ${count} standard/raw F310 mapping, dead zone, inversion, hat and disconnect checks`);
})().catch(e=>{console.error(e);process.exit(1)});
