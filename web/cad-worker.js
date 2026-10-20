importScripts('./vendor/occt-import-js.js');
let engine;
onmessage=async e=>{try{engine??=await occtimportjs({locateFile:p=>'./vendor/'+p});let result=engine.ReadStepFile(new Uint8Array(e.data),{linearUnit:'meter',linearDeflectionType:'absolute_value',linearDeflection:.001,angularDeflection:.3});if(!result.success)throw Error('OpenCascade could not read this STEP assembly');postMessage({ok:true,result});}catch(error){postMessage({ok:false,error:error.message});}};
