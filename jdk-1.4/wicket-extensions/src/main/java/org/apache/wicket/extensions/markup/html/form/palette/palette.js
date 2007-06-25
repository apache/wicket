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
	function paletteResolve( id) {
		return document.getElementById(id);
	}

	function paletteChoicesOnFocus(choicesId, selectionId, recorderId) {
		paletteClearSelectionHelper(paletteResolve(selectionId));
	}
	
	function paletteSelectionOnFocus(choicesId, selectionId, recorderId) {
		paletteClearSelectionHelper(paletteResolve(choicesId));
	}
		
	
	function paletteAdd(choicesId, selectionId, recorderId) {
		var choices=paletteResolve(choicesId);
		var selection=paletteResolve(selectionId);

		if (paletteMoveHelper(choices, selection)) {
			var recorder=paletteResolve(recorderId);
			paletteUpdateRecorder(selection, recorder);
		}
	}
	
	function paletteRemove(choicesId, selectionId, recorderId) {
		var choices=paletteResolve(choicesId);
		var selection=paletteResolve(selectionId);

		if (paletteMoveHelper(selection, choices)) {
			var recorder=paletteResolve(recorderId);
			paletteUpdateRecorder(selection, recorder);
		}
	}

	function paletteMoveHelper(source, dest) {
		var dirty=false;
		for (var i=0;i<source.options.length;i++) {
			if (source.options[i].selected) {	
				dest.appendChild(source.options[i]);
				i--;
				dirty=true;
			}
		}
		return dirty;
	}
	
	function paletteMoveUp(choicesId, selectionId, recorderId) {
		var selection=paletteResolve(selectionId);

		if (paletteMoveUpHelper(selection)) {
			var recorder=paletteResolve(recorderId);
			paletteUpdateRecorder(selection, recorder);
		}
	}
	
	function paletteMoveUpHelper(box) {
		var dirty=false;
		for (var i=0;i<box.options.length;i++) {
			if (box.options[i].selected && i>0) {
				if(!box.options[i-1].selected) {
					box.insertBefore(box.options[i],box.options[i-1]);
					dirty=true;
				}
			}
		}
		return dirty;
	}
	
	function paletteMoveDown(choicesId, selectionId, recorderId) {
		var selection=paletteResolve(selectionId);

		if (paletteMoveDownHelper(selection)) {
			var recorder=paletteResolve(recorderId);
			paletteUpdateRecorder(selection, recorder);
		}
	}

	function paletteMoveDownHelper(box) {
		var dirty=false;
		for (var i=box.options.length-1;i>=0;i--) {
			if (box.options[i].selected && i<box.options.length-1) {
				if(!box.options[i+1].selected) {
					box.insertBefore(box.options[i+1],box.options[i]);
					dirty=true;
				}
			}
		}
		return dirty;
	}	
	
	function paletteUpdateRecorder(selection, recorder) {
		recorder.value="";
		for (var i=0;i<selection.options.length;i++) {
			recorder.value=recorder.value+selection.options[i].value;
			if (i+1<selection.options.length) {
				recorder.value=recorder.value+",";
			}
		}
		
		if (recorder.onchange!=null) { recorder.onchange(); }
	}
	
	function paletteClearSelectionHelper(box) {
		for (var i=0;i<box.options.length;i++) {
			box.options[i].selected=false;
		}	
	}
