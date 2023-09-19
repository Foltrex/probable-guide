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
    AJS.$("#space-key-form").submit(function (event) {
        event.preventDefault();
    });

    const initTableObject = {
        autoFocus: false,
        el: jQuery("#user-table"),
        allowReorder: true,
        resources: {
            all: AJS.contextPath() + "/rest/scn-smart-permissions-manager-plugin/1.0/space-permission/all",
            self: AJS.contextPath() + "/rest/scn-smart-permissions-manager-plugin/1.0/space-permission/self"
        },
        deleteConfirmationCallback: function (model) {
            AJS.$("#restful-table-model")[0].innerHTML = "<b>ID:</b> " + model.id + " <b>status:</b> " + model.status + " <b>description:</b> " + model.description;
            AJS.dialog2("#delete-confirmation-dialog").show();
            return new Promise(function (resolve, reject) {
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
                header: "Permission Level",
                editView: AJS.RestfulTable.CustomEditView.extend({
                    render: function (self) {
                        var $select = $("<select name='group' class='select'>" +
                                "<option value='Friends'>Friends</option>" +
                                "<option value='Family'>Family</option>" +
                                "<option value='Work'>Work</option>" +
                                "</select>");
            
                        $select.val(self.value); // select currently selected
                        return $select;
                    }
                })
            }
        ]
    };
    let table;

    const searchButton = AJS.$("#search-button");
    const spaceSearchInput = AJS.$("#space-key");
    searchButton.click(() => {
        AJS.$("#user-table").empty();
        AJS.$(".aui-restfultable-init").remove();
        const spaceSearchInputText = spaceSearchInput.val();
        table = new AJS.RestfulTable({
            ...initTableObject,
            resources: {
                all: AJS.contextPath() + "/rest/scn-smart-permissions-manager-plugin/1.0/space-permission/all/" + spaceSearchInputText,
                self: AJS.contextPath() + "/rest/scn-smart-permissions-manager-plugin/1.0/space-permission/self"
            },

        })
    });

    table = new AJS.RestfulTable(initTableObject);
});
