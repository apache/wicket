
var WicketPreview = {
  require: function(libraryName) {
    // inserting via DOM fails in Safari 2.0, so brute force approach
    document.write('<script type="text/javascript" src="' + libraryName + '"></script>');
  },
  findJavascriptPath: function() {
    var scripts = document.getElementsByTagName("script");
    for (var x = 0; x < scripts.length; x++) {
      var s = scripts[x];
      if (s.src && s.src.match(/wicket-preview\.js(\?.*)?$/)) {
        return s.src.replace(/wicket-preview\.js(\?.*)?$/,'');
      }
    }
  },
  load: function() {
    var path = WicketPreview.findJavascriptPath();
    var includes = 'behaviour,dojo,wicket-preview-behaviour'.split(',');
    for (var x = 0; x < includes.length; x++) {
      var include = includes[x];
      WicketPreview.require(path + include + '.js');
    }
  }
}

WicketPreview.load();


