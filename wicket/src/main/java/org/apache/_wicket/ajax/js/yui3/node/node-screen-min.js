/*
Copyright (c) 2008, Yahoo! Inc. All rights reserved.
Code licensed under the BSD License:
http://developer.yahoo.net/yui/license.txt
version: 3.0.0pr1
*/
YUI.add("node-screen",function(B){B.each(["winWidth","winHeight","docWidth","docHeight","docScrollX","docScrollY"],function(D,E){B.Node.getters(D,B.Node.wrapDOMMethod(D));});B.Node.addDOMMethods(["getXY","setXY","getX","setX","getY","setY"]);var A=["region","viewportRegion"],C=B.Node.getDOMNode;B.each(A,function(D,E){B.Node.getters(D,B.Node.wrapDOMMethod(D));});B.Node.addDOMMethods(["inViewportRegion"]);B.Node.methods({intersect:function(E,D,F){if(D instanceof B.Node){D=C(D);}return B.DOM.intersect(C(E),D,F);},inRegion:function(E,D,F,G){if(D instanceof B.Node){D=C(D);}return B.DOM.inRegion(C(E),D,F,G);}});},"3.0.0pr1",{requires:["dom-screen"]});