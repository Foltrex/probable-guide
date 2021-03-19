const path = require("path");

module.exports = (target) => ({
    filename: "build/js/[name].bundle.js",
    path: path.resolve(__dirname, "../../target", target),
});
