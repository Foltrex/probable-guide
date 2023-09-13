AJS.$(document).ready(function () {
    new AJS.RestfulTable({
        autoFocus: false,
        el: jQuery("#user-table"),
        allowReorder: true,
        resources: {
            all: "rest/scn-smart-permissions-manager-plugin/1.0/space-permission",
            self: "rest/evt-restful-table/1.0/events-restful-table/self"
        },
        columns: [
            {
                id: "spaceKey",
                header: "Space Key"
            },
            {
                id: "username",
                header: "Username"
            },
            {
                id: "permissionLevel",
                header: "Permission Level"
            }
        ]
    });
});
