const path = require("path");
const SCN_AUTOMATION_PLUGIN = "scn-automation-plugin";
const SCN_LOGTIME_PLUGIN = "scn-logtime-plugin";
const isProd = process.env.NODE_ENV === "production";

const getPluginConfig = (target) => ({
    context: path.resolve(__dirname, "src", "modules", target),
    mode: "production",
    entry: require("./config/webpack/entry")(target),
    output: require("./config/webpack/output")(target),
    resolve: require("./config/webpack/resolve"),
    optimization: require("./config/webpack/optimization"),
    plugins: require("./config/webpack/plugins")(target),
    module: require("./config/webpack/module"),
});

module.exports = isProd
    ? [
          getPluginConfig(SCN_AUTOMATION_PLUGIN),
          getPluginConfig(SCN_LOGTIME_PLUGIN),
      ]
    : {
          context: path.resolve(__dirname, "src"),
          mode: "development",
          entry: { index: ["@babel/polyfill", "./index.tsx"] },
          output: {
              filename: "[name].bundle.js",
              path: path.resolve(__dirname, "target"),
          },
          resolve: require("./config/webpack/resolve"),
          devServer: require("./config/webpack/devServer"),
          plugins: require("./config/webpack/plugins")(),
          module: require("./config/webpack/module"),
      };
