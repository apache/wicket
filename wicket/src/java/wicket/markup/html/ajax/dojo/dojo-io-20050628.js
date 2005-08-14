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
function dj_debug(){
var _8=arguments;
if(dj_undef("println",dojo.hostenv)){
dj_throw("dj_debug not available (yet?)");
}
if(!dojo.hostenv.is_debug_){
return;
}
var _9=dj_global["jum"];
var s=_9?"":"DEBUG: ";
for(var i=0;i<_8.length;++i){
s+=_8[i];
}
if(_9){
jum.debug(s);
}else{
dojo.hostenv.println(s);
}
}
function dj_throw(_11){
var he=dojo.hostenv;
if(dj_undef("hostenv",dojo)&&dj_undef("println",dojo)){
dojo.hostenv.println("FATAL: "+_11);
}
throw Error(_11);
}
function dj_error_to_string(_13){
return ((!dj_undef("message",_13))?_13.message:(dj_undef("description",_13)?_13:_13.description));
}
function dj_rethrow(_14,_15){
var _16=dj_error_to_string(_15);
dj_throw(_14+": "+_16);
}
function dj_eval(s){
return dj_global.eval?dj_global.eval(s):eval(s);
}
function dj_unimplemented(_17,_18){
var _19="'"+_17+"' not implemented";
if((typeof _18!="undefined")&&(_18)){
_19+=" "+_18;
}
dj_throw(_19);
}
function dj_inherits(_20,_21){
if(typeof _21!="function"){
dj_throw("superclass: "+_21+" borken");
}
_20.prototype=new _21();
_20.prototype.constructor=_20;
_20["super"]=_21;
}
dojo.render={name:"",ver:0,os:{win:false,linux:false,osx:false},html:{capable:false,support:{builtin:false,plugin:false},ie:false,opera:false,khtml:false,safari:false,moz:false},svg:{capable:false,support:{builtin:false,plugin:false},corel:false,adobe:false,batik:false},swf:{capable:false,support:{builtin:false,plugin:false},mm:false},swt:{capable:false,support:{builtin:false,plugin:false},ibm:false}};
dojo.hostenv=(function(){
var djc=djConfig;
function _def(obj,_24,def){
return (dj_undef(_24,obj)?def:obj[_24]);
}
return {is_debug_:_def(djc,"isDebug",false),base_script_uri_:_def(djc,"baseScriptUri",undefined),base_relative_path_:_def(djc,"baseRelativePath",""),library_script_uri_:_def(djc,"libraryScriptUri",""),auto_build_widgets_:_def(djc,"parseWidgets",true),name_:"(unset)",version_:"(unset)",pkgFileName:"__package__",loading_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_26,_27){
this.modulePrefixes_[_26]={name:_26,value:_27};
},getModulePrefix:function(_28){
var mp=this.modulePrefixes_;
if((mp[_28])&&(mp[_28]["name"])){
return mp[_28].value;
}
return _28;
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
var _31=uri.lastIndexOf("/");
this.base_script_uri_=this.base_relative_path_;
return this.base_script_uri_;
};
dojo.hostenv.setBaseScriptUri=function(uri){
this.base_script_uri_=uri;
};
dojo.hostenv.loadPath=function(_32,_33,cb){
if(!_32){
dj_throw("Missing relpath argument");
}
if((_32.charAt(0)=="/")||(_32.match(/^\w+:/))){
dj_throw("relpath '"+_32+"'; must be relative");
}
var uri=this.getBaseScriptUri()+_32;
try{
return ((!_33)?this.loadUri(uri):this.loadUriAndCheck(uri,_33));
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
var _35=this.getText(uri,null,true);
if(_35==null){
return 0;
}
var _36=dj_eval(_35);
return 1;
};
dojo.hostenv.getDepsForEval=function(_37){
if(!_37){
_37="";
}
var _38=[];
var tmp=_37.match(/dojo.hostenv.loadModule\(.*?\)/mg);
if(tmp){
for(var x=0;x<tmp.length;x++){
_38.push(tmp[x]);
}
}
tmp=_37.match(/dojo.hostenv.require\(.*?\)/mg);
if(tmp){
for(var x=0;x<tmp.length;x++){
_38.push(tmp[x]);
}
}
tmp=_37.match(/dojo.hostenv.conditionalLoadModule\([\w\W]*?\)/gm);
if(tmp){
for(var x=0;x<tmp.length;x++){
_38.push(tmp[x]);
}
}
return _38;
};
dojo.hostenv.loadUriAndCheck=function(uri,_41,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dj_debug("failed loading ",uri," with error: ",e);
}
return ((ok)&&(this.findModule(_41,false)))?true:false;
};
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
dojo.hostenv.loadModule=function(_44,_45,_46){
var _47=this.findModule(_44,false);
if(_47){
return _47;
}
if(!dj_undef(_44,this.loading_modules_)){
dj_debug("recursive attempt to load module '"+_44+"'");
}else{
this.addedToLoadingCount.push(_44);
}
this.loading_modules_[_44]=1;
var _48=_44.replace(/\./g,"/")+".js";
var _49=_44.split(".");
var _50=_44.split(".");
_49[0]=this.getModulePrefix(_49[0]);
var _51=_49.pop();
_49.push(_51);
if(_51=="*"){
_44=(_50.slice(0,-1)).join(".");
var _47=this.findModule(_44,0);
if(_47){
return _47;
}
while(_49.length){
_49.pop();
_49.push("__package__");
_48=_49.join("/")+".js";
if(_48.charAt(0)=="/"){
_48=_48.slice(1);
}
ok=this.loadPath(_48,((!_46)?_44:null));
if(ok){
break;
}
_49.pop();
}
}else{
_48=_49.join("/")+".js";
_44=_50.join(".");
var ok=this.loadPath(_48,((!_46)?_44:null));
if((!ok)&&(!_45)){
_49.pop();
while(_49.length){
_48=_49.join("/")+".js";
ok=this.loadPath(_48,((!_46)?_44:null));
if(ok){
break;
}
_49.pop();
_48=_49.join("/")+"/__package__.js";
if(_48.charAt(0)=="/"){
_48=_48.slice(1);
}
ok=this.loadPath(_48,((!_46)?_44:null));
if(ok){
break;
}
}
}
if((!ok)&&(!_46)){
dj_throw("Could not load '"+_44+"'; last tried '"+_48+"'");
}
}
if(!_46){
_47=this.findModule(_44,false);
if(!_47){
dj_throw("symbol '"+_44+"' is not defined after loading '"+_48+"'");
}
}
return _47;
};
function dj_load(_52,_53){
return dojo.hostenv.loadModule(_52,_53);
}
dojo.hostenv.startPackage=function(_54){
var _55=_54.split(/\./);
if(_55[_55.length-1]=="*"){
_55.pop();
}
return dj_eval_object_path(_55.join("."),true);
};
dojo.hostenv.findModule=function(_56,_57){
if(!dj_undef(_56,this.modules_)){
return this.modules_[_56];
}
var _58=dj_eval_object_path(_56);
if((typeof _58!=="undefined")&&(_58)){
return this.modules_[_56]=_58;
}
if(_57){
dj_throw("no loaded module named '"+_56+"'");
}
return null;
};
if(typeof window=="undefined"){
dj_throw("no window object");
}
(function(){
if((dojo.hostenv["base_script_uri_"]==""||dojo.hostenv["base_relative_path_"]=="")&&document&&document.getElementsByTagName){
var _59=document.getElementsByTagName("script");
var _60=/__package__\.js$/i;
for(var i=0;i<_59.length;i++){
var src=_59[i].getAttribute("src");
if(_60.test(src)){
var _62=src.replace(_60,"");
if(dojo.hostenv["base_script_uri_"]==""){
dojo.hostenv["base_script_uri_"]=_62;
}
if(dojo.hostenv["base_relative_path_"]==""){
dojo.hostenv["base_relative_path_"]=_62;
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
var _63=null;
var _64=null;
try{
_63=new XMLHttpRequest();
}
catch(e){
}
if(!_63){
for(var i=0;i<3;++i){
var _65=DJ_XMLHTTP_PROGIDS[i];
try{
_63=new ActiveXObject(_65);
}
catch(e){
_64=e;
}
if(_63){
DJ_XMLHTTP_PROGIDS=[_65];
break;
}else{
}
}
}
if((_64)&&(!_63)){
dj_rethrow("Could not create a new ActiveXObject using any of the progids "+DJ_XMLHTTP_PROGIDS.join(", "),_64);
}else{
if(!_63){
return dj_throw("No XMLHTTP implementation available, for uri "+uri);
}
}
return _63;
};
dojo.hostenv.getText=function(uri,_66,_67){
var _68=this.getXmlhttpObject();
if(_66){
_68.onreadystatechange=function(){
if((4==_68.readyState)&&(_68["status"])){
if(_68.status==200){
dj_debug("LOADED URI: "+uri);
_66(_68.responseText);
}
}
};
}
_68.open("GET",uri,_66?true:false);
_68.send(null);
if(_66){
return null;
}
return _68.responseText;
};
function dj_last_script_src(){
var _69=window.document.getElementsByTagName("script");
if(_69.length<1){
dj_throw("No script elements in window.document, so can't figure out my script src");
}
var _70=_69[_69.length-1];
var src=_70.src;
if(!src){
dj_throw("Last script element (out of "+_69.length+") has no src");
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
var _74=new dojo.xml.Parse();
var _75=_74.parseElement(document.body,null,true);
dojo.webui.widgets.getParser().createComponents(_75);
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
dojo.hostenv.conditionalLoadModule=function(_76){
var _77=_76["common"]||[];
var _78=(_76[dojo.hostenv.name_])?_77.concat(_76[dojo.hostenv.name_]||[]):_77.concat(_76["default"]||[]);
for(var x=0;x<_78.length;x++){
var _79=_78[x];
if(_79.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_79);
}else{
dojo.hostenv.loadModule(_79);
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
dojo.io.Request=function(url,mt,_82,_83){
this.url=url;
this.mimetype=mt;
this.transport=_82;
this.changeUrl=_83;
this.formNode=null;
this.events_={};
var _84=this;
this.error=function(_85,_86){
switch(_85){
case "io":
var _87=dojo.io.IOEvent.IO_ERROR;
var _88="IOError: error during IO";
break;
case "parse":
var _87=dojo.io.IOEvent.PARSE_ERROR;
var _88="IOError: error during parsing";
default:
var _87=dojo.io.IOEvent.UNKOWN_ERROR;
var _88="IOError: cause unkown";
}
var _89=new dojo.io.IOEvent("error",null,_84,_88,this.url,_87);
_84.dispatchEvent(_89);
if(_84.onerror){
_84.onerror(_88,_84.url,_89);
}
};
this.load=function(_90,_91,evt){
var _92=new dojo.io.IOEvent("load",_91,_84,null,null,null);
_84.dispatchEvent(_92);
if(_84.onload){
_84.onload(_92);
}
};
this.backButton=function(){
var _93=new dojo.io.IOEvent("backbutton",null,_84,null,null,null);
_84.dispatchEvent(_93);
if(_84.onbackbutton){
_84.onbackbutton(_93);
}
};
this.forwardButton=function(){
var _94=new dojo.io.IOEvent("forwardbutton",null,_84,null,null,null);
_84.dispatchEvent(_94);
if(_84.onforwardbutton){
_84.onforwardbutton(_94);
}
};
};
dojo.io.Request.prototype.addEventListener=function(_95,_96){
if(!this.events_[_95]){
this.events_[_95]=[];
}
for(var i=0;i<this.events_[_95].length;i++){
if(this.events_[_95][i]==_96){
return;
}
}
this.events_[_95].push(_96);
};
dojo.io.Request.prototype.removeEventListener=function(_97,_98){
if(!this.events_[_97]){
return;
}
for(var i=0;i<this.events_[_97].length;i++){
if(this.events_[_97][i]==_98){
this.events_[_97].splice(i,1);
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
dojo.io.IOEvent=function(_99,data,_101,_102,_103,_104){
this.type=_99;
this.data=data;
this.request=_101;
this.errorMessage=_102;
this.errorUrl=_103;
this.errorCode=_104;
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
dojo.io.bind=function(_109){
if(!_109["mimetype"]){
_109.mimetype="text/plain";
}
if(!_109["method"]&&!_109["formNode"]){
_109.method="get";
}else{
if(_109["formNode"]){
_109.method=_109["formNode"].method||"get";
}
}
if(_109["handler"]){
_109.handle=_109.handler;
}
if(!_109["handle"]){
_109.handle=function(){
};
}
if(_109["loaded"]){
_109.load=_109.loaded;
}
if(_109["changeUrl"]){
_109.changeURL=_109.changeUrl;
}
for(var x=0;x<this.hdlrFuncNames.length;x++){
var fn=this.hdlrFuncNames[x];
if(typeof _109[fn]=="function"){
continue;
}
if(typeof _109.handler=="object"){
if(typeof _109.handler[fn]=="function"){
_109[fn]=_109.handler[fn]||_109.handler["handle"]||function(){
};
}
}else{
if(typeof _109["handler"]=="function"){
_109[fn]=_109.handler;
}else{
if(typeof _109["handle"]=="function"){
_109[fn]=_109.handle;
}
}
}
}
var _111="";
if(_109["transport"]){
_111=_109["transport"];
if(!this[_111]){
return false;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_109))){
_111=tmp;
}
}
if(_111==""){
return false;
}
}
this[_111].bind(_109);
return true;
};
dojo.io.argsFromMap=function(map){
var _113=new Object();
var _114="";
for(var x in map){
if(!_113[x]){
_114+=encodeURIComponent(x)+"="+encodeURIComponent(map[x])+"&";
}
}
return _114;
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
dojo.alg.forEach=function(arr,_120){
for(var i=0;i<arr.length;i++){
_120(arr[i]);
}
};
dojo.alg.for_each=dojo.alg.forEach;
dojo.alg.map=function(arr,obj,_121){
for(var i=0;i<arr.length;++i){
_121.call(obj,arr[i]);
}
};
dojo.alg.for_each_call=dojo.alg.map;
dojo.hostenv.loadModule("dojo.alg.Alg",false,true);
dojo.hostenv.startPackage("dojo.io.BrowserIO");
dojo.hostenv.loadModule("dojo.io.IO");
dojo.hostenv.loadModule("dojo.alg.*");
dojo.io.checkChildrenForFile=function(node){
var _123=false;
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
dojo.io.formHasFile=function(_124){
return dojo.io.checkChildrenForFile(_124);
};
dojo.io.buildFormGetString=function(_125){
var ec=encodeURIComponent;
var tvar="";
var ctyp=_125.nodeName?_125.nodeName.toLowerCase():"";
var etyp=_125.type?_125.type.toLowerCase():"";
if(((ctyp=="input")&&(etyp!="radio")&&(etyp!="checkbox"))||(ctyp=="select")||(ctyp=="textarea")){
if((ctyp=="input")&&(etyp=="submit")){
}else{
if(!((ctyp=="select")&&(_125.getAttribute("multiple")))){
tvar=ec(_125.getAttribute("name"))+"="+ec(_125.value)+"&";
}else{
var tn=ec(_125.getAttribute("name"));
var _131=_125.getElementsByTagName("option");
for(var x=0;x<_131.length;x++){
if(_131[x].selected){
tvar+=tn+"="+ec(_131[x].value)+"&";
}
}
}
}
}else{
if(ctyp=="input"){
if(_125.checked){
tvar=ec(_125.getAttribute("name"))+"="+ec(_125.value)+"&";
}
}
}
if(_125.hasChildNodes()){
for(var _132=(_125.childNodes.length-1);_132>=0;_132--){
tvar+=dojo.io.buildFormGetString(_125.childNodes.item(_132));
}
}
return tvar;
};
dojo.io.setIFrameSrc=function(_133,src,_134){
try{
var r=dojo.render.html;
if(!_134){
if(r.safari){
_133.location=src;
}else{
frames[_133.name].location=src;
}
}else{
var idoc=(r.moz)?_133.contentWindow:_133;
idoc.location.replace(src);
dj_debug(_133.contentWindow.location);
}
}
catch(e){
dj_debug("setIFrameSrc: "+e);
}
};
dojo.io.createIFrame=function(_137){
if(window[_137]){
return window[_137];
}
if(window.frames[_137]){
return window.frames[_137];
}
var r=dojo.render.html;
var _138=null;
_138=document.createElement((((r.ie)&&(r.win))?"<iframe name="+_137+">":"iframe"));
with(_138){
name=_137;
setAttribute("name",_137);
id=_137;
}
window[_137]=_138;
document.body.appendChild(_138);
with(_138.style){
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
dojo.io.setIFrameSrc(_138,dojo.hostenv.getBaseScriptUri()+"/blank.html",true);
return _138;
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
var _139=this;
this.initialHref=window.location.href;
this.initialHash=window.location.hash;
this.moveForward=false;
var _140={};
this.useCache=false;
this.historyStack=[];
this.forwardStack=[];
this.historyIframe=null;
this.bookmarkAnchor=null;
this.locationTimer=null;
function getCacheKey(url,_141,_142){
return url+"|"+_141+"|"+_142.toLowerCase();
}
function addToCache(url,_143,_144,http){
_140[getCacheKey(url,_143,_144)]=http;
}
function getFromCache(url,_146,_147){
return _140[getCacheKey(url,_146,_147)];
}
this.clearCache=function(){
_140={};
};
function doLoad(_148,http,url,_149,_150){
if(http.status==200){
var ret;
if(_148.mimetype=="text/javascript"){
ret=dj_eval(http.responseText);
}else{
if(_148.mimetype=="text/xml"){
ret=http.responseXML;
if(!ret||typeof ret=="string"){
ret=dojo.xml.domUtil.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
if(_150){
addToCache(url,_149,_148.method,http);
}
if(typeof _148.load=="function"){
_148.load("load",ret,http);
}else{
if(typeof _148.handle=="function"){
_148.handle("load",ret,http);
}
}
}else{
var _152=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
if(typeof _148.error=="function"){
_148.error("error",_152);
}else{
if(typeof _148.handle=="function"){
_148.handle("error",_152,_152);
}
}
}
}
this.addToHistory=function(args){
var _154=args["back"]||args["backButton"]||args["handle"];
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
var _156=_154;
var lh=null;
var hsl=this.historyStack.length-1;
if(hsl>=0){
while(!this.historyStack[hsl]["urlHash"]){
hsl--;
}
lh=this.historyStack[hsl]["urlHash"];
}
if(lh){
_154=function(){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+lh+"';",1);
}
_156();
};
}
this.forwardStack=[];
var _159=args["forward"]||args["forwardbutton"];
var tfw=function(){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_159){
_159();
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
this.historyStack.push({"url":url,"callback":_154,"kwArgs":args,"urlHash":hash});
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
this.iframeLoaded=function(evt,_161){
var isp=_161.href.split("?");
if(isp.length<2){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
var _163=isp[1];
if(this.moveForward){
this.moveForward=false;
return;
}
var last=this.historyStack.pop();
if(!last){
if(this.forwardStack.length>0){
var next=this.forwardStack[this.forwardStack.length-1];
if(_163==next.url.split("?")[1]){
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
this.canHandle=function(_166){
return dojo.alg.inArray(_166["mimetype"],["text/plain","text/html","text/xml","text/javascript"])&&dojo.alg.inArray(_166["method"].toLowerCase(),["post","get"])&&!(_166["formNode"]&&dojo.io.formHasFile(_166["formNode"]));
};
this.bind=function(_167){
if(!_167["url"]){
if(!_167["formNode"]&&(_167["backButton"]||_167["back"]||_167["changeURL"]||_167["watchForURL"])&&(!window["djConfig"]&&!window["djConfig"]["preventBackButtonFix"])){
this.addToHistory(_167);
return true;
}
}
var url=_167.url;
var _168="";
if(_167["formNode"]){
var ta=_167.formNode.getAttribute("action");
if((ta)&&(!_167["url"])){
url=ta;
}
var tp=_167.formNode.getAttribute("method");
if((tp)&&(!_167["method"])){
_167.method=tp;
}
_168+=dojo.io.buildFormGetString(_167.formNode);
}
if(!_167["method"]){
_167.method="get";
}
if(_167["content"]){
_168+=dojo.io.argsFromMap(_167.content);
}
if(_167["postContent"]&&_167.method.toLowerCase()=="post"){
_168=_167.postContent;
}
if(_167["backButton"]||_167["back"]||_167["changeURL"]){
this.addToHistory(_167);
}
var _171=_167["sync"]?false:true;
var _172=_167["useCache"]==true||(this.useCache==true&&_167["useCache"]!=false);
if(_172){
var _173=getFromCache(url,_168,_167.method);
if(_173){
doLoad(_167,_173,url,_168,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject();
var _174=false;
if(_171){
http.onreadystatechange=function(){
if((4==http.readyState)&&(http.status)){
if(_174){
return;
}
_174=true;
doLoad(_167,http,url,_168,_172);
}
};
}
if(_167.method.toLowerCase()=="post"){
http.open("POST",url,_171);
http.setRequestHeader("Content-Type",_167["contentType"]||"application/x-www-form-urlencoded");
http.send(_168);
}else{
http.open("GET",url+((_168!="")?"?"+_168:""),_171);
http.send(null);
}
if(!_171){
doLoad(_167,http,url,_168,_172);
}
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};

