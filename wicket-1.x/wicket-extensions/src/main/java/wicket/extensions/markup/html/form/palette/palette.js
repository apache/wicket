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
				var option=new Option(source.options[i].text, source.options[i].value);
				var destIndex=dest.options.length;
				dest.options[destIndex]=option;
				dest.options[destIndex].selected=true;
				source.options[i]=null;
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
		var start=0;
		var dirty=false;

		for (start=0;start<box.options.length;start++) {
			if (box.options[start].selected==false) {
				break;
			}
		}
		start++;

		for (var i=start;i<box.options.length;i++) {
			if (box.options[i].selected) {
				paletteSwapHelper(box, i, i-1);
				box.options[i-1].selected=true;
				dirty=true;
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
		var start=0;
		var dirty=false;

		for (start=box.options.length-1;start>=0;start--) {
			if (box.options[start].selected==false) {
				break;
			}
		}
		start--;

		for (var i=start;i>=0;i--) {
			if (box.options[i].selected) {
				paletteSwapHelper(box, i, i+1);
				box.options[i+1].selected=true;
				dirty=true;
			}
		}
		return dirty;
	}
	
		
	function paletteSwapHelper(box, idx1, idx2) {
		var value1=box.options[idx1].value;
		var text1=box.options[idx1].text;
		var value2=box.options[idx2].value;
		var text2=box.options[idx2].text;
		box.options[idx1]=new Option(text2, value2);
		box.options[idx2]=new Option(text1, value1);
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
	