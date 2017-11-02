$('div.codemirror-html').each(function() {
    var $this = $(this);
    var textarea = $this.find('textarea').hide()[0];
    var editor = CodeMirror(function(elt) {
        textarea.parentNode.appendChild(elt);
    }, {
        value: textarea.value,
        lineNumbers: true,
        indentUnit: 4,
        theme: 'ambiance'
    });
    editor.on('change', function(inst) {
        textarea.value = inst.getValue();
    });
});
