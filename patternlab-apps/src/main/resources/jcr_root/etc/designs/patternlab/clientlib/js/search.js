var PatternSearch = {

  data:   [],
  active: false,

  init: function() {

    var patternSearchResults= $('body').data("search"),
        currentPath = location.pathname.substr(0, location.pathname.indexOf('.'));

    for (var i in patternSearchResults) {
          var element = {};
          element.patternId = patternSearchResults[i];
          element.patternPath = currentPath + '.pattern.' +  element.patternId + '.html';
          this.data.push(element);
    }

    var patterns = new Bloodhound({
      datumTokenizer: function(d) { return Bloodhound.tokenizers.nonword(d.patternId); },
      queryTokenizer: Bloodhound.tokenizers.nonword,
      limit: 10,
      local: this.data
    });

    // initialize the bloodhound suggestion engine
    patterns.initialize();

    $('#sg-find .typeahead').typeahead({ highlight: true }, {
      displayKey: 'patternId',
      source: patterns.ttAdapter()
    }).on('typeahead:selected', PatternSearch.onSelected).on('typeahead:autocompleted', PatternSearch.onAutocompleted);

  },

  openPath: function(item) {
    window.open(item.patternPath,"_top");
  },

  onSelected: function(e,item) {
    PatternSearch.openPath(item);
  },

  onAutocompleted: function(e,item) {
    PatternSearch.openPath(item);
  },

  openSearch: function() {
    PatternSearch.active = true;
    $("#sg-find").addClass('show-overflow');
    $('#sg-find .typeahead').val("");
    $('.sg-tools-toggle').addClass('active');
    $('.sg-checklist').addClass('active');
    $('#sg-find #typeahead')[0].focus();
  },

  closeSearch: function() {
    PatternSearch.active = false;
    document.activeElement.blur();
    $("#sg-find").removeClass('show-overflow');
    $('.sg-tools-toggle').removeClass('active');
    $('.sg-checklist').removeClass('active');
    $('#sg-find .typeahead').val("");
  },

  toggleSearch: function(e) {
    var evtobj = window.event? event : e;
    if (evtobj.ctrlKey && evtobj.shiftKey && evtobj.keyCode == 70) { //ctrl + shift + F
        if (!PatternSearch.active) {
          PatternSearch.openSearch();
        } else {
          PatternSearch.closeSearch();
        }
    }
  }

};

PatternSearch.init();

window.onkeydown = PatternSearch.toggleSearch;

$('#sg-find .typeahead').focus(function() {
  if (!PatternSearch.active) {
    PatternSearch.openSearch();
  }
});

$('#sg-find .typeahead').blur(function() {
  PatternSearch.closeSearch();
});
