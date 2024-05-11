module.exports = {
  lintOnSave: false, // 关闭eslint
  productionSourceMap: false,
  publicPath: '/',
  outputDir: '../tools/protocol-a/protocol-a-demo/src/main/resources/static/', // 打包的目录
  devServer: {
    open: false, // 自动启动浏览器
    host: '0.0.0.0', // localhost
    port: 10086, // 端口号
    https: false,
    hot: 'only', // 热更新
    proxy: {
      '^/demo': {
        target: 'http://172.24.49.97:18088/demo/',
        ws: true, // 开启WebSocket
        secure: false, // 如果是https接口，需要配置这个参数
        changeOrigin: true,
        pathRewrite: {
          '^/demo': '' // 这里理解成用'/api'代替target里面的地址,比如我要调用'http://40.00.100.100:3002/user/add'，直接写'/api/user/add'即可
        }
      }
    }
  }
}
