const { CleanWebpackPlugin } = require("clean-webpack-plugin");

const HTMLWebpackPlugin = require("html-webpack-plugin");
const path = require("path");
const webpack = require("webpack");

module.exports = {
    context: path.resolve(__dirname, "../src"),
    mode: "development",
    entry: { index: "./index.tsx" },
    output: {
        filename: "[name].bundle.js",
        path: path.resolve(__dirname, "../target"),
    },
    resolve: require("./config/resolve"),
    devServer: {
        port: 4200,
        hot: true,
        historyApiFallback: true,
        proxy: {
            "/rest": {
                target: "http://localhost:2990",
                auth: "admin:admin",
                pathRewrite: { "^/rest": "/jira/rest" },
            },
            "/browse": {
                target: "http://localhost:2990",
                auth: "admin:admin",
                pathRewrite: { "^/browse": "/jira/browse" },
            },
            "/secure": {
                target: "http://localhost:2990",
                auth: "admin:admin",
                pathRewrite: { "^/secure": "/jira/secure" },
            },
        },
    },
    plugins: [
        new CleanWebpackPlugin(),
        new HTMLWebpackPlugin({
            template: path.resolve(__dirname, "../public/index.html"),
            title: "Test environment",
            minify: {
                collapseWhitespace: true,
                removeComments: true,
            },
        }),
        new webpack.ProvidePlugin({
            process: "process/browser",
        }),
    ],
    module: require("./config/module"),
};
