var currentPeriod=0;
var slideStep=0;
var buttonNameText=document.getElementById('viewButton').innerHTML;


var gadgetMyTime = AJS.Gadget({
    baseUrl: "__ATLASSIAN_BASE_URL__",
    useOauth: "/rest/gadget/1.0/currentUser",

    view:{
        onResizeAdjustHeight: true,
        onResizeReload: true,
        enableReload: true,
        template: function(args) {
            var gadget = this;

            gadget.getView().html(args.timeobj.html);
        },
        args: [
            {
                key: "timeobj",
                ajaxOptions: function() {
                    return {
                        url: "/rest/mytime-gadget/1.0/timeobj.json",
                        data: {
                            viewType: buttonNameText,
                            currentPeriod: currentPeriod,
                            slideStep: slideStep
                        }
                    };
                }
            }
        ]
    }

});


function reloadMe(){
    AJS.$( ".gadget" ).remove();

    gadgetMyTime = AJS.Gadget({
        baseUrl: "__ATLASSIAN_BASE_URL__",
        useOauth: "/rest/gadget/1.0/currentUser",
        view:{
            onResizeAdjustHeight: true,
            onResizeReload: true,
            enableReload: true,
            template: function(args) {
                var gadget = this;

                gadget.getView().html(args.timeobj.html);
            },
            args: [
                {
                    key: "timeobj",
                    ajaxOptions: function() {
                        return {
                            url: "/rest/mytime-gadget/1.0/timeobj.json",
                            data: {
                                viewType: buttonNameText,
                                currentPeriod: currentPeriod,
                                slideStep: slideStep
                            }
                        };
                    }
                }
            ]
        }

    });
    gadgetMyTime.showView(true);
};

function slideForward(){
    currentPeriod=0;

    document.getElementById('slideStepHidden').value= parseInt(document.getElementById('slideStepHidden').value)+1;

    slideStep = document.getElementById('slideStepHidden').value;

    reloadMe();

}

function slideBack(){
    currentPeriod=0;

    document.getElementById('slideStepHidden').value= parseInt(document.getElementById('slideStepHidden').value)-1;

    slideStep = document.getElementById('slideStepHidden').value;

    reloadMe();
}

function changeView(){
    if(document.getElementById('viewButton').innerHTML=="Weekly view"){
        document.getElementById('viewButton').innerHTML="Monthly view";
    }else{
        document.getElementById('viewButton').innerHTML="Weekly view"
    }
    buttonNameText=document.getElementById('viewButton').innerHTML;
    currentPeriod=1;
    slideStep = document.getElementById('slideStepHidden').value;
    reloadMe();
    currentPeriod=0;
};


 	
			