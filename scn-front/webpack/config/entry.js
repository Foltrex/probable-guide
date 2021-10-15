module.exports = (target) => {
    switch (target) {
        case "scn-automation-plugin":
            return {
                "autotimetracking-table": "./AutoTT.tsx",
                "worklog-backup": "./WorklogBackup.tsx",
            };
        case "scn-logtime-plugin":
            return { test: "./test.ts" };
        default:
            return {};
    }
};
