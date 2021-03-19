module.exports = {
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
};
