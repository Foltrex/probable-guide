module.exports = {
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
};
