
const modRewrite = require('connect-modrewrite');
var serveStatic = require('serve-static');
const connect = require('connect');
const http = require('http');
const path = require('path');

const files = path.join(__dirname, 'files');

var app = connect();

app.use(modRewrite([
    '^/test$ /index.html',
    '^/test/\\d*$ /index.html [L]',
    '^/test/\\d*/\\d*$ /flag.html [L]'
]))
    .use(serveStatic(files));

http.createServer(app).listen(8080);
