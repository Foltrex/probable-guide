const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const HTMLWebpackPlugin = require("html-webpack-plugin");
const TerserWebpackPlugin = require("terser-webpack-plugin");
const WrmPlugin = require("atlassian-webresource-webpack-plugin");
const path = require("path");

const isDev = process.env.NODE_ENV === "development";
const isProd = !isDev;

const optimization = () => {
    const config = {};

    if (isProd) {
        config.minimizer = [new TerserWebpackPlugin()];
    }

    return config;
};

const plugins = () => {
    const base = [];
    if (isProd) {
        base.push(
            new WrmPlugin({
                pluginKey: "com.scn.jira.automation.scn-automation-plugin",
                locationPrefix: "build/js",
                watch: true,
                xmlDescriptors: path.resolve(
                    __dirname,
                    "../main/resources",
                    "META-INF",
                    "plugin-descriptors",
                    "wr-defs.xml"
                ),
            })
        );
    } else {
        base.push(
            new HTMLWebpackPlugin({
                template: "../public/index.html",
                title: "Test environment",
                minify: {
                    collapseWhitespace: true,
                    removeComments: true,
                },
            })
        );
    }
    base.push(new CleanWebpackPlugin());

    return base;
};

module.exports = {
    context: path.resolve(__dirname, "src"),
    mode: "development",
    entry: {
        "autotimetracking-table": [
            "@babel/polyfill",
            "./autotimetracking-table.tsx",
        ],
    },
    output: {
        filename: "[name].bundle.js",
        path: isProd
            ? path.resolve(__dirname, "../main/resources/build/js")
            : path.resolve(__dirname, "build"),
    },
    resolve: {
        extensions: [".tsx", ".ts", ".jsx", ".js"],
    },
    optimization: optimization(),
    devServer: {
        port: 4200,
        hot: true,
        historyApiFallback: true,
        proxy: {
            "/rest": {
                target: "http://localhost:2990",
                auth: "akalaputs:akalaputs",
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
    devtool: isDev ? "source-map" : "",
    plugins: plugins(),
    module: {
        rules: [
            {
                test: /\.(ts|js)x?$/,
                exclude: /node_modules/,
                use: {
                    loader: "babel-loader",
                },
            },
            {
                test: /\.css$/,
                use: ["style-loader", "css-loader"],
            },
        ],
    },
};
