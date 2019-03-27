AJS.$(function(){
	//init ADD button
    AJS.$("#add").click(function () {
    	moveSelectItemsFunc("#groupsToAdd", "#groupsToRemove", "add");
    });
    //init REMOVE button
    AJS.$("#remove").click(function () {
    	moveSelectItemsFunc("#groupsToRemove", "#groupsToAdd", "remove");
    });
});

var moveSelectItemsFunc = function (sourceSelect, destSelect, operation) {
	var itemsSelected = AJS.$(sourceSelect).val();
	if (itemsSelected == null){
		AJS.messages.warning("#aui-message-bar",{
			   title: "Please select at least one group to " + operation,
			   fadeout: 'true'
			});
		return;
	}
	AJS.$.post(AJS.contextPath() + "/rest/scn-worklog-plugin/1.0/globalsettings/moveGroup", {operation: operation, groupnames: itemsSelected})
	  .done(function() {
	    AJS.messages.success("#aui-message-bar",{
	        title: "Data is saved",
	        fadeout: 'true'
	     }); 
		  	AJS.$.each(itemsSelected, function(key, value) {
			  AJS.$(sourceSelect + " option[value='" + value + "']").remove();
			  AJS.$(destSelect).append(AJS.$('<option value=' + value + '>' + value + '</option>'));
		  });
	  })
	  .fail(function() {
		  AJS.messages.error("#aui-message-bar",{
			   title: "Unexpected error happen, please try again later",
			   fadeout: 'true'
			});
	  });
};
