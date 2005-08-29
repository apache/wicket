/* Copyright (c) 2004-2005 The Dojo Foundation, Licensed under the Academic Free License version 2.1 or above */var dj_global=this;
function dj_undef(_1,_2){
if(!_2){
_2=dj_global;
}
return (typeof _2[_1]=="undefined");
}
function dj_eval_object_path(_3,_4){
if(typeof _3!="string"){
return dj_global;
}
if(_3.indexOf(".")==-1){
return dj_undef(_3)?undefined:dj_global[_3];
}
var _5=_3.split(/\./);
var _6=dj_global;
for(var i=0;i<_5.length;++i){
if(!_4){
_6=_6[_5[i]];
if((typeof _6=="undefined")||(!_6)){
return _6;
}
}else{
if(dj_undef(_5[i],_6)){
_6[_5[i]]={};
}
_6=_6[_5[i]];
}
}
return _6;
}
if(dj_undef("djConfig")){
var djConfig={};
}
var dojo;
if(dj_undef("dojo")){
dojo={};
}
dojo.version={major:0,minor:0,patch:0,revision:"",toString:function(){
var v=dojo.version;
return v.major+"."+v.minor+"."+v.patch+" ("+v.revision+")";
}};
function dj_error_to_string(_9){
return ((!dj_undef("message",_9))?_9.message:(dj_undef("description",_9)?_9:_9.description));
}
function dj_debug(){
var _10=arguments;
if(dj_undef("println",dojo.hostenv)){
dj_throw("dj_debug not available (yet?)");
}
if(!dojo.hostenv.is_debug_){
return;
}
var _11=dj_global["jum"];
var s=_11?"":"DEBUG: ";
for(var i=0;i<_10.length;++i){
if(!false&&_10[i] instanceof Error){
var msg="["+_10[i].name+": "+dj_error_to_string(_10[i])+(_10[i].fileName?", file: "+_10[i].fileName:"")+(_10[i].lineNumber?", line: "+_10[i].lineNumber:"")+"]";
}else{
var msg=_10[i];
}
s+=msg+" ";
}
if(_11){
jum.debug(s);
}else{
dojo.hostenv.println(s);
}
}
function dj_throw(_14){
var he=dojo.hostenv;
if(dj_undef("hostenv",dojo)&&dj_undef("println",dojo)){
dojo.hostenv.println("FATAL: "+_14);
}
throw Error(_14);
}
function dj_rethrow(_16,_17){
var _18=dj_error_to_string(_17);
dj_throw(_16+": "+_18);
}
function dj_eval(s){
return dj_global.eval?dj_global.eval(s):eval(s);
}
function dj_unimplemented(_19,_20){
var _21="'"+_19+"' not implemented";
if((typeof _20!="undefined")&&(_20)){
_21+=" "+_20;
}
dj_throw(_21);
}
function dj_deprecated(_22,_23){
var _24="DEPRECATED: "+_22;
if((typeof _23!="undefined")&&(_23)){
_24+=" "+_23;
}
dj_debug(_24);
}
function dj_inherits(_25,_26){
if(typeof _26!="function"){
dj_throw("superclass: "+_26+" borken");
}
_25.prototype=new _26();
_25.prototype.constructor=_25;
_25.superclass=_26.prototype;
_25["super"]=_26.prototype;
}
dojo.render={name:"",ver:0,os:{win:false,linux:false,osx:false},html:{capable:false,support:{builtin:false,plugin:false},ie:false,opera:false,khtml:false,safari:false,moz:false,prefixes:["html"]},svg:{capable:false,support:{builtin:false,plugin:false},corel:false,adobe:false,batik:false,prefixes:["svg"]},swf:{capable:false,support:{builtin:false,plugin:false},mm:false,prefixes:["Swf","Flash","Mm"]},swt:{capable:false,support:{builtin:false,plugin:false},ibm:false,prefixes:["Swt"]}};
dojo.hostenv=(function(){
var djc=djConfig;
function _def(obj,_29,def){
return (dj_undef(_29,obj)?def:obj[_29]);
}
return {is_debug_:_def(djc,"isDebug",false),base_script_uri_:_def(djc,"baseScriptUri",undefined),base_relative_path_:_def(djc,"baseRelativePath",""),library_script_uri_:_def(djc,"libraryScriptUri",""),auto_build_widgets_:_def(djc,"parseWidgets",true),ie_prevent_clobber_:_def(djc,"iePreventClobber",false),ie_clobber_minimal_:_def(djc,"ieClobberMinimal",false),name_:"(unset)",version_:"(unset)",pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_31,_32){
this.modulePrefixes_[_31]={name:_31,value:_32};
},getModulePrefix:function(_33){
var mp=this.modulePrefixes_;
if((mp[_33])&&(mp[_33]["name"])){
return mp[_33].value;
}
return _33;
},getTextStack:[],loadUriStack:[],loadedUris:[],modules_:{},modulesLoadedFired:false,modulesLoadedListeners:[],getName:function(){
return this.name_;
},getVersion:function(){
return this.version_;
},getText:function(uri){
dj_unimplemented("getText","uri="+uri);
},getLibraryScriptUri:function(){
dj_unimplemented("getLibraryScriptUri","");
}};
})();
dojo.hostenv.getBaseScriptUri=function(){
if(!dj_undef("base_script_uri_",this)){
return this.base_script_uri_;
}
var uri=this.library_script_uri_;
if(!uri){
uri=this.library_script_uri_=this.getLibraryScriptUri();
if(!uri){
dj_throw("Nothing returned by getLibraryScriptUri(): "+uri);
}
}
var _36=uri.lastIndexOf("/");
this.base_script_uri_=this.base_relative_path_;
return this.base_script_uri_;
};
dojo.hostenv.setBaseScriptUri=function(uri){
this.base_script_uri_=uri;
};
dojo.hostenv.loadPath=function(_37,_38,cb){
if(!_37){
dj_throw("Missing relpath argument");
}
if((_37.charAt(0)=="/")||(_37.match(/^\w+:/))){
dj_throw("relpath '"+_37+"'; must be relative");
}
var uri=this.getBaseScriptUri()+_37;
try{
return ((!_38)?this.loadUri(uri):this.loadUriAndCheck(uri,_38));
}
catch(e){
if(dojo.hostenv.is_debug_){
dj_debug(e);
}
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(dojo.hostenv.loadedUris[uri]){
return;
}
var _40=this.getText(uri,null,true);
if(_40==null){
return 0;
}
var _41=dj_eval(_40);
return 1;
};
dojo.hostenv.getDepsForEval=function(_42){
if(!_42){
_42="";
}
var _43=[];
var tmp=_42.match(/dojo.hostenv.loadModule\(.*?\)/mg);
if(tmp){
for(var x=0;x<tmp.length;x++){
_43.push(tmp[x]);
}
}
tmp=_42.match(/dojo.hostenv.require\(.*?\)/mg);
if(tmp){
for(var x=0;x<tmp.length;x++){
_43.push(tmp[x]);
}
}
tmp=_42.match(/dojo.require\(.*?\)/mg);
if(tmp){
for(var x=0;x<tmp.length;x++){
_43.push(tmp[x]);
}
}
tmp=_42.match(/dojo.hostenv.conditionalLoadModule\([\w\W]*?\)/gm);
if(tmp){
for(var x=0;x<tmp.length;x++){
_43.push(tmp[x]);
}
}
return _43;
};
dojo.hostenv.loadUriAndCheck=function(uri,_46,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dj_debug("failed loading ",uri," with error: ",e);
}
return ((ok)&&(this.findModule(_46,false)))?true:false;
};
dojo.loaded=function(){
};
dojo.hostenv.loaded=function(){
this.modulesLoadedFired=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
}
dojo.loaded();
};
dojo.addOnLoad=function(obj,_49){
if(arguments.length==1){
dojo.hostenv.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dojo.hostenv.modulesLoadedListeners.push(function(){
obj[_49]();
});
}
}
};
dojo.hostenv.modulesLoaded=function(){
if(this.modulesLoadedFired){
return;
}
if((this.loadUriStack.length==0)&&(this.getTextStack.length==0)){
if(this.inFlightCount>0){
dj_debug("couldn't initialize, there are files still in flight");
return;
}
this.loaded();
}
};
dojo.hostenv.moduleLoaded=function(_50){
var _51=dj_eval_object_path((_50.split(".").slice(0,-1)).join("."));
this.loaded_modules_[(new String(_50)).toLowerCase()]=_51;
};
dojo.hostenv.loadModule=function(_52,_53,_54){
var _55=this.findModule(_52,false);
if(_55){
return _55;
}
if(dj_undef(_52,this.loading_modules_)){
this.addedToLoadingCount.push(_52);
}
this.loading_modules_[_52]=1;
var _56=_52.replace(/\./g,"/")+".js";
var _57=_52.split(".");
var _58=_52.split(".");
for(var i=_57.length-1;i>0;i--){
var _59=_57.slice(0,i).join(".");
var _60=this.getModulePrefix(_59);
if(_60!=_59){
_57.splice(0,i,_60);
break;
}
}
var _61=_57[_57.length-1];
if(_61=="*"){
_52=(_58.slice(0,-1)).join(".");
while(_57.length){
_57.pop();
_57.push(this.pkgFileName);
_56=_57.join("/")+".js";
if(_56.charAt(0)=="/"){
_56=_56.slice(1);
}
ok=this.loadPath(_56,((!_54)?_52:null));
if(ok){
break;
}
_57.pop();
}
}else{
_56=_57.join("/")+".js";
_52=_58.join(".");
var ok=this.loadPath(_56,((!_54)?_52:null));
if((!ok)&&(!_53)){
_57.pop();
while(_57.length){
_56=_57.join("/")+".js";
ok=this.loadPath(_56,((!_54)?_52:null));
if(ok){
break;
}
_57.pop();
_56=_57.join("/")+"/"+this.pkgFileName+".js";
if(_56.charAt(0)=="/"){
_56=_56.slice(1);
}
ok=this.loadPath(_56,((!_54)?_52:null));
if(ok){
break;
}
}
}
if((!ok)&&(!_54)){
dj_throw("Could not load '"+_52+"'; last tried '"+_56+"'");
}
}
if(!_54){
_55=this.findModule(_52,false);
if(!_55){
dj_throw("symbol '"+_52+"' is not defined after loading '"+_56+"'");
}
}
return _55;
};
function dj_load(_62,_63){
return dojo.hostenv.loadModule(_62,_63);
}
dojo.hostenv.startPackage=function(_64){
var _65=_64.split(/\./);
if(_65[_65.length-1]=="*"){
_65.pop();
}
return dj_eval_object_path(_65.join("."),true);
};
dojo.hostenv.findModule=function(_66,_67){
if(!dj_undef(_66,this.modules_)){
return this.modules_[_66];
}
if(this.loaded_modules_[(new String(_66)).toLowerCase()]){
return this.loaded_modules_[_66];
}
var _68=dj_eval_object_path(_66);
if((typeof _68!=="undefined")&&(_68)){
return this.modules_[_66]=_68;
}
if(_67){
dj_throw("no loaded module named '"+_66+"'");
}
return null;
};
dj_addNodeEvtHdlr=function(_69,_70,fp,_72){
if(_69.attachEvent){
_69.attachEvent("on"+_70,fp);
}else{
if(_69.addEventListener){
_69.addEventListener(_70,fp,_72);
}else{
var _73=_69["on"+_70];
if(typeof _73!="undefined"){
_69["on"+_70]=function(){
fp.apply(_69,arguments);
_73.apply(_69,arguments);
};
}else{
_69["on"+_70]=fp;
}
}
}
return true;
};
if(typeof window=="undefined"){
dj_throw("no window object");
}
(function(){
if((dojo.hostenv["base_script_uri_"]==""||dojo.hostenv["base_relative_path_"]=="")&&document&&document.getElementsByTagName){
var _74=document.getElementsByTagName("script");
var _75=/(__package__|dojo)\.js$/i;
for(var i=0;i<_74.length;i++){
var src=_74[i].getAttribute("src");
if(_75.test(src)){
var _77=src.replace(_75,"");
if(dojo.hostenv["base_script_uri_"]==""){
dojo.hostenv["base_script_uri_"]=_77;
}
if(dojo.hostenv["base_relative_path_"]==""){
dojo.hostenv["base_relative_path_"]=_77;
}
break;
}
}
}
})();
with(dojo.render){
html.UA=navigator.userAgent;
html.AV=navigator.appVersion;
html.capable=true;
html.support.builtin=true;
ver=parseFloat(html.AV);
os.mac=html.AV.indexOf("Macintosh")==-1?false:true;
os.win=html.AV.indexOf("Windows")==-1?false:true;
html.opera=html.UA.indexOf("Opera")==-1?false:true;
html.khtml=((html.AV.indexOf("Konqueror")>=0)||(html.AV.indexOf("Safari")>=0))?true:false;
html.safari=(html.AV.indexOf("Safari")>=0)?true:false;
html.mozilla=html.moz=((html.UA.indexOf("Gecko")>=0)&&(!html.khtml))?true:false;
html.ie=((document.all)&&(!html.opera))?true:false;
html.ie50=html.ie&&html.AV.indexOf("MSIE 5.0")>=0;
html.ie55=html.ie&&html.AV.indexOf("MSIE 5.5")>=0;
html.ie60=html.ie&&html.AV.indexOf("MSIE 6.0")>=0;
}
dojo.hostenv.startPackage("dojo.hostenv");
dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
var DJ_XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _78=null;
var _79=null;
try{
_78=new XMLHttpRequest();
}
catch(e){
}
if(!_78){
for(var i=0;i<3;++i){
var _80=DJ_XMLHTTP_PROGIDS[i];
try{
_78=new ActiveXObject(_80);
}
catch(e){
_79=e;
}
if(_78){
DJ_XMLHTTP_PROGIDS=[_80];
break;
}
}
}
if((_79)&&(!_78)){
dj_rethrow("Could not create a new ActiveXObject using any of the progids "+DJ_XMLHTTP_PROGIDS.join(", "),_79);
}else{
if(!_78){
return dj_throw("No XMLHTTP implementation available, for uri "+uri);
}
}
return _78;
};
dojo.hostenv.getText=function(uri,_81,_82){
var _83=this.getXmlhttpObject();
if(_81){
_83.onreadystatechange=function(){
if((4==_83.readyState)&&(_83["status"])){
if(_83.status==200){
dj_debug("LOADED URI: "+uri);
_81(_83.responseText);
}
}
};
}
_83.open("GET",uri,_81?true:false);
_83.send(null);
if(_81){
return null;
}
return _83.responseText;
};
function dj_last_script_src(){
var _84=window.document.getElementsByTagName("script");
if(_84.length<1){
dj_throw("No script elements in window.document, so can't figure out my script src");
}
var _85=_84[_84.length-1];
var src=_85.src;
if(!src){
dj_throw("Last script element (out of "+_84.length+") has no src");
}
return src;
}
if(!dojo.hostenv["library_script_uri_"]){
dojo.hostenv.library_script_uri_=dj_last_script_src();
}
dojo.hostenv.println=function(s){
var ti=null;
var dis="<div>"+s+"</div>";
try{
ti=document.createElement("div");
document.body.appendChild(ti);
ti.innerHTML=s;
}
catch(e){
try{
document.write(dis);
}
catch(e2){
window.status=s;
}
}
delete ti;
delete dis;
delete s;
};
dj_addNodeEvtHdlr(window,"load",function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
dojo.hostenv.modulesLoaded();
});
dojo.hostenv.makeWidgets=function(){
if((dojo.hostenv.auto_build_widgets_)||(dojo.hostenv.searchIds.length>0)){
if(dj_eval_object_path("dojo.widget.Parse")){
try{
var _88=new dojo.xml.Parse();
var _89=dojo.hostenv.searchIds;
if(_89.length>0){
for(var x=0;x<_89.length;x++){
if(!document.getElementById(_89[x])){
continue;
}
var _90=_88.parseElement(document.getElementById(_89[x]),null,true);
dojo.widget.getParser().createComponents(_90);
}
}else{
if(dojo.hostenv.auto_build_widgets_){
var _90=_88.parseElement(document.body,null,true);
dojo.widget.getParser().createComponents(_90);
}
}
}
catch(e){
dj_debug("auto-build-widgets error:",e);
}
}
}
};
dojo.hostenv.modulesLoadedListeners.push(function(){
if(!dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
});
if((!window["djConfig"])||(!window["djConfig"]["preventBackButtonFix"])){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"'></iframe>");
}
dojo.hostenv.writeIncludes=function(){
};
dojo.hostenv.conditionalLoadModule=function(_91){
var _92=_91["common"]||[];
var _93=(_91[dojo.hostenv.name_])?_92.concat(_91[dojo.hostenv.name_]||[]):_92.concat(_91["default"]||[]);
for(var x=0;x<_93.length;x++){
var _94=_93[x];
if(_94.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_94);
}else{
dojo.hostenv.loadModule(_94);
}
}
};
dojo.hostenv.require=dojo.hostenv.loadModule;
dojo.require=function(){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(){
if((arguments[0]=="common")||(dojo.render[arguments[0]].capable)){
dojo.require(arguments[1],arguments[2],arguments[3]);
}
};
dojo.conditionalRequire=dojo.requireIf;
dojo.kwCompoundRequire=function(){
dojo.hostenv.conditionalLoadModule.apply(dojo.hostenv,arguments);
};
dojo.hostenv.provide=dojo.hostenv.startPackage;
dojo.provide=function(){
dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.hostenv.startPackage("dojo.io.IO");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error"];
dojo.io.Request=function(url,mt,_97,_98){
this.url=url;
this.mimetype=mt;
this.transport=_97;
this.changeUrl=_98;
this.formNode=null;
this.events_={};
var _99=this;
this.error=function(type,_101){
switch(type){
case "io":
var _102=dojo.io.IOEvent.IO_ERROR;
var _103="IOError: error during IO";
break;
case "parse":
var _102=dojo.io.IOEvent.PARSE_ERROR;
var _103="IOError: error during parsing";
default:
var _102=dojo.io.IOEvent.UNKOWN_ERROR;
var _103="IOError: cause unkown";
}
var _104=new dojo.io.IOEvent("error",null,_99,_103,this.url,_102);
_99.dispatchEvent(_104);
if(_99.onerror){
_99.onerror(_103,_99.url,_104);
}
};
this.load=function(type,data,evt){
var _107=new dojo.io.IOEvent("load",data,_99,null,null,null);
_99.dispatchEvent(_107);
if(_99.onload){
_99.onload(_107);
}
};
this.backButton=function(){
var _108=new dojo.io.IOEvent("backbutton",null,_99,null,null,null);
_99.dispatchEvent(_108);
if(_99.onbackbutton){
_99.onbackbutton(_108);
}
};
this.forwardButton=function(){
var _109=new dojo.io.IOEvent("forwardbutton",null,_99,null,null,null);
_99.dispatchEvent(_109);
if(_99.onforwardbutton){
_99.onforwardbutton(_109);
}
};
};
dojo.io.Request.prototype.addEventListener=function(type,func){
if(!this.events_[type]){
this.events_[type]=[];
}
for(var i=0;i<this.events_[type].length;i++){
if(this.events_[type][i]==func){
return;
}
}
this.events_[type].push(func);
};
dojo.io.Request.prototype.removeEventListener=function(type,func){
if(!this.events_[type]){
return;
}
for(var i=0;i<this.events_[type].length;i++){
if(this.events_[type][i]==func){
this.events_[type].splice(i,1);
}
}
};
dojo.io.Request.prototype.dispatchEvent=function(evt){
if(!this.events_[evt.type]){
return;
}
for(var i=0;i<this.events_[evt.type].length;i++){
this.events_[evt.type][i](evt);
}
return false;
};
dojo.io.IOEvent=function(type,data,_111,_112,_113,_114){
this.type=type;
this.data=data;
this.request=_111;
this.errorMessage=_112;
this.errorUrl=_113;
this.errorCode=_114;
};
dojo.io.IOEvent.UNKOWN_ERROR=0;
dojo.io.IOEvent.IO_ERROR=1;
dojo.io.IOEvent.PARSE_ERROR=2;
dojo.io.Error=function(msg,type,num){
this.message=msg;
this.type=type||"unknown";
this.number=num||0;
};
dojo.io.transports.addTransport=function(name){
this.push(name);
this[name]=dojo.io[name];
};
dojo.io.bind=function(_117){
if(!_117["url"]){
_117.url="";
}else{
_117.url=_117.url.toString();
}
if(!_117["mimetype"]){
_117.mimetype="text/plain";
}
if(!_117["method"]&&!_117["formNode"]){
_117.method="get";
}else{
if(_117["formNode"]){
_117.method=_117["method"]||_117["formNode"].method||"get";
}
}
if(_117["handler"]){
_117.handle=_117.handler;
}
if(!_117["handle"]){
_117.handle=function(){
};
}
if(_117["loaded"]){
_117.load=_117.loaded;
}
if(_117["changeUrl"]){
_117.changeURL=_117.changeUrl;
}
for(var x=0;x<this.hdlrFuncNames.length;x++){
var fn=this.hdlrFuncNames[x];
if(typeof _117[fn]=="function"){
continue;
}
if(typeof _117.handler=="object"){
if(typeof _117.handler[fn]=="function"){
_117[fn]=_117.handler[fn]||_117.handler["handle"]||function(){
};
}
}else{
if(typeof _117["handler"]=="function"){
_117[fn]=_117.handler;
}else{
if(typeof _117["handle"]=="function"){
_117[fn]=_117.handle;
}
}
}
}
var _119="";
if(_117["transport"]){
_119=_117["transport"];
if(!this[_119]){
return false;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_117))){
_119=tmp;
}
}
if(_119==""){
return false;
}
}
this[_119].bind(_117);
return true;
};
dojo.io.argsFromMap=function(map){
var _121=new Object();
var _122="";
for(var x in map){
if(!_121[x]){
_122+=encodeURIComponent(x)+"="+encodeURIComponent(map[x])+"&";
}
}
return _122;
};
dojo.provide("dojo.alg.Alg");
dojo.alg.find=function(arr,val){
for(var i=0;i<arr.length;++i){
if(arr[i]==val){
return i;
}
}
return -1;
};
dojo.alg.inArray=function(arr,val){
if((!arr||arr.constructor!=Array)&&(val&&val.constructor==Array)){
var a=arr;
arr=val;
val=a;
}
return dojo.alg.find(arr,val)>-1;
};
dojo.alg.inArr=dojo.alg.inArray;
dojo.alg.getNameInObj=function(ns,item){
if(!ns){
ns=dj_global;
}
for(var x in ns){
if(ns[x]===item){
return new String(x);
}
}
return null;
};
dojo.alg.has=function(obj,name){
return (typeof obj[name]!=="undefined");
};
dojo.alg.forEach=function(arr,_128,_129){
var il=arr.length;
for(var i=0;i<((_129)?il:arr.length);i++){
if(_128(arr[i])=="break"){
break;
}
}
};
dojo.alg.for_each=dojo.alg.forEach;
dojo.alg.map=function(arr,obj,_131){
for(var i=0;i<arr.length;++i){
_131.call(obj,arr[i]);
}
};
dojo.alg.tryThese=function(){
for(var x=0;x<arguments.length;x++){
try{
if(typeof arguments[x]=="function"){
var ret=(arguments[x]());
if(ret){
return ret;
}
}
}
catch(e){
dj_debug(e);
}
}
};
dojo.alg.delayThese=function(farr,cb,_134,_135){
if(!farr.length){
if(typeof _135=="function"){
_135();
}
return;
}
if((typeof _134=="undefined")&&(typeof cb=="number")){
_134=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.alg.delayThese(farr,cb,_134,_135);
},_134);
};
dojo.alg.for_each_call=dojo.alg.map;
dojo.hostenv.loadModule("dojo.alg.Alg",false,true);
dojo.hostenv.moduleLoaded("dojo.alg.*");
dojo.provide("dojo.io.BrowserIO");
dojo.require("dojo.io.IO");
dojo.require("dojo.alg.*");
dojo.io.checkChildrenForFile=function(node){
var _137=false;
var _138=node.getElementsByTagName("input");
dojo.alg.forEach(_138,function(_139){
if(_137){
return;
}
if(_139.getAttribute("type")=="file"){
_137=true;
}
});
return _137;
};
dojo.io.formHasFile=function(_140){
return dojo.io.checkChildrenForFile(_140);
};
dojo.io.buildFormGetString=function(_141){
var ec=encodeURIComponent;
var tvar="";
var ctyp=_141.nodeName?_141.nodeName.toLowerCase():"";
var etyp=_141.type?_141.type.toLowerCase():"";
if(((ctyp=="input")&&(etyp!="radio")&&(etyp!="checkbox"))||(ctyp=="select")||(ctyp=="textarea")){
if((ctyp=="input")&&(etyp=="submit")){
}else{
if(!((ctyp=="select")&&(_141.getAttribute("multiple")))){
tvar=ec(_141.getAttribute("name"))+"="+ec(_141.value)+"&";
}else{
var tn=ec(_141.getAttribute("name"));
var _147=_141.getElementsByTagName("option");
for(var x=0;x<_147.length;x++){
if(_147[x].selected){
tvar+=tn+"="+ec(_147[x].value)+"&";
}
}
}
}
}else{
if(ctyp=="input"){
if(_141.checked){
tvar=ec(_141.getAttribute("name"))+"="+ec(_141.value)+"&";
}
}
}
if(_141.hasChildNodes()){
for(var _148=(_141.childNodes.length-1);_148>=0;_148--){
tvar+=dojo.io.buildFormGetString(_141.childNodes.item(_148));
}
}
return tvar;
};
dojo.io.setIFrameSrc=function(_149,src,_150){
try{
var r=dojo.render.html;
if(!_150){
if(r.safari){
_149.location=src;
}else{
frames[_149.name].location=src;
}
}else{
var idoc=(r.moz)?_149.contentWindow:_149;
idoc.location.replace(src);
dj_debug(_149.contentWindow.location);
}
}
catch(e){
dj_debug("setIFrameSrc: "+e);
}
};
dojo.io.createIFrame=function(_153){
if(window[_153]){
return window[_153];
}
if(window.frames[_153]){
return window.frames[_153];
}
var r=dojo.render.html;
var _154=null;
_154=document.createElement((((r.ie)&&(r.win))?"<iframe name="+_153+">":"iframe"));
with(_154){
name=_153;
setAttribute("name",_153);
id=_153;
}
window[_153]=_154;
document.body.appendChild(_154);
with(_154.style){
position="absolute";
left=top="0px";
height=width="1px";
visibility="hidden";
if(dojo.hostenv.is_debug_){
position="relative";
height="100px";
width="300px";
visibility="visible";
}
}
dojo.io.setIFrameSrc(_154,dojo.hostenv.getBaseScriptUri()+"iframe_history.html",true);
return _154;
};
dojo.io.cancelDOMEvent=function(evt){
if(!evt){
return false;
}
if(evt.preventDefault){
evt.stopPropagation();
evt.preventDefault();
}else{
if(window.event){
window.event.cancelBubble=true;
window.event.returnValue=false;
}
}
return false;
};
dojo.io.XMLHTTPTransport=new function(){
var _155=this;
this.initialHref=window.location.href;
this.initialHash=window.location.hash;
this.moveForward=false;
var _156={};
this.useCache=false;
this.historyStack=[];
this.forwardStack=[];
this.historyIframe=null;
this.bookmarkAnchor=null;
this.locationTimer=null;
function getCacheKey(url,_157,_158){
return url+"|"+_157+"|"+_158.toLowerCase();
}
function addToCache(url,_159,_160,http){
_156[getCacheKey(url,_159,_160)]=http;
}
function getFromCache(url,_162,_163){
return _156[getCacheKey(url,_162,_163)];
}
this.clearCache=function(){
_156={};
};
function doLoad(_164,http,url,_165,_166){
if(http.status==200||(location.protocol=="file:"&&http.status==0)){
var ret;
if(_164.method.toLowerCase()=="head"){
var _167=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _167;
};
var _168=_167.split(/[\r\n]+/g);
for(var i=0;i<_168.length;i++){
var pair=_168[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_164.mimetype=="text/javascript"){
ret=dj_eval(http.responseText);
}else{
if(_164.mimetype=="text/xml"){
ret=http.responseXML;
if(!ret||typeof ret=="string"){
ret=dojo.xml.domUtil.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
}
if(_166){
addToCache(url,_165,_164.method,http);
}
if(typeof _164.load=="function"){
_164.load("load",ret,http);
}
}else{
var _170=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
if(typeof _164.error=="function"){
_164.error("error",_170,http);
}
}
}
function setHeaders(http,_171){
if(_171["headers"]){
for(var _172 in _171["headers"]){
if(_172.toLowerCase()=="content-type"&&!_171["contentType"]){
_171["contentType"]=_171["headers"][_172];
}else{
http.setRequestHeader(_172,_171["headers"][_172]);
}
}
}
}
this.addToHistory=function(args){
var _174=args["back"]||args["backButton"]||args["handle"];
var hash=null;
if(!this.historyIframe){
this.historyIframe=window.frames["djhistory"];
}
if(!this.bookmarkAnchor){
this.bookmarkAnchor=document.createElement("a");
document.body.appendChild(this.bookmarkAnchor);
this.bookmarkAnchor.style.display="none";
}
if((!args["changeURL"])||(dojo.render.html.ie)){
var url=dojo.hostenv.getBaseScriptUri()+"iframe_history.html?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
}
if(args["changeURL"]){
hash="#"+((args["changeURL"]!==true)?args["changeURL"]:(new Date()).getTime());
setTimeout("window.location.href = '"+hash+"';",1);
this.bookmarkAnchor.href=hash;
if(dojo.render.html.ie){
var _176=_174;
var lh=null;
var hsl=this.historyStack.length-1;
if(hsl>=0){
while(!this.historyStack[hsl]["urlHash"]){
hsl--;
}
lh=this.historyStack[hsl]["urlHash"];
}
if(lh){
_174=function(){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+lh+"';",1);
}
_176();
};
}
this.forwardStack=[];
var _179=args["forward"]||args["forwardbutton"];
var tfw=function(){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_179){
_179();
}
};
if(args["forward"]){
args.forward=tfw;
}else{
if(args["forwardButton"]){
args.forwardButton=tfw;
}
}
}else{
if(dojo.render.html.moz){
if(!this.locationTimer){
this.locationTimer=setInterval("dojo.io.XMLHTTPTransport.checkLocation();",200);
}
}
}
}
this.historyStack.push({"url":url,"callback":_174,"kwArgs":args,"urlHash":hash});
};
this.checkLocation=function(){
var hsl=this.historyStack.length;
if((window.location.hash==this.initialHash)||(window.location.href==this.initialHref)&&(hsl==1)){
this.handleBackButton();
return;
}
if(this.forwardStack.length>0){
if(this.forwardStack[this.forwardStack.length-1].urlHash==window.location.hash){
this.handleForwardButton();
return;
}
}
if((hsl>=2)&&(this.historyStack[hsl-2])){
if(this.historyStack[hsl-2].urlHash==window.location.hash){
this.handleBackButton();
return;
}
}
};
this.iframeLoaded=function(evt,_181){
var isp=_181.href.split("?");
if(isp.length<2){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
var _183=isp[1];
if(this.moveForward){
this.moveForward=false;
return;
}
var last=this.historyStack.pop();
if(!last){
if(this.forwardStack.length>0){
var next=this.forwardStack[this.forwardStack.length-1];
if(_183==next.url.split("?")[1]){
this.handleForwardButton();
}
}
return;
}
this.historyStack.push(last);
if(this.historyStack.length>=2){
if(isp[1]==this.historyStack[this.historyStack.length-2].url.split("?")[1]){
this.handleBackButton();
}
}else{
this.handleBackButton();
}
};
this.handleBackButton=function(){
var last=this.historyStack.pop();
if(!last){
return;
}
if(last["callback"]){
last.callback();
}else{
if(last.kwArgs["backButton"]){
last.kwArgs["backButton"]();
}else{
if(last.kwArgs["back"]){
last.kwArgs["back"]();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("back");
}
}
}
}
this.forwardStack.push(last);
};
this.handleForwardButton=function(){
var last=this.forwardStack.pop();
if(!last){
return;
}
if(last.kwArgs["forward"]){
last.kwArgs.back();
}else{
if(last.kwArgs["forwardButton"]){
last.kwArgs.forwardButton();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("forward");
}
}
}
this.historyStack.push(last);
};
var _186=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_187){
return _186&&dojo.alg.inArray(_187["mimetype"],["text/plain","text/html","text/xml","text/javascript"])&&dojo.alg.inArray(_187["method"].toLowerCase(),["post","get","head"])&&!(_187["formNode"]&&dojo.io.formHasFile(_187["formNode"]));
};
this.bind=function(_188){
if(!_188["url"]){
if(!_188["formNode"]&&(_188["backButton"]||_188["back"]||_188["changeURL"]||_188["watchForURL"])&&(!window["djConfig"]&&!window["djConfig"]["preventBackButtonFix"])){
this.addToHistory(_188);
return true;
}
}
var url=_188.url;
var _189="";
if(_188["formNode"]){
var ta=_188.formNode.getAttribute("action");
if((ta)&&(!_188["url"])){
url=ta;
}
var tp=_188.formNode.getAttribute("method");
if((tp)&&(!_188["method"])){
_188.method=tp;
}
_189+=dojo.io.buildFormGetString(_188.formNode);
}
if(!_188["method"]){
_188.method="get";
}
if(_188["content"]){
_189+=dojo.io.argsFromMap(_188.content);
}
if(_188["postContent"]&&_188.method.toLowerCase()=="post"){
_189=_188.postContent;
}
if(_188["backButton"]||_188["back"]||_188["changeURL"]){
this.addToHistory(_188);
}
var _192=_188["sync"]?false:true;
var _193=_188["useCache"]==true||(this.useCache==true&&_188["useCache"]!=false);
if(_193){
var _194=getFromCache(url,_189,_188.method);
if(_194){
doLoad(_188,_194,url,_189,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject();
var _195=false;
if(_192){
http.onreadystatechange=function(){
if(4==http.readyState){
if(_195){
return;
}
_195=true;
doLoad(_188,http,url,_189,_193);
}
};
}
if(_188.method.toLowerCase()=="post"){
http.open("POST",url,_192);
setHeaders(http,_188);
http.setRequestHeader("Content-Type",_188["contentType"]||"application/x-www-form-urlencoded");
http.send(_189);
}else{
var _196=url;
if(_189!=""){
_196+=(url.indexOf("?")>-1?"&":"?")+_189;
}
http.open(_188.method.toUpperCase(),_196,_192);
setHeaders(http,_188);
http.send(null);
}
if(!_192){
doLoad(_188,http,url,_189,_193);
}
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};

