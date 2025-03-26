const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  app.use(
    '/api',
    createProxyMiddleware({ 
      target: 'http://localhost:8080', // 요청할 서버 주소
      // target: 'https://seeandyougo.com', // 요청할 서버 주소
      changeOrigin: true,
    })
  );
};