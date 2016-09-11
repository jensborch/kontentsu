/**
 * System configuration for Kontentsu editing client.
 */
(function (global) {
  const paths = {
    // paths serve as alias
    'npm:': 'node_modules/'
  };
  const map = {
    // our app is within the app folder
    app: 'app',
    // angular bundles
    '@angular': 'npm:@angular',
    '@angular2-material': 'npm:@angular2-material',

    // other libraries
    'rxjs': 'npm:rxjs',
    'angular2-in-memory-web-api': 'npm:angular2-in-memory-web-api',
  };
  const packages = {
    app: {
      main: './main.js',
      defaultExtension: 'js'
    },
    rxjs: {
      defaultExtension: 'js'
    },
    'angular2-in-memory-web-api': {
      main: './index.js',
      defaultExtension: 'js'
    }
  };
  const angularPackages = [
    'common',
    'compiler',
    'core',
    'http',
    'platform-browser',
    'platform-browser-dynamic',
    'router',
    'forms'
  ];
  angularPackages.forEach(function (name) {
    packages['@angular/' + name] = {
      format: 'cjs',
      main: 'bundles/' + name + '.umd.js',
      defaultExtension: 'js'
    };
  });
  var materialComponents = [
    'core',
    'toolbar',
    'menu'
  ];
  materialComponents.forEach(function (name) {
    packages[("@angular2-material/" + name)] = {
      format: 'cjs',
      main: name + '.umd.js',
      defaultExtension: 'js'
    };
  });
  var config = {
    map: map,
    packages: packages,
    paths: paths
  };

  System.config(config);

})(this);