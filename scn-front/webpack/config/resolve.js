const path = require("path");

module.exports = {
    extensions: [".tsx", ".ts", ".jsx", ".js"],
    alias: {
        api: path.resolve(__dirname, "../../src/api"),
        components: path.resolve(__dirname, "../../src/components"),
        config: path.resolve(__dirname, "../../src/config"),
        containers: path.resolve(__dirname, "../../src/containers"),
        models: path.resolve(__dirname, "../../src/models"),
        modules: path.resolve(__dirname, "../../src/modules"),
        services: path.resolve(__dirname, "../../src/services"),
        utils: path.resolve(__dirname, "../../src/utils"),
    },
};
