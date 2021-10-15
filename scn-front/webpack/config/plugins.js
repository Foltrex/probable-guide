const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const HTMLWebpackPlugin = require("html-webpack-plugin");
const WrmPlugin = require("atlassian-webresource-webpack-plugin");
const path = require("path");

module.exports = (target = "") => {
    const plugins = [];
    plugins.push(new CleanWebpackPlugin());
    switch (target) {
        case "scn-automation-plugin":
            plugins.push(
                new WrmPlugin({
                    pluginKey: "com.scn.jira.automation.scn-automation-plugin",
                    watch: true,
                    xmlDescriptors: path.resolve(
                        __dirname,
                        "../../target",
                        "scn-automation-plugin",
                        "compiled-front",
                        "META-INF",
                        "plugin-descriptors",
                        "wr-defs.xml"
                    ),
                })
            );
            break;
        case "scn-logtime-plugin":
            plugins.push(
                new WrmPlugin({
                    pluginKey: "com.scn.jira.scn-logtime-plugin",
                    watch: true,
                    xmlDescriptors: path.resolve(
                        __dirname,
                        "../../target",
                        "scn-logtime-plugin",
                        "compiled-front",
                        "META-INF",
                        "plugin-descriptors",
                        "wr-defs.xml"
                    ),
                })
            );
            break;
        default:
            plugins.push(
                new HTMLWebpackPlugin({
                    template: path.resolve(
                        __dirname,
                        "../../public/index.html"
                    ),
                    title: "Test environment",
                    minify: {
                        collapseWhitespace: true,
                        removeComments: true,
                    },
                })
            );
            break;
    }

    return plugins;
};
