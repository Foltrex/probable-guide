const http = require('http');
const httpProxy = require('http-proxy');

const targetUrl = 'http://localhost:2990';

const proxy = httpProxy.createProxyServer({});

proxy.on('proxyRes', (proxyRes, req, res) => {
    proxyRes.headers['Access-Control-Allow-Origin'] = 'http://localhost:2991 http://localhost:4200';
    proxyRes.headers['Access-Control-Allow-Headers'] = '*';
    proxyRes.headers['Access-Control-Allow-Methods'] = '*';
    proxyRes.headers['X-Frame-Options'] = "http://localhost:2991 http://localhost:4200";
    proxyRes.headers['Content-Security-Policy'] = "frame-ancestors http://localhost:2991 http://localhost:4200";
});

const server = http.createServer((req, res) => {
    console.log('Proxying request:', req.url);
    proxy.web(req, res, {target: targetUrl});
});

server.listen(2991, () => {
});
