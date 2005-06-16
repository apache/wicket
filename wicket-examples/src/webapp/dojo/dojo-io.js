/* Copyright (c) 2004-2005 The Dojo Foundation, Licensed under the Academic Free License version 2.1 or above */if(typeof djConfig=="undefined"){
var djConfig={};
}
var dojo;
if(typeof dojo=="undefined"){
dojo={};
}
var dj_global=this;
function dj_debug(){
var _1=arguments;
if(typeof dojo.hostenv.println!="function"){
dj_throw("attempt to call dj_debug when there is no dojo.hostenv println implementation (yet?)");
}
if(!dojo.hostenv.is_debug_){
return;
}
var _2=dj_global["jum"];
var s=_2?"":"DEBUG: ";
for(var i=0;i<_1.length;++i){
s+=_1[i];
}
if(_2){
jum.debug(s);
}else{
dojo.hostenv.println(s);
}
}
function dj_throw(_5){
if((typeof dojo.hostenv!="undefined")&&(typeof dojo.hostenv.println!="undefined")){
dojo.hostenv.println("fatal error: "+_5);
}
throw Error(_5);
}
function dj_error_to_string(_6){
return (typeof _6.message!=="undefined"?_6.message:(typeof _6.description!=="undefined"?_6.description:_6));
}
function dj_rethrow(_7,_8){
var _9=dj_error_to_string(_8);
dj_throw(_7+": "+_9);
}
function dj_eval(s){
return dj_global.eval?dj_global.eval(s):eval(s);
}
function dj_unimplemented(_10,_11){
var _12="No implementation of function '"+_10+"'";
if((typeof _11!="undefined")&&(_11)){
_12+=" "+_11;
}
_12+=" (host environment '"+dojo.hostenv.getName()+"')";
dj_throw(_12);
}
function dj_inherits(_13,_14){
if(typeof _14!="function"){
dj_throw("eek: superclass not a function: "+_14+"\nsubclass is: "+_13);
}
_13.prototype=new _14();
_13.prototype.constructor=_13;
_13["super"]=_14;
}
dojo.render={name:"",ver:0,os:{win:false,linux:false,osx:false},html:{capable:false,support:{builtin:false,plugin:false},ie:false,opera:false,khtml:false,safari:false,moz:false},svg:{capable:false,support:{builtin:false,plugin:false},corel:false,adobe:false,batik:false},swf:{capable:false,support:{builtin:false,plugin:false},mm:false},swt:{capable:false,support:{builtin:false,plugin:false},ibm:false}};
dojo.hostenv={is_debug_:((typeof djConfig["isDebug"]=="undefined")?false:djConfig["isDebug"]),base_script_uri_:((typeof djConfig["baseScriptUri"]=="undefined")?undefined:djConfig["baseScriptUri"]),base_relative_path_:((typeof djConfig["baseRelativePath"]=="undefined")?"":djConfig["baseRelativePath"]),library_script_uri_:((typeof djConfig["libraryScriptUri"]=="undefined")?"":djConfig["libraryScriptUri"]),auto_build_widgets_:((typeof djConfig["parseWidgets"]=="undefined")?true:djConfig["parseWidgets"]),loading_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modules_:{}};
dojo.hostenv.name_="(unset)";
dojo.hostenv.version_="(unset)";
dojo.hostenv.pkgFileName="__package__";
dojo.hostenv.getName=function(){
return this.name_;
};
dojo.hostenv.getVersion=function(){
return this.version_;
};
dojo.hostenv.getText=function(uri){
dj_unimplemented("dojo.hostenv.getText","uri="+uri);
};
dojo.hostenv.getLibraryScriptUri=function(){
dj_unimplemented("dojo.hostenv.getLibraryScriptUri","");
};
dojo.hostenv.getBaseScriptUri=function(){
if(typeof this.base_script_uri_!="undefined"){
return this.base_script_uri_;
}
var uri=this.library_script_uri_;
if(!uri){
uri=this.library_script_uri_=this.getLibraryScriptUri();
if(!uri){
dj_throw("Nothing returned by getLibraryScriptUri(): "+uri);
}
}
var _16=uri.lastIndexOf("/");
this.base_script_uri_=this.base_relative_path_;
return this.base_script_uri_;
};
dojo.hostenv.setBaseScriptUri=function(uri){
this.base_script_uri_=uri;
};
dojo.hostenv.loadPath=function(_17,_18,cb){
if(!_17){
dj_throw("Missing relpath argument");
}
if((_17.charAt(0)=="/")||(_17.match(/^\w+:/))){
dj_throw("Illegal argument '"+_17+"'; must be relative path");
}
var _20=this.getBaseScriptUri();
var uri=_20+_17;
try{
var ok;
if(!_18){
ok=this.loadUri(uri);
}else{
ok=this.loadUriAndCheck(uri,_18);
}
return ok;
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
var _22=this.getText(uri,null,true);
if(_22==null){
return 0;
}
var _23=dj_eval(_22);
return 1;
};
dojo.hostenv.getDepsForEval=function(_24){
if(!_24){
_24="";
}
var _25=[];
var tmp=_24.match(/dojo.hostenv.loadModule\(.*?\)/mg);
if(tmp){
for(var x=0;x<tmp.length;x++){
_25.push(tmp[x]);
}
}
tmp=_24.match(/dojo.hostenv.require\(.*?\)/mg);
if(tmp){
for(var x=0;x<tmp.length;x++){
_25.push(tmp[x]);
}
}
tmp=_24.match(/dojo.hostenv.conditionalLoadModule\([\w\W]*?\)/gm);
if(tmp){
for(var x=0;x<tmp.length;x++){
_25.push(tmp[x]);
}
}
return _25;
};
dojo.hostenv.getTextStack=[];
dojo.hostenv.loadUriStack=[];
dojo.hostenv.loadedUris=[];
dojo.hostenv.loadUriAndCheck=function(uri,_28,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dj_debug("failed loading ",uri," with error: ",e);
}
return ((ok)&&(this.findModule(_28,false)))?true:false;
};
dojo.hostenv.modulesLoadedFired=false;
dojo.hostenv.modulesLoadedListeners=[];
dojo.hostenv.loaded=function(){
this.modulesLoadedFired=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
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
dojo.hostenv.loadModule=function(_30,_31,_32){
var _33=this.findModule(_30,false);
if(_33){
return _33;
}
if(typeof this.loading_modules_[_30]!=="undefined"){
dj_debug("recursive attempt to load module '"+_30+"'");
}else{
this.addedToLoadingCount.push(_30);
}
this.loading_modules_[_30]=1;
var _34=_30.replace(/\./g,"/")+".js";
var _35=_30.split(".");
var _36=_30.split(".");
if(_35[0]=="dojo"){
_35[0]="src";
}
var _37=_35.pop();
_35.push(_37);
if(_37=="*"){
_30=(_36.slice(0,-1)).join(".");
var _33=this.findModule(_30,0);
if(_33){
return _33;
}
while(_35.length){
_35.pop();
_35.push("__package__");
_34=_35.join("/")+".js";
if(_34.charAt(0)=="/"){
_34=_34.slice(1);
}
ok=this.loadPath(_34,((!_32)?_30:null));
if(ok){
break;
}
_35.pop();
}
}else{
_34=_35.join("/")+".js";
_30=_36.join(".");
var ok=this.loadPath(_34,((!_32)?_30:null));
if((!ok)&&(!_31)){
_35.pop();
while(_35.length){
_34=_35.join("/")+".js";
ok=this.loadPath(_34,((!_32)?_30:null));
if(ok){
break;
}
_35.pop();
_34=_35.join("/")+"/__package__.js";
if(_34.charAt(0)=="/"){
_34=_34.slice(1);
}
ok=this.loadPath(_34,((!_32)?_30:null));
if(ok){
break;
}
}
}
if((!ok)&&(!_32)){
dj_throw("Could not find module '"+_30+"'; last tried path '"+_34+"'");
}
}
if(!_32){
_33=this.findModule(_30,false);
if(!_33){
dj_throw("Module symbol '"+_30+"' is not defined after loading '"+_34+"'");
}
}
return _33;
};
function dj_load(_38,_39){
return dojo.hostenv.loadModule(_38,_39);
}
function dj_eval_object_path(_40){
if(typeof _40!="string"){
return dj_global;
}
if(_40.indexOf(".")==-1){
dj_debug("typeof this[",_40,"]=",typeof (this[_40])," and typeof dj_global[]=",typeof (dj_global[_40]));
return (typeof dj_global[_40]=="undefined")?undefined:dj_global[_40];
}
var _41=_40.split(/\./);
var obj=dj_global;
for(var i=0;i<_41.length;++i){
obj=obj[_41[i]];
if((typeof obj=="undefined")||(!obj)){
return obj;
}
}
return obj;
}
dojo.hostenv.startPackage=function(_43){
var _44=_43.split(/\./);
if(_44[_44.length-1]=="*"){
_44.pop();
dj_debug("startPackage: popped a *, new packagename is : ",sysm.join("."));
}
var obj=dj_global;
var _45="dj_global";
for(var i=0;i<_44.length;++i){
var _46=obj[_44[i]];
_45+="."+_44[i];
if((eval("typeof "+_45+" == 'undefined'"))||(eval("!"+_45))){
dj_debug("startPackage: defining: ",_44.slice(0,i+1).join("."));
obj=dj_global;
for(var x=0;x<i;x++){
obj=obj[_44[x]];
}
obj[_44[i]]={};
}
}
return obj;
};
dojo.hostenv.findModule=function(_47,_48){
if(typeof this.modules_[_47]!="undefined"){
return this.modules_[_47];
}
var _49=dj_eval_object_path(_47);
if((typeof _49!=="undefined")&&(_49)){
return this.modules_[_47]=_49;
}
if(_48){
dj_throw("no loaded module named '"+_47+"'");
}
return null;
};
if(typeof window=="undefined"){
dj_throw("no window object");
}
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
html.moz=((html.UA.indexOf("Gecko")>=0)&&(!html.khtml))?true:false;
html.ie=((document.all)&&(!html.opera))?true:false;
html.ie50=html.ie&&html.AV.indexOf("MSIE 5.0")>=0;
html.ie55=html.ie&&html.AV.indexOf("MSIE 5.5")>=0;
html.ie60=html.ie&&html.AV.indexOf("MSIE 6.0")>=0;
}
dojo.hostenv.startPackage("dojo.hostenv");
dojo.hostenv.name_="browser";
var DJ_XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _50=null;
var _51=null;
try{
_50=new XMLHttpRequest();
}
catch(e){
}
if(!_50){
for(var i=0;i<3;++i){
var _52=DJ_XMLHTTP_PROGIDS[i];
try{
_50=new ActiveXObject(_52);
}
catch(e){
_51=e;
}
if(_50){
DJ_XMLHTTP_PROGIDS=[_52];
break;
}else{
}
}
}
if((_51)&&(!_50)){
dj_rethrow("Could not create a new ActiveXObject using any of the progids "+DJ_XMLHTTP_PROGIDS.join(", "),_51);
}else{
if(!_50){
return dj_throw("No XMLHTTP implementation available, for uri "+uri);
}
}
return _50;
};
dojo.hostenv.getText=function(uri,_53,_54){
var _55=this.getXmlhttpObject();
if(_53){
_55.onreadystatechange=function(){
if((4==_55.readyState)&&(_55["status"])){
if(_55.status==200){
dj_debug("LOADED URI: "+uri);
_53(_55.responseText);
}
}
};
}
_55.open("GET",uri,_53?true:false);
_55.send(null);
if(_53){
return null;
}
return _55.responseText;
};
function dj_last_script_src(){
var _56=window.document.getElementsByTagName("script");
if(_56.length<1){
dj_throw("No script elements in window.document, so can't figure out my script src");
}
var _57=_56[_56.length-1];
var src=_57.src;
if(!src){
dj_throw("Last script element (out of "+_56.length+") has no src");
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
window.onload=function(evt){
dojo.hostenv.modulesLoaded();
};
dojo.hostenv.modulesLoadedListeners.push(function(){
if(dojo.hostenv.auto_build_widgets_){
if(dj_eval_object_path("dojo.webui.widgets.Parse")){
try{
var _62=new dojo.xml.Parse();
var _63=_62.parseElement(document.body,null,true);
var _64=new dojo.webui.widgets.Parse(_63);
_64.createComponents(_63);
}
catch(e){
dj_debug("auto-build-widgets error: "+e);
}
}
}
});
if((!window["djConfig"])||(!window["djConfig"]["preventBackButtonFix"])){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(dojo.hostenv.getBaseScriptUri()+"/blank.html")+"'></iframe>");
}
dojo.hostenv.conditionalLoadModule=function(_65){
var _66=_65["common"]||[];
var _67=(_65[dojo.hostenv.name_])?_66.concat(_65[dojo.hostenv.name_]||[]):_66.concat(_65["default"]||[]);
for(var x=0;x<_67.length;x++){
var _68=_67[x];
if(_68.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_68);
}else{
dojo.hostenv.loadModule(_68);
}
}
};
dojo.hostenv.require=dojo.hostenv.loadModule;
dojo.hostenv.provide=dojo.hostenv.startPackage;
dj_debug("Using host environment: ",dojo.hostenv.name_);
dj_debug("getBaseScriptUri()=",dojo.hostenv.getBaseScriptUri());
dojo.hostenv.startPackage("dojo.io.IO");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error"];
dojo.io.Request=function(url,mt,_71,_72){
this.url=url;
this.mimetype=mt;
this.transport=_71;
this.changeUrl=_72;
this.formNode=null;
this.events_={};
var _73=this;
this.error=function(_74,_75){
switch(_74){
case "io":
var _76=dojo.io.IOEvent.IO_ERROR;
var _77="IOError: error during IO";
break;
case "parse":
var _76=dojo.io.IOEvent.PARSE_ERROR;
var _77="IOError: error during parsing";
default:
var _76=dojo.io.IOEvent.UNKOWN_ERROR;
var _77="IOError: cause unkown";
}
var _78=new dojo.io.IOEvent("error",null,_73,_77,this.url,_76);
_73.dispatchEvent(_78);
if(_73.onerror){
_73.onerror(_77,_73.url,_78);
}
};
this.load=function(_79,_80,evt){
var _81=new dojo.io.IOEvent("load",_80,_73,null,null,null);
_73.dispatchEvent(_81);
if(_73.onload){
_73.onload(_81);
}
};
this.backButton=function(){
var _82=new dojo.io.IOEvent("backbutton",null,_73,null,null,null);
_73.dispatchEvent(_82);
if(_73.onbackbutton){
_73.onbackbutton(_82);
}
};
this.forwardButton=function(){
var _83=new dojo.io.IOEvent("forwardbutton",null,_73,null,null,null);
_73.dispatchEvent(_83);
if(_73.onforwardbutton){
_73.onforwardbutton(_83);
}
};
};
dojo.io.Request.prototype.addEventListener=function(_84,_85){
if(!this.events_[_84]){
this.events_[_84]=[];
}
for(var i=0;i<this.events_[_84].length;i++){
if(this.events_[_84][i]==_85){
return;
}
}
this.events_[_84].push(_85);
};
dojo.io.Request.prototype.removeEventListener=function(_86,_87){
if(!this.events_[_86]){
return;
}
for(var i=0;i<this.events_[_86].length;i++){
if(this.events_[_86][i]==_87){
this.events_[_86].splice(i,1);
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
dojo.io.IOEvent=function(_88,_89,_90,_91,_92,_93){
this.type=_88;
this.data=_89;
this.request=_90;
this.errorMessage=_91;
this.errorUrl=_92;
this.errorCode=_93;
};
dojo.io.IOEvent.UNKOWN_ERROR=0;
dojo.io.IOEvent.IO_ERROR=1;
dojo.io.IOEvent.PARSE_ERROR=2;
dojo.io.Error=function(msg,_95,num){
this.message=msg;
this.type=_95||"unknown";
this.number=num||0;
};
dojo.io.transports.addTransport=function(_97){
this.push(_97);
this[_97]=dojo.io[_97];
};
dojo.io.bind=function(_98){
if(!_98["mimetype"]){
_98.mimetype="text/plain";
}
if(!_98["method"]&&!_98["formNode"]){
_98.method="get";
}else{
if(_98["formNode"]){
_98.method=_98["formNode"].method||"get";
}
}
if(_98["handler"]){
_98.handle=_98.handler;
}
if(!_98["handle"]){
_98.handle=function(){
};
}
if(_98["loaded"]){
_98.load=_98.loaded;
}
if(_98["changeUrl"]){
_98.changeURL=_98.changeUrl;
}
for(var x=0;x<this.hdlrFuncNames.length;x++){
var fn=this.hdlrFuncNames[x];
if(typeof _98[fn]=="function"){
continue;
}
if(typeof _98.handler=="object"){
if(typeof _98.handler[fn]=="function"){
_98[fn]=_98.handler[fn]||_98.handler["handle"]||function(){
};
}
}else{
if(typeof _98["handler"]=="function"){
_98[fn]=_98.handler;
}else{
if(typeof _98["handle"]=="function"){
_98[fn]=_98.handle;
}
}
}
}
var _100="";
if(_98["transport"]){
_100=_98["transport"];
if(!this[_100]){
return false;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_98))){
_100=tmp;
}
}
if(_100==""){
return false;
}
}
this[_100].bind(_98);
return true;
};
dojo.io.argsFromMap=function(map){
var _102=new Object();
var _103="";
for(var x in map){
if(!_102[x]){
_103+=encodeURIComponent(x)+"="+encodeURIComponent(map[x])+"&";
}
}
return _103;
};
dojo.hostenv.startPackage("dojo.alg.Alg");
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
dojo.alg.forEach=function(arr,_110){
for(var i=0;i<arr.length;i++){
_110(arr[i]);
}
};
dojo.alg.for_each=dojo.alg.forEach;
dojo.alg.map=function(arr,obj,_111){
for(var i=0;i<arr.length;++i){
_111.call(obj,arr[i]);
}
};
dojo.alg.for_each_call=dojo.alg.map;
dojo.hostenv.loadModule("dojo.alg.Alg",false,true);
dojo.hostenv.startPackage("dojo.io.BrowserIO");
dojo.hostenv.loadModule("dojo.io.IO");
dojo.hostenv.loadModule("dojo.alg.*");
dojo.io.checkChildrenForFile=function(node){
var _113=false;
for(var x=0;x<node.childNodes.length;x++){
if(node.nodeType==1){
if(node.nodeName.toLowerCase()=="input"){
if(node.getAttribute("type")=="file"){
return true;
}
}
if(node.childNodes.length){
for(var x=0;x<node.childNodes.length;x++){
if(dojo.io.checkChildrenForFile(node.childNodes.item(x))){
return true;
}
}
}
}
}
return false;
};
dojo.io.formHasFile=function(_114){
return dojo.io.checkChildrenForFile(_114);
};
dojo.io.buildFormGetString=function(_115){
var ec=encodeURIComponent;
var tvar="";
var ctyp=_115.nodeName?_115.nodeName.toLowerCase():"";
var etyp=_115.type?_115.type.toLowerCase():"";
if(((ctyp=="input")&&(etyp!="radio")&&(etyp!="checkbox"))||(ctyp=="select")||(ctyp=="textarea")){
if((ctyp=="input")&&(etyp=="submit")){
}else{
if(!((ctyp=="select")&&(_115.getAttribute("multiple")))){
tvar=ec(_115.getAttribute("name"))+"="+ec(_115.value)+"&";
}else{
var tn=ec(_115.getAttribute("name"));
var _121=_115.getElementsByTagName("option");
for(var x=0;x<_121.length;x++){
if(_121[x].selected){
tvar+=tn+"="+ec(_121[x].value)+"&";
}
}
}
}
}else{
if(ctyp=="input"){
if(_115.checked){
tvar=ec(_115.getAttribute("name"))+"="+ec(_115.value)+"&";
}
}
}
if(_115.hasChildNodes()){
for(var _122=(_115.childNodes.length-1);_122>=0;_122--){
tvar+=dojo.io.buildFormGetString(_115.childNodes.item(_122));
}
}
return tvar;
};
dojo.io.setIFrameSrc=function(_123,src,_124){
try{
var r=dojo.render.html;
if(!_124){
if(r.safari){
_123.location=src;
}else{
frames[_123.name].location=src;
}
}else{
var idoc=(r.moz)?_123.contentWindow:_123;
idoc.location.replace(src);
dj_debug(_123.contentWindow.location);
}
}
catch(e){
dj_debug("setIFrameSrc: "+e);
}
};
dojo.io.createIFrame=function(_127){
if(window[_127]){
return window[_127];
}
if(window.frames[_127]){
return window.frames[_127];
}
var r=dojo.render.html;
var _128=null;
_128=document.createElement((((r.ie)&&(r.win))?"<iframe name="+_127+">":"iframe"));
with(_128){
name=_127;
setAttribute("name",_127);
id=_127;
}
window[_127]=_128;
document.body.appendChild(_128);
with(_128.style){
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
dojo.io.setIFrameSrc(_128,dojo.hostenv.getBaseScriptUri()+"/blank.html",true);
return _128;
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
var _129=this;
this.initialHref=window.location.href;
this.initialHash=window.location.hash;
this.moveForward=false;
var _130={};
this.useCache=false;
this.historyStack=[];
this.forwardStack=[];
this.historyIframe=null;
this.bookmarkAnchor=null;
this.locationTimer=null;
function getCacheKey(url,_131,_132){
return url+"|"+_131+"|"+_132.toLowerCase();
}
function addToCache(url,_133,_134,http){
_130[getCacheKey(url,_133,_134)]=http;
}
function getFromCache(url,_136,_137){
return _130[getCacheKey(url,_136,_137)];
}
this.clearCache=function(){
_130={};
};
function doLoad(_138,http,url,_139,_140){
if(http.status==200){
var ret;
if(_138.mimetype=="text/javascript"){
ret=dj_eval(http.responseText);
}else{
if(_138.mimetype=="text/xml"){
ret=http.responseXML;
if(!ret||typeof ret=="string"){
ret=dojo.xml.domUtil.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
if(_140){
addToCache(url,_139,_138.method,http);
}
if(typeof _138.load=="function"){
_138.load("load",ret,http);
}else{
if(typeof _138.handle=="function"){
_138.handle("load",ret,http);
}
}
}else{
var _142=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
if(typeof _138.error=="function"){
_138.error("error",_142);
}else{
if(typeof _138.handle=="function"){
_138.handle("error",_142,_142);
}
}
}
}
this.addToHistory=function(args){
var _144=args["back"]||args["backButton"]||args["handle"];
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
var url=dojo.hostenv.getBaseScriptUri()+"blank.html?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
}
if(args["changeURL"]){
hash="#"+((args["changeURL"]!==true)?args["changeURL"]:(new Date()).getTime());
setTimeout("window.location.href = '"+hash+"';",1);
this.bookmarkAnchor.href=hash;
if(dojo.render.html.ie){
var _146=_144;
var lh=null;
var hsl=this.historyStack.length-1;
if(hsl>=0){
while(!this.historyStack[hsl]["urlHash"]){
hsl--;
}
lh=this.historyStack[hsl]["urlHash"];
}
if(lh){
_144=function(){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+lh+"';",1);
}
_146();
};
}
this.forwardStack=[];
var _149=args["forward"]||args["forwardbutton"];
var tfw=function(){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_149){
_149();
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
this.historyStack.push({"url":url,"callback":_144,"kwArgs":args,"urlHash":hash});
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
this.iframeLoaded=function(evt,_151){
var isp=_151.href.split("?");
if(isp.length<2){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
var _153=isp[1];
if(this.moveForward){
this.moveForward=false;
return;
}
var last=this.historyStack.pop();
if(!last){
if(this.forwardStack.length>0){
var next=this.forwardStack[this.forwardStack.length-1];
if(_153==next.url.split("?")[1]){
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
this.canHandle=function(_156){
return dojo.alg.inArray(_156["mimetype"],["text/plain","text/html","text/xml","text/javascript"])&&dojo.alg.inArray(_156["method"],["post","get"])&&!(_156["formNode"]&&dojo.io.formHasFile(_156["formNode"]));
};
this.bind=function(_157){
if(!_157["url"]){
if(!_157["formNode"]&&(_157["backButton"]||_157["back"]||_157["changeURL"]||_157["watchForURL"])&&(!window["djConfig"]&&!window["djConfig"]["preventBackButtonFix"])){
this.addToHistory(_157);
return true;
}
}
var url=_157.url;
var _158="";
if(_157["formNode"]){
var ta=_157.formNode.getAttribute("action");
if((ta)&&(!_157["url"])){
url=ta;
}
var tp=_157.formNode.getAttribute("method");
if((tp)&&(!_157["method"])){
_157.method=tp;
}
_158+=dojo.io.buildFormGetString(_157.formNode);
}
if(!_157["method"]){
_157.method="get";
}
if(_157["content"]){
_158+=dojo.io.argsFromMap(_157.content);
}
if(_157["postContent"]&&_157.method.toLowerCase()=="post"){
_158=_157.postContent;
}
if(_157["backButton"]||_157["back"]||_157["changeURL"]){
this.addToHistory(_157);
}
var _161=_157["sync"]?false:true;
var _162=_157["useCache"]==true||(this.useCache==true&&_157["useCache"]!=false);
if(_162){
var _163=getFromCache(url,_158,_157.method);
if(_163){
doLoad(_157,_163,url,_158,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject();
var _164=false;
if(_161){
http.onreadystatechange=function(){
if((4==http.readyState)&&(http.status)){
if(_164){
return;
}
_164=true;
doLoad(_157,http,url,_158,_162);
}
};
}
if(_157.method.toLowerCase()=="post"){
http.open("POST",url,_161);
http.setRequestHeader("Content-Type",_157["contentType"]||"application/x-www-form-urlencoded");
http.send(_158);
}else{
http.open("GET",url+((_158!="")?"?"+_158:""),_161);
http.send(null);
}
if(!_161){
doLoad(_157,http,url,_158,_162);
}
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};

