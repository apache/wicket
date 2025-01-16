function handleDefaultSubmit(event, submitId, keyStroke) {
    const tag = event.target.tagName.toLowerCase();
    const contenteditable = event.target.getAttribute('contenteditable') === 'true';

    // we're interested only in inputs, textareas and contenteditable fields
    if (tag !== 'input' && tag !== 'textarea' && !contenteditable) return;

    // submit for inputs
    if (tag === 'input' && event.which != 13) return;
    // multi-line inputs (textarea, contenteditable)
    if (tag === 'textarea' || contenteditable) {
        switch (keyStroke) {
            case 'ENTER': if (event.which != 13) return;
            case 'CTRL_ENTER': if (!event.ctrlKey || event.which != 13) return; break;
            case 'SHIFT_ENTER': if (!event.shiftKey || event.which != 13) return; break;
            case 'NONE':
            default: return;
        }
    }

    var b = document.getElementById(submitId);
    if (window.getComputedStyle(b).visibility === 'hidden') return;

    event.stopPropagation();
    event.preventDefault();

    if (b != null && b.onclick != null && typeof (b.onclick) != 'undefined') {
        var r = Wicket.bind(b.onclick, b)();
        if (r != false) b.click();
    } else {
        b.click();
    }

    return false;
}