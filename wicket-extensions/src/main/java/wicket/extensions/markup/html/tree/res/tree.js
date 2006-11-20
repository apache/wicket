/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
if (typeof(Wicket) == "undefined")
	Wicket = { };

Wicket.Tree = { };

Wicket.Tree.removeNodes = function(prefix, nodeList) {
	for(var i = 0; i < nodeList.length; i++ ) {
		var e = document.getElementById(prefix + nodeList[i]);
		if (e != null) {
			e.parentNode.removeChild(e);
		} else {
			// while developing alert a warning
			alert("Can't find node with id " + prefix + nodeList[i] + ". This shouldn't happen - possible bug in tree?");
		}
	}
}

Wicket.Tree.createElement = function(elementId, afterId) {
	var after = document.getElementById(afterId);
	var newNode = document.createElement("script");
	newNode.setAttribute("id", elementId);

	var p = after.parentNode;

	for (var i = 0; i < p.childNodes.length; ++i) {
		if (after == p.childNodes[i])
			break;
	}
	if (i == p.childNodes.length - 1) {
		p.appendChild(newNode);		
	} else {
		p.insertBefore(newNode, p.childNodes[i+1]);
	}
}
