function pageLoad(){
    console.log("Invoked pageLoad()");
}

function recordList(name){
    var url = "record/list/";
    console.log("Invoked listJS()");
    fetch(url, {method: "GET"}).then(response => {
        return response.json();
    }).then (response => {
        if (response.hasOwnProperty("Error")) {
            alert(JSON.stringify(response));
        } else {
            refreshRecordTable(name, response);
        };
    });
}

function achievementList(name){
    var url = "achievement/list/";
    console.log("Invoked achievementList()");
    fetch(url, {method: "GET"}).then( response => {
        return response.json();
    }).then (response => {
        if (response.hasOwnProperty("Error")) {
            alert(JSON.stringify(response));
        } else {
            refreshAchievementTable(name, response);
        };
    });
}

function refreshRecordTable(name, response){
    removeAllRecordRows(name);
    for (let id in response){
        addTableRecordRow(name, id, response[id]);
    }
}

function refreshAchievementTable(name, response){
    removeAllAchievementRows(name);
    for (let id in response){
        addTableAchievementRow(name, id, response[id]);
    }
}

function removeAllRecordRows(name) {
    var tblObject = document.getElementById(name);
    // Remove any existing rows except for the title row
    tblObject.removeChild(tblObject.getElementsByTagName("tbody")[0]);
    var tblBody = document.createElement("tbody");
    tblObject.appendChild(tblBody);
}

function removeAllAchievementRows(name) {
    var tblObject = document.getElementById(name);
    // Remove any existing rows except for the title row
    tblObject.removeChild(tblObject.getElementsByTagName("tbody")[0]);
    var tblBody = document.createElement("tbody");
    tblObject.appendChild(tblBody);
}

function addTableRecordRow(name, id, jsonElements){
    var tblObject = document.getElementById(name);
    var tblBody = tblObject.getElementsByTagName("tbody")[0];
    var tblRow = tblBody.insertRow();
    tblRow.insertCell(0).innerHTML = id;
    tblRow.insertCell(1).innerHTML = jsonElements["UserId"];
    tblRow.insertCell(2).innerHTML = jsonElements["RecordDate"];
    tblRow.insertCell(3).innerHTML = jsonElements["ChoiceId"];
    tblRow.insertCell(4).innerHTML = jsonElements["ChoiceName"];
}

function addTableAchievementRow(name, id, jsonElements){
    var tblObject = document.getElementById(name);
    var tblBody = tblObject.getElementsByTagName("tbody")[0];
    var tblRow = tblBody.insertRow();
    tblRow.insertCell(0).innerHTML = jsonElements["AchievementDate"];
    tblRow.insertCell(1).innerHTML = jsonElements["AchievementName"];
}