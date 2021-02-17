module.exports = (target) => {
    switch (target) {
        case "scn-automation-plugin":
            return {
                "autotimetracking-table": ["@babel/polyfill", "./AutoTT.tsx"],
                "worklog-backup": ["@babel/polyfill", "./WorklogBackup.tsx"],
            };
        case "scn-logtime-plugin":
            return { test: ["@babel/polyfill", "./test.ts"] };
        default:
            return {};
    }
};
