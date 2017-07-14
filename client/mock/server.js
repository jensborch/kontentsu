
const modRewrite = require('connect-modrewrite');
const serveStatic = require('serve-static');
const cors = require('connect-cors')
const connect = require('connect');
const http = require('http');
const path = require('path');

const files = path.join(__dirname, 'files');

var app = connect();

app.use(modRewrite([
    '^/kontentsu/api/files/pages/(.+)/$ /pages/$1/index.json [L]',
    '^/kontentsu/api/files/images/(.+)$ /images/$1 [L]'
]))
    .use(cors({}))
    .use(serveStatic(files));

http.createServer(app).listen(9090);
