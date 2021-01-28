const TerserWebpackPlugin = require("terser-webpack-plugin");

module.exports = { minimizer: [new TerserWebpackPlugin()] };
