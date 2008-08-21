/*
Copyright (c) 2008, Yahoo! Inc. All rights reserved.
Code licensed under the BSD License:
http://developer.yahoo.net/yui/license.txt
version: 3.0.0pr1
*/
YUI.add("anim-node-plugin",function(A){A.namespace("Plugin");A.Plugin.NodeFX=function(B){B.node=B.owner;A.Plugin.NodeFX.superclass.constructor.apply(this,arguments);};A.Plugin.NodeFX.NAME="nodefxplugin";A.Plugin.NodeFX.NS="fx";A.extend(A.Plugin.NodeFX,A.Anim);},"3.0.0pr1",{requires:["anim-base","node-base"]});