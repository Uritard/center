module.exports = {
    presets: [
        '@vue/cli-plugin-babel/preset'
    ],
    plugins: [
        ["transform-es2015-arrow-functions", { spec: true }],
        '@babel/plugin-proposal-optional-chaining'
    ]
}
