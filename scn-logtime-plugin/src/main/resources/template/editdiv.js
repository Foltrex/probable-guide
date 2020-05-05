function showErrorMessage(title, message) {
    AJS.flag({
        type: 'error',
        title: title,
        close: 'auto',
        body: message,
    });
}

function toggle(div_id) {
    var el = document.getElementById(div_id);
    if (el.style.display == 'none') {
        el.style.display = 'block';
    } else {
        el.style.display = 'none';
    }
}

function setPosition(popUpDivVar, event) {
    var popUpDiv1 = document.getElementById(popUpDivVar);
    popUpDiv1.style.top = event.pageY - 150 + 'px';
    popUpDiv1.style.left = window.innerWidth / 2 - 200 + 'px';
}

function blanket_size(popUpDivVar) {
    if (typeof window.innerWidth != 'undefined') {
        viewportheight = window.innerHeight;
    } else {
        viewportheight = document.documentElement.clientHeight;
    }
    if ((viewportheight > document.body.parentNode.scrollHeight) && (viewportheight > document.body.parentNode.clientHeight)) {
        blanket_height = viewportheight;
    } else {
        if (document.body.parentNode.clientHeight > document.body.parentNode.scrollHeight) {
            blanket_height = document.body.parentNode.clientHeight;
        } else {
            blanket_height = document.body.parentNode.scrollHeight;
        }
    }
    var blanket = document.getElementById('blanket');
    blanket.style.height = blanket_height + 'px';
    var popUpDiv1 = document.getElementById(popUpDivVar);
    popUpDiv_height = blanket_height / 2 - 50;//200 is half popup's height
    popUpDiv1.style.top = popUpDiv_height + 'px';

}

function window_pos(popUpDivVar) {
    if (typeof window.innerWidth != 'undefined') {
        viewportwidth = window.innerHeight;
    } else {
        viewportwidth = document.documentElement.clientHeight;
    }
    if ((viewportwidth > document.body.parentNode.scrollWidth) && (viewportwidth > document.body.parentNode.clientWidth)) {
        window_width = viewportwidth;
    } else {
        if (document.body.parentNode.clientWidth > document.body.parentNode.scrollWidth) {
            window_width = document.body.parentNode.clientWidth;
        } else {
            window_width = document.body.parentNode.scrollWidth;
        }
    }
    var popUpDiv1 = document.getElementById(popUpDivVar);
    window_width = window_width / 2 - 200;//200 is half popup's width
    popUpDiv1.style.left = window_width + 'px';
}

function showpopup(windowname, event) {
    blanket_size(windowname);
    window_pos(windowname);
    setPosition(windowname, event)
    toggle('blanket');
    toggle(windowname);
}

function popup(windowname) {
    blanket_size(windowname);
    window_pos(windowname);
    toggle('blanket');
    toggle(windowname);
}

var current_editId = "";
var doubleckicked = false;
var clicked = false;
var closedEdit = false;
const defaultTimeValue = '8h';

function openinplaceSlow(userIdentifier, identifierSE, idetifier, isScn) {
    setTimeout(function () {
        inplace(userIdentifier, identifierSE, idetifier, isScn)
    }, 200)
}

function openinplace(userIdentifier, identifierSE, idetifier, isScn) {
    inplace(userIdentifier, identifierSE, idetifier, isScn)
    /*setTimeout(function() {
        inplace(userIdentifier,identifierSE, idetifier, isScn)
    }, 100)*/
}

function projectAdd(projectTypeSel) {
    document.getElementById(projectTypeSel).style.visibility = "visible";
}

function projectChanged(projectId, issueTdId, worklogTypeSel, userId, wlWlSel) {
    if (projectId == '0') {

        document.getElementById(worklogTypeSel).style.visibility = "hidden";
        document.getElementById(wlWlSel).innerHTML = "";
        document.getElementById(worklogTypeSel).value = '0';
        document.getElementById(issueTdId).innerHTML = "";

        for (var cell in arrayCountAdding[userId]) {
            var st = arrayCountAdding[userId][cell];
            var st1 = st.substring(0, st.indexOf("_"));
            var st2 = st.substring(st.indexOf("_") + 1);
            document.getElementById(st1).innerHTML = "";
            document.getElementById(st2).value = "";
        }
        wlsToSave = [];
    } else {
        document.getElementById(issueTdId).innerHTML = "";
        document.getElementById(wlWlSel).innerHTML = "WL/WL*";
        document.getElementById(worklogTypeSel).style.visibility = "visible";
        var url = "/rest/logtime-gadget/1.0/getIssues.json";
        AJS.$.ajax({
            url: url,
            type: "GET",
            data: ({projectId: projectId}),
            dataType: "json",
            success: function (msg) {
                var issueSelectId = "issueSelect" + userId;
                var valueissue = "<select class=\"task-select\" id=\"" + issueSelectId + "\" onChange=\"issueChanged(" + userId + ")\">";
                var iss = msg.issues;
                var issId = msg.issueIds;
                for (var i in iss) {
                    valueissue = valueissue + "<option value=\"" + issId[i] + "\" >" + iss[i] + "</option>";
                }
                valueissue = valueissue + "</select>";

                document.getElementById(issueTdId).innerHTML = valueissue;

            }
        });
    }
}

function issueChanged(userId) {
    var issueId = document.getElementById("issueSelect" + userId).value;
    if (issueId == '0') {

        document.getElementById(issueId).innerHTML = "";

        for (var cell in arrayCountAdding[userId]) {
            var st = arrayCountAdding[userId][cell];
            var st1 = st.substring(0, st.indexOf("_"));
            var st2 = st.substring(st.indexOf("_") + 1);
            document.getElementById(st1).innerHTML = "";
            document.getElementById(st2).value = "";
        }
        wlsToSave = [];
    }
}

function inplace(userIdentifier, identifierSE, idetifier, isScn) {
    if (closedEdit) {
        closedEdit = false;
        return;
    }
    if (doubleckicked) return;
    clicked = true;
    if (current_editId == idetifier) {
        return;
    }
    if (current_editId != "") {
        let val = document.getElementById(current_editId + "_inplace").value;
        document.getElementById('_' + current_editId).innerHTML = val;
    }
    current_editId = idetifier;
    let value = document.getElementById('_' + idetifier).innerHTML;
    document.getElementById('_' + idetifier).innerHTML = "<table><tr><td class=\"editble-input-td\"><input class=\"editble-input\" type=\"text\" size=\"5px\" onkeydown=\"customKeyUp(event,\'" + isScn + "\',\'" + userIdentifier + "\',\'" + identifierSE + "\',\'" + value + "\',\'" + idetifier + "\')\" onChange=\"onFunctionLost(\'" + isScn + "\',\'" + userIdentifier + "\',\'" + identifierSE + "\')\" name=\"t\" id=\"" + idetifier + "_inplace\" value=\"" + value + "\" autofocus></td><td class=\"editble-button\"><button id=\"closebtn\" onClick=\"closeInplace(\'" + value + "\',\'" + idetifier + "\')\" class=\"close-small-btn\"/></td></tr></table> ";
    setTimeout(function () {
        document.getElementById(current_editId + "_inplace").select();
    }, 50);

    closedEdit = false;
}

var timepattern1 = /^\s*([0-9]*[0-9]):[0-5][0-9]\s*$/;
var timepattern2 = /^\s*([0-9]*[0-9]w)*\s*([0-9]*[0-9]d)*\s*(([0-9]*[0-9])h)?\s*([0-5]*[0-9]m)?\s*$/;
var timepattern3 = /^\s*([0-9]*)\s*$/;
var timepattern4 = /^\s*([1-9]*[0-9]*)\s*$/;

function closeInplace(val, idetifier) {
    document.getElementById('_' + idetifier).innerHTML = val;
    current_editId = "";
    clicked = false;
    closedEdit = true;
}

function onFunctionLost(isScn, userIdentifier, identifierSE) {
    setTimeout(function () {
        onFunctionLost1(isScn, userIdentifier, identifierSE)
    }, 200)
}

function onFunctionLost1(isScn, userIdentifier, identifierSE) {
    if (closedEdit) {
        closedEdit = false;
        return;
    }
    if (document.getElementById(current_editId + "_inplace") == null) {
        closedEdit = false;
        return;
    }
    var val = document.getElementById(current_editId + "_inplace").value;
    var isValidated = false;
    if (timepattern1.test(val) || timepattern2.test(val) || timepattern3.test(val)) {
        isValidated = true;
    } else {
        isValidated = false;
        document.getElementById('_' + current_editId).style.color = "red";
        closeInplace(val, current_editId);
        return;
    }

    if (isValidated == true) {
        document.getElementById('_' + current_editId).innerHTML = correctTime(val);
        if (isScn != 'true') {
            if (document.getElementById(arrayCountS[userIdentifier][identifierSE]).innerHTML != "") {
                var complexId2 = arrayCount[arrayCountS[userIdentifier][identifierSE].substring(1)];
            } else {
                var complexId2 = "";
            }
        } else {
            var complexId2 = arrayCount[arrayCountE[userIdentifier][identifierSE].substring(1)];
        }
        var url = (isScn == 'true' ? "/rest/logtime-gadget/1.0/updateScnWorklog.json" : "/rest/logtime-gadget/1.0/updateExtWorklog.json");
        var cur = current_editId;
        AJS.$.ajax({
            url: url,
            type: "GET",
            data: ({
                complexWLId: arrayCount[current_editId],
                complexId2: complexId2,
                newValue: val,
                newWLType: 'undefined',
                comment: 'undefined'
            }),
            dataType: "json",
            success: function (msg) {
                if (isScn == 'true') {
                    document.getElementById(arrayCountS[userIdentifier][identifierSE]).style.color = "green";
                } else {
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).style.color = "green";
                }

                if (msg.copied && identifierSE != '-1') {
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).innerHTML = correctTime(val);
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).style.color = "green";
                    if (msg.wlIdExt != arrayCount[arrayCountE[userIdentifier][identifierSE].substring(1)]) {
                        arrayCount[arrayCountE[userIdentifier][identifierSE].substring(1)] = msg.wlIdExt;
                    }
                }

                if (msg.wlId != arrayCount[cur]) {
                    arrayCount[cur] = msg.wlId;
                    cur = "";
                }
            },
            error: function ($xhr) {
                if ($xhr.status === 403) {
                    const data = JSON.parse($xhr.responseText);
                    showErrorMessage(data.reason, data.message);
                }
            }
        });
    }
    current_editId = "";
    clicked = false;
}

function addWorklog(event, userCount, identifier, addWlComment, dateWeek, userKey) {
    //when the addCells are clicked to add new worklogs
    if (clicked) return;
    doubleckicked = true;
    if (current_editId == identifier) {
        return;
    }
    current_editId = identifier;

    document.getElementById("pop_edit_worklog_id").value = identifier;

    let editableCellId = document.getElementById("pop_edit_worklog_id").value;
    let timeSpent = document.getElementById(editableCellId).innerHTML;
    document.getElementById("pop_time").value = (timeSpent && timeSpent !== 'h') ? timeSpent : defaultTimeValue;
    document.getElementById("popupUpdateButton").innerText = (timeSpent && timeSpent !== 'h') ? 'Update' : 'Create';

    setTimeout(function () {
        document.getElementById("pop_time").select();
    }, 50);

    document.getElementById("textpopUp").value = document.getElementById(addWlComment).value;

    document.getElementById("commentId").value = addWlComment;
    document.getElementById("dateWeekId").value = dateWeek;
    document.getElementById("userKeyId").value = userKey;


    document.getElementById('worklogTypeSelect').value = document.getElementById('worklogTypeSel' + userCount).value;

    document.getElementById('pop_user_identifier').value = userCount;


    document.getElementById('add_wl').value = '1';

    document.getElementById('worklogTypeSelect').disabled = true;
    //set also WorklogTime and add comment
    showpopup('popUpWorklogDiv', event);
}

function updateWorklog(event, selCount, identifier, isExternal, timeSpent, wltypeId, comment, userIdentifier, identifierSE) {
    setTimeout(function () {
        updateWorklog1(event, selCount, identifier, isExternal, timeSpent, wltypeId, comment, userIdentifier, identifierSE)
    }, 200)
}

function updateWorklog1(event, selCount, identifier, isExternal, timeSpent, wltypeId, comment, userIdentifier, identifierSE) {
    //alert(event.clientX + "  " + event.clientY);

    if (closedEdit) {
        closedEdit = false;
        return;
    }
    if (clicked) return;
    doubleckicked = true;
    if (current_editId == selCount) {
        return;
    }
    current_editId = selCount;

    document.getElementById("pop_edit_worklog_id").value = identifier;

    if (isExternal != 'true') {
        timeSpent = document.getElementById(arrayCountS[userIdentifier][identifierSE]).innerHTML;
    } else {
        timeSpent = document.getElementById(arrayCountE[userIdentifier][identifierSE]).innerHTML;
    }

    // get value from table
    document.getElementById("pop_time").value = (timeSpent && timeSpent !== 'h') ? timeSpent : defaultTimeValue;
    document.getElementById("popupUpdateButton").innerText = (timeSpent && timeSpent !== 'h') ? 'Update' : 'Create';

    setTimeout(function () {
        document.getElementById("pop_time").select();
    }, 50);
    //document.getElementById("textpopUp").value = comment;
    document.getElementById("textpopUp").value = arrayCountComments[selCount];
    document.getElementById('worklogTypeSelect').value = wltypeId;

    document.getElementById('pop_worklog_scn_ext').value = isExternal;
    document.getElementById('pop_user_identifier').value = userIdentifier;
    document.getElementById('pop_identifier_SE').value = identifierSE;
    document.getElementById('add_wl').value = '0';
    document.getElementById('worklogTypeSelect').disabled = false;
    //set also WorklogTime and add comment
    showpopup('popUpWorklogDiv', event);
}

function addUpdateWorklog() {
    if (document.getElementById('add_wl').value == '0') {
        updateWorklogAJAX();
    } else {
        addWorklogAJAX();
    }
}

var wlsToSave = [];

function addWorklogAJAX() {
    //pressing update button on PopUp for adding new task
    var time = document.getElementById("pop_time").value;

    var isValidated = false;
    if (timepattern1.test(time) || timepattern2.test(time) || timepattern3.test(time)) {
        isValidated = true;

    } else {
        isValidated = false;
    }

    if (isValidated) {
        var editableCellId = document.getElementById("pop_edit_worklog_id").value;

        document.getElementById(editableCellId).innerHTML = document.getElementById("pop_time").value;
        document.getElementById(document.getElementById("commentId").value).value = document.getElementById("textpopUp").value;

        wlsToSave.push(document.getElementById("pop_time").value + "_" + document.getElementById("textpopUp").value + '_' + document.getElementById("dateWeekId").value + '_' + document.getElementById("userKeyId").value + '_' + document.getElementById('worklogTypeSelect').value);

        doubleckicked = false;
        document.getElementById('worklogTypeSelect').disabled = false;
        cancelWorklogAJAX();
    } else {
        alert("Please enter time in correct XX:XX format or XXd XXh XXm");
    }
}

function addWorklogsToDb() {

    var url = "/rest/logtime-gadget/1.0/updateScnWorklogs.json";

    AJS.$.ajax({
        url: url,
        type: "GET",
        data: ({
            wlsToSave: wlsToSave,
            issueId: document.getElementById("issueSelect" + document.getElementById('pop_user_identifier').value).value
        }),
        dataType: "json",
        success: function (msg) {
            checkProjects(msg.message);
            reloadMeClean();
        },
        error: function ($xhr) {
            if ($xhr.status === 403) {
                const data = JSON.parse($xhr.responseText);
                showErrorMessage(data.reason, data.message);
            }
        }
    });
}

function updateWorklogAJAX() {
    popup('popUpWorklogDiv');
    var tmpId = document.getElementById("pop_edit_worklog_id").value;
    var tmpTime = document.getElementById("pop_time").value;
    var vlTypeTmp = document.getElementById('worklogTypeSelect').value;
    var e = document.getElementById('worklogTypeSelect');
    var vlTypeText = e.options[e.selectedIndex].text;

    var commentTmp = document.getElementById("textpopUp").value;
    var cur = document.getElementById('current_editId');

    var isExt = document.getElementById('pop_worklog_scn_ext').value;
    var url = (isExt != 'true' ? "/rest/logtime-gadget/1.0/updateScnWorklog.json" : "/rest/logtime-gadget/1.0/updateExtWorklog.json");

    var isValidated = false;

    if (timepattern1.test(tmpTime) || timepattern2.test(tmpTime) || timepattern3.test(tmpTime)) {
        isValidated = true;
    } else {
        isValidated = false;

        var userIdentifier = document.getElementById('pop_user_identifier').value;
        var identifierSE = document.getElementById('pop_identifier_SE').value;
        if (isExt != 'true') {
            document.getElementById(arrayCountS[userIdentifier][identifierSE]).innerHTML = tmpTime;
            document.getElementById(arrayCountS[userIdentifier][identifierSE]).style.color = "red";
        } else {
            document.getElementById(arrayCountE[userIdentifier][identifierSE]).innerHTML = tmpTime;
            document.getElementById(arrayCountE[userIdentifier][identifierSE]).style.color = "red"
        }
        current_editId = "";
    }

    if (isValidated) {
        var userIdentifier = document.getElementById('pop_user_identifier').value;
        var identifierSE = document.getElementById('pop_identifier_SE').value;
        var complexId2;
        if (isExt == 'true') {
            complexId2 = "";
        } else {
            complexId2 = arrayCount[arrayCountE[userIdentifier][identifierSE].substring(1)];
        }

        AJS.$.ajax({
            url: url,
            type: "GET",
            data: ({
                complexWLId: arrayCount[current_editId],
                complexId2: complexId2,
                newValue: tmpTime,
                newWLType: vlTypeTmp,
                comment: commentTmp
            }),
            dataType: "json",
            success: function (msg) {
                arrayCountComments[current_editId] = replaceSymbols(commentTmp);
                //arrayCountComments[current_editId]=commentTmp;
                //alert(arrayCountComments[current_editId]);
                if (isExt != 'true') {
                    document.getElementById(arrayCountS[userIdentifier][identifierSE]).innerHTML = correctTime(tmpTime);
                    document.getElementById(arrayCountS[userIdentifier][identifierSE]).style.color = "green";
                    document.getElementById(arrayCountS[userIdentifier][identifierSE]).title = vlTypeText + ' ' + arrayCountComments[current_editId];
                } else {
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).innerHTML = tmpTime;
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).style.color = "green"
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).title = vlTypeText + ' ' + arrayCountComments[current_editId];
                }
                if (msg.copied) {
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).innerHTML = tmpTime;
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).style.color = "green";
                    arrayCountComments[arrayCountE[userIdentifier][identifierSE].substring(1)] = replaceSymbols(commentTmp);
                    document.getElementById(arrayCountE[userIdentifier][identifierSE]).title = vlTypeText + ' ' + arrayCountComments[current_editId];
                    if (msg.wlIdExt != arrayCount[arrayCountE[userIdentifier][identifierSE].substring(1)]) {
                        arrayCount[arrayCountE[userIdentifier][identifierSE].substring(1)] = msg.wlIdExt;
                    }
                }

                if (msg.wlId != arrayCount[current_editId]) {
                    arrayCount[current_editId] = msg.wlId;
                }
                current_editId = "";
            },
            error: function ($xhr) {
                if ($xhr.status === 403) {
                    const data = JSON.parse($xhr.responseText);
                    showErrorMessage(data.reason, data.message);
                    current_editId = "";
                }
            }
        });
    }
    doubleckicked = false;
}

function correctTime(tmpTime) {
    if (timepattern4.test(tmpTime)) {
        return tmpTime + 'h';
    }
    return tmpTime;
}

function replaceSymbols(str) {

    //str = str.replace(new RegExp("\'",'g'),"").replace(new RegExp("\"",'g'),"").replace(new RegExp("\<",'g'),"").replace(new RegExp("\>",'g'),"");
    str = str.replace(/\'/g, ' ').replace(/\"/g, ' ').replace(/\</g, ' ').replace(/\>/g, ' ');
    var pattern = /\r\n|\r|\n/g;
    str = str.replace(pattern, " ");

    return str;
}

function cancelWorklogAJAX() {
    popup('popUpWorklogDiv');
    doubleckicked = false;
    current_editId = "";
}
