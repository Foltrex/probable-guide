const path = require("path");

module.exports = (target) => ({
    filename: "js/[name].bundle.js",
    path: path.resolve(__dirname, "../../target", target, "compiled-front"),
});
