const path = require("path");
const SCN_AUTOMATION_PLUGIN = "scn-automation-plugin";
const SCN_LOGTIME_PLUGIN = "scn-logtime-plugin";

const getPluginConfig = (target) => ({
    context: path.resolve(__dirname, "../src", "modules", target),
    mode: "production",
    entry: require("./config/entry")(target),
    output: require("./config/output")(target),
    resolve: require("./config/resolve"),
    optimization: require("./config/optimization"),
    plugins: require("./config/plugins")(target),
    module: require("./config/module"),
});

module.exports = [
    getPluginConfig(SCN_AUTOMATION_PLUGIN),
    getPluginConfig(SCN_LOGTIME_PLUGIN),
];
