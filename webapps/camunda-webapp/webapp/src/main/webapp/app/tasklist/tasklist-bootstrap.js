(function(document, window, require) {
  'use strict';

  var baseUrl = '../../../';
  var APP_NAME = 'tasklist';
  var pluginPackages = window.PLUGIN_PACKAGES || [];

  require([baseUrl +'require-conf'], function(rjsConf) {
    require({
      baseUrl:    baseUrl,
      paths:      rjsConf.paths,
      shim:       rjsConf.shim,
      packages:   rjsConf.packages.concat(pluginPackages),
      urlArgs:    'bust='+ (new Date()).getTime()
    });

    require([
      'angular',
      'angular-resource',
      'angular-sanitize',
      'angular-ui',
      'ngDefine',
      'bootstrap',
      'jquery-ui'
    ], function(angular) {
      require([
        APP_NAME,
        'domReady!'
      ], function() {
        rjsConf.utils.bootAngular(angular, APP_NAME);
      });
    });
  });

})(document, window || this, require);
