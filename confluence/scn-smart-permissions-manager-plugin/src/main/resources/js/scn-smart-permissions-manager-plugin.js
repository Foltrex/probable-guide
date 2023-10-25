
AJS.$(document).ready(function () {
    AJS.$("#space-key-form").submit(function (event) {
        event.preventDefault();
    });

    const initTableObject = {
        autoFocus: false,
        el: jQuery("#user-table"),
        allowReorder: true,
        resources: {
            all: AJS.contextPath() + "/rest/scn-smart-permissions-manager-plugin/1.0/space-permission/",
            self: AJS.contextPath() + "/rest/scn-smart-permissions-manager-plugin/1.0/space-permission/"
        },
        deleteConfirmationCallback: function (model) {
            AJS.$("#user-permission-model").html(`
                If you do this, there's no going back. 
                Are you certain that you want to delete ${model.username} access to ${model.spaceKey}?`
            );
            AJS.dialog2("#delete-permission-dialog").show();
            return new Promise(function (resolve, reject) {
                AJS.$("#delete-permisison-dialog-confirm").on('click', function (e) {
                    resolve();
                    e.preventDefault();
                    AJS.dialog2("#delete-permission-dialog").hide();
                });
                AJS.$("#delete-permission-dialog-cancel").on('click', function (e) {
                    reject();
                    e.preventDefault();
                    AJS.dialog2("#delete-permission-dialog").hide();
                });
            });
        },
        columns: [
            {
                id: "spaceKey",
                header: "Space Key",
                allowEdit: false,
                createView: AJS.RestfulTable.CustomCreateView.extend({
                    render: _ => {
                        const spaceKeyDataList = AJS.$("#spaces").clone();
                        const randomNumber = Math.floor(Math.random());
                        spaceKeyDataList.attr('id', `spaces-${randomNumber}`)
                        return $(`
                            <input name='spaceKey' type='text' class='text' list='spaces-${randomNumber}' />
                            ${spaceKeyDataList.prop('outerHTML')}

                        `);
                    }
                }),
                readView: AJS.RestfulTable.CustomReadView.extend({
                    render: self => $(`<span>${self.value}</span>`)
                })
            },
            {
                id: "username",
                header: "Username",
                allowEdit: false,
                createView: AJS.RestfulTable.CustomCreateView.extend({
                    render: _ => {
                        const usernameDatalist = AJS.$("#usernames").clone();
                        const randomNumber = Math.floor(Math.random());
                        usernameDatalist.attr('id', `usernames-${randomNumber}`)
                        return $(`
                            <input name='username' type='text' class='text' list='usernames-${randomNumber}'/>
                            ${usernameDatalist.prop('outerHTML')}
                        `)
                    }
                }),
                readView: AJS.RestfulTable.CustomReadView.extend({
                    render: self => $(`<span>${self.value}</span>`)
                })
            },
            {
                id: "permissionLevel",
                header: "Permission Level",
                createView: AJS.RestfulTable.CustomCreateView.extend({
                    render: function (self) {
                        return $(`
                            <select name='permissionLevel' class='select'>
                                <option value='View'>Can view</option>
                                <option value='Edit'>Can view and edit</option>
                            </select>
                        `);
                    }
                }),
                editView: AJS.RestfulTable.CustomEditView.extend({
                    render: function (self) {
                        var $select = $(`
                            <select name='permissionLevel' class='select'>
                                <option value='View'>Can view</option>
                                <option value='Edit'>Can view and edit</option>
                            </select>
                        `);

                        $select.val(self.value);
                        return $select;
                    }
                }),
                readView: AJS.RestfulTable.CustomReadView.extend({
                    render: function (self) {
                        var $select = $(`
                            <select name='permissionLevel' class='select'>
                                <option value='View'>Can view</option>
                                <option value='Edit'>Can view and edit</option>
                            </select>
                        `);

                        $select.val(self.value);
                        return $select;
                    }
                })
            },
            // {
            //     id: "date",
            //     header: "Event date",
            //     createView: AJS.RestfulTable.CustomCreateView.extend({
            //         render: function (self) {
            //             var $field = AJS.$('<input type="date" class="text aui-date-picker" name="date">');
            //             // $field.datePicker({'overrideBrowserDefault': true});
            //             return $field;
            //         }
            //     }),
            //     editView: AJS.RestfulTable.CustomEditView.extend({
            //         render: function (self) {
            //             var $field = AJS.$('<input type="date" class="text aui-date-picker" name="date">');
            //             // $field.datePicker({'overrideBrowserDefault': true});
            //             if (!_.isUndefined(self.value)) {
            //                 $field.val(new Date(self.value).print("%Y-%m-%d"));
            //             }
            //             return $field;
            //         }
            //     }),
            //     readView: AJS.RestfulTable.CustomReadView.extend({
            //         render: function (self) {
            //             // var val = (!_.isUndefined(self.value)) ? new Date(self.value).print("%Y-%m-%d") : undefined;
            //             var val = self.value;
            //             return '<span data-field-name="date">' + (val ? val : '') + '</span>';
            //         }
            //     })
            // }
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
