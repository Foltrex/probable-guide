document.addEventListener('DOMContentLoaded', () => {
    // const spaceSearchInput = document.getElementById("space-key");
    // console.log(spaceSearchInput);
    // spaceSearchInput.addEventListener('change', () => {
    //     const spaceSearchInputText = spaceSearchInput.innerText;
    //     console.log(spaceSearchInputText);
    // })
    // console.log(spaceSearchInputText);
})

AJS.$(document).ready(function () {
    const spaceSearchInput = AJS.$("#space-key");
    console.log(spaceSearchInput);
    spaceSearchInput.change(() => {
        const spaceSearchInputText = spaceSearchInput.text();
        console.log("hiii" + spaceSearchInputText);
    })

    let table;
    function refreshTable() {
        if (table) {
            table.refresh();
        }
    }

    table = new AJS.RestfulTable({
        autoFocus: false,
        el: jQuery("#user-table"),
        allowReorder: true,
        resources: {
            all: AJS.contextPath() + "/rest/scn-smart-permissions-manager-plugin/1.0/space-permission/all",
            self: AJS.contextPath() + "/rest/scn-smart-permissions-manager-plugin/1.0/space-permission/self"
        },
        deleteConfirmationCallback: function(model) {
            AJS.$("#restful-table-model")[0].innerHTML = "<b>ID:</b> " + model.id + " <b>status:</b> " + model.status + " <b>description:</b> " + model.description;
            AJS.dialog2("#delete-confirmation-dialog").show();
            return new Promise(function(resolve, reject) {
                AJS.$("#dialog-submit-button").on('click', function (e) {
                    resolve();
                    e.preventDefault();
                    AJS.dialog2("#delete-confirmation-dialog").hide();
                });
                AJS.$(".aui-dialog2-header-close, #warning-dialog-cancel").on('click', function (e) {
                    reject();
                    e.preventDefault();
                    AJS.dialog2("#delete-confirmation-dialog").hide();
                });
            });
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
