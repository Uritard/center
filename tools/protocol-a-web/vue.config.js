// vue.config.js
const path = require('path');

const CompressionPlugin = require('compression-webpack-plugin'); // 开启压缩
const {HashedModuleIdsPlugin} = require('webpack');
const Happypack = require('happypack');

function resolve(dir) {
    return path.join(__dirname, dir)
}

const isProduction = process.env.NODE_ENV === 'production';

module.exports = {
    lintOnSave: false, // 关闭eslint
    productionSourceMap: !isProduction,
    publicPath: './',
    assetsDir: 'assets',
    outputDir: process.env.outputDir, // 生成文件的目录名称
    chainWebpack: config => {
        // 移除 prefetch 插件
        config.plugins['delete']('prefetch');
        // 移除 preload 插件，避免加载多余的资源
        config.plugins['delete']('preload-index');
        // 定义文件夹的路径
        config.resolve.alias.set('@', resolve('src'));

        // 替换压缩图片原有loader
        const imagesRule = config.module.rule('images');
        imagesRule.uses.clear();
        imagesRule.use('file-loader')
            .loader('url-loader')
            .options({
                limit: 10240,
                fallback: {
                    loader: 'file-loader',
                    options: {
                        outputPath: 'assets/images'
                    }
                }
            });

        // 压缩响应的app.json返回的代码压缩
        config.optimization.minimize(true);

        // webpack 会默认给commonChunk打进chunk-vendors，所以需要对webpack的配置进行delete
        config.optimization['delete']('splitChunks');
    },

    configureWebpack: config => {
        const plugins = [];
        if (isProduction) {
            // 代码打包之后取出console.log 压缩代码
            const TerserPlugin = require('terser-webpack-plugin');
            // 代码压缩去除console.log
            plugins.push(
                new TerserPlugin({
                    terserOptions: {
                        ecma: undefined,
                        warnings: false,
                        parse: {},
                        compress: {
                            pure_funcs: ['console.log'] // 移除console
                        }
                    }
                })
            );
            // 服务器也要相应开启gzip
            plugins.push(
                new CompressionPlugin({
                    algorithm: 'gzip',
                    test: /\.(js|css)$/, // 匹配文件名
                    threshold: 1024 * 20, // 对超过10k的数据压缩
                    deleteOriginalAssets: false, // 不删除源文件
                    minRatio: 0.8 // 压缩比
                })
            );

            // 用于根据模块的相对路径生成 hash 作为模块 id, 一般用于生产环境
            plugins.push(
                new HashedModuleIdsPlugin()
            )

            // 多线程优化构建速度
            config.plugins.push(
                new Happypack({
                    loaders: ['babel-loader', 'vue-loader', 'url-loader'],
                    cache: true,
                    threads: 3 // 线程数取决于你电脑性能的好坏，好的电脑建议开更多线程
                })
            );
        }
        // 开启分离js
        config.optimization = {
            runtimeChunk: 'single',
            splitChunks: {
                cacheGroups: {
                    vendor: {
                        chunks: 'all',
                        test: /[\\/]node_modules[\\/]/,
                        name(module) {
                            // 排除node_modules 然后吧 @ 替换为空 ,考虑到服务器的兼容
                            const packageName = module.context.match(/[\\/]node_modules[\\/](.*?)([\\/]|$)/)[1]
                            return `npm.${packageName.replace('@', '')}`
                        },
                        minChunks: 3, // 模块至少使用次数
                        maxInitialRequests: 5, // 首页加载的时候引入的文件最多5个
                        minSize: 1024 * 100 // 字节 引入的文件大于1024 * 100kb 才进行分割
                    },
                    common: {
                        chunks: 'all',
                        test: /[\\/]src[\\/]js[\\/]/,
                        name: 'common',
                        minChunks: 2,
                        maxInitialRequests: 5,
                        minSize: 0,
                        priority: 60
                    },
                    styles: {
                        name: 'styles',
                        test: /\.(sa|sc|c)ss$/,
                        chunks: 'all',
                        enforce: true
                    },
                    runtimeChunk: {
                        name: 'manifest'
                    }
                }
            }
        };
        return {plugins}
    },
    css: {},
    pluginOptions: {
        // 配置全局less
        'style-resources-loader': {
            preProcessor: 'less',
            patterns: [resolve('./src/style/theme.less')]
        }
    },
    devServer: {
        open: false, // 自动启动浏览器
        host: '0.0.0.0', // localhost
        port: 10086, // 端口号
        https: false,
        hotOnly: false, // 热更新
        proxy: {
            '^/demo': {
                target: 'http://192.168.20.11:18088/demo/',
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
