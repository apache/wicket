/*
Copyright (c) 2008, Yahoo! Inc. All rights reserved.
Code licensed under the BSD License:
http://developer.yahoo.net/yui/license.txt
version: 3.0.0pr1
*/
YUI.add('anim-node-plugin', function(Y) {

/**
 *  Binds an Anim instance to a Node instance
 * @module anim
 * @submodule anim-node-plugin
 */

Y.namespace('Plugin');
Y.Plugin.NodeFX = function(config) {
    config.node = config.owner;
    Y.Plugin.NodeFX.superclass.constructor.apply(this, arguments);
};

Y.Plugin.NodeFX.NAME = "nodefxplugin";
Y.Plugin.NodeFX.NS = "fx";

Y.extend(Y.Plugin.NodeFX, Y.Anim);



}, '3.0.0pr1' ,{requires:['anim-base', 'node-base']});
