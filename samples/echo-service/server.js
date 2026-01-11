const http = require('http');

const server = http.createServer((req, res) => {
  let body = '';
  req.on('data', chunk => { body += chunk; });
  req.on('end', () => {
    res.setHeader('Content-Type', 'application/json');
    res.writeHead(200);
    res.end(JSON.stringify({
      method: req.method,
      path: req.url,
      headers: req.headers,
      body: body ? JSON.parse(body) : null
    }));
  });
});

server.listen(9000, () => {
  console.log('Echo service listening on port 9000');
});
