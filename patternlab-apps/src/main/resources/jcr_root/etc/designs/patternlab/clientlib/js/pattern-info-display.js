var PatternInfoDisplayViewer = {

  active: [ ],

  hidden: true,

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

      //render description
      var description = $('#sg-pattern-extra-'+patternId + ' .sg-pattern-desc'),
          markdownData = description.attr('data-description');

      if (markdownData != undefined && markdownData.length > 0) {
          var result = window.markdownit().render(markdownData);
          description.html(result);
      }

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
            $(panels[j]).find('code.language-' + language)[0].innerHTML = Prism.highlight($.trim(code).replace(/^\s*[\r\n]/gm,''), Prism.languages[language]);
         }
         else {
            var link = panels[j].getAttribute('data-link'),
                trials = 0;

            function stripScripts(s) {
                var div = document.createElement('div');
                div.innerHTML = s;
                var scripts = div.getElementsByTagName('script');
                var i = scripts.length;
                while (i--) {
                  scripts[i].parentNode.removeChild(scripts[i]);
                }
                return div.innerHTML;
              }

            function updateMarkup(url, language, panel) {
                if (link != null && link.length > 0) {
                    $.ajax({
                       url:link,
                       type:'GET',
                       panel: panel,
                       language: language,
                       success: function(code){
                           var bodyIndex = code.indexOf('<body') + 5,
                               bodyCode = code.substring(code.indexOf('>',bodyIndex) + 1, code.indexOf('</body>')),
                               strippedCode = stripScripts(bodyCode),
                               trimmedCode = $.trim(strippedCode).replace('<template>','').replace(/^\s*[\r\n]/gm,'');
                           $(this.panel).find('code.language-' + language)[0].innerHTML = Prism.highlight(trimmedCode, Prism.languages[language]);
                       },
                       error: function() {
                           if (trials < 5) {
                               trials++;
                               window.setTimeout(function() {updateMarkup(url, language, panel);}, 200);
                           }
                       }
                    });

                }
            }
            updateMarkup(link, language, panels[j]);

         }
      }

       if (panels.length > 0) {
               var panel = $(panels[0]);
               panel.show();
               var height = panel.find('code').height() + 63;
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
             var height = $(panels[index]).find('code').height() + 63;
             $(panels).height(height);
         });
      });

      $('#sg-t-patterninfo')[0].onclick = (function(e) {
         e.preventDefault();
         if (that.hidden) {
            $('.sg-pattern-extra-toggle').not('.active').click();
         }
         else {
            $('.sg-pattern-extra-toggle.active').click();
         }
         $(this).text(that.hidden ? 'Hide Pattern Info' : 'Show Pattern Info');
         that.hidden = !that.hidden;
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