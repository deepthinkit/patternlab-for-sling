var PatternInfoDisplayViewer = {

  active: [ ],

  init: function() {
    var that = this,
        toggles = document.querySelectorAll('.sg-pattern-extra-toggle');

    for (var i = 0; i < toggles.length; ++i) {

      //add on click listeners
      toggles[i].onclick = (function(e) {
          e.preventDefault();
          that.toggle(this.getAttribute('data-pattern-id'));
      });

      var patternId = toggles[i].getAttribute('data-pattern-id');

      var tabs = $('#sg-pattern-extra-'+patternId + ' .sg-tabs-list > li');
      if (tabs.length > 0) {
         $(tabs[0]).addClass('sg-tab-title-active');
      }

      var panels = $('#sg-pattern-extra-'+patternId + ' .sg-tabs-panel');

      //render markup data with prism
      for (var j = 0; j < panels.length; ++j) {
         var language = panels[j].getAttribute('data-language'),
             code = panels[j].getAttribute('data-code');
         if (code != null && code.length > 0) {
            $(panels[j]).find('code.language-markup')[0].innerHTML = Prism.highlight($.trim(code).replace(/^\s*[\r\n]/gm,''), Prism.languages[language]);
         }
         else {
            var link = panels[j].getAttribute('data-link');
            if (link != null && link.length > 0) {
                $.ajax({
                   url:link,
                   type:'GET',
                   injectElement: panels[j],
                   success: function(code){
                       $(this.injectElement).find('code.language-markup')[0].innerHTML = Prism.highlight($.trim(code).replace('<template>','').replace(/^\s*[\r\n]/gm,''), Prism.languages[language]);
                   }
                });

            }
         }
      }

       if (panels.length > 0) {
               var panel = $(panels[0]);
               panel.show();
               var height = panel.find('code.language-markup').height() + 63;
               panel.closest('.sg-tabs-panel').height(height);
       }


      var tabAnchors = tabs.find('a');

      tabAnchors.each( function(index){
         $(this).on('click',function(event) {
             event.preventDefault();
             var patternExtra = $(this).closest('.sg-tabs');
             var tabs = patternExtra.find('.sg-tabs-list > li');
             tabs.removeClass('sg-tab-title-active');
             $(tabs[index]).addClass('sg-tab-title-active');
             var panels = patternExtra.find('.sg-tabs-panel');
             panels.hide();
             $(panels[index]).show();
             var height = $(panels[index]).find('code.language-markup').height() + 63;
             $(panels).height(height);
         });
      });
    }
  },

  toggle: function(patternId) {
    if ((PatternInfoDisplayViewer.active[patternId] === undefined) || !PatternInfoDisplayViewer.active[patternId]) {
      this.active[patternId] = true;
      document.getElementById('sg-pattern-extra-toggle-'+patternId).classList.add('active');
      document.getElementById('sg-pattern-extra-'+patternId).classList.add('active');
    } else {
      this.active[patternId] = false;
      document.getElementById('sg-pattern-extra-toggle-'+patternId).classList.remove('active');
      document.getElementById('sg-pattern-extra-'+patternId).classList.remove('active');
    }
  }

};


PatternInfoDisplayViewer.init();