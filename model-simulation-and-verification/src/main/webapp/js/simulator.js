var xmlData = null;
var fileContent = null;
var msgSourceWindow = null;
var modelId = null;

//Fix for IE (IE do not support 'startsWith') 
if (!String.prototype.startsWith) 
{
  String.prototype.startsWith = function(searchString, position)
  {
    position = position || 0;
    return this.substr(position, searchString.length) === searchString;
  };
}

function loadModelSelect() {
    $('#model_generalRes_select').empty();
    var modelIdList = xmlData.find('SimulationResults > PathAnalysis');
    for (var i = 0; i < modelIdList.length; i++) {
        $('#model_generalRes_select').append($('<option>', {
            value : modelIdList[i].getAttribute('modelId'),
            text : modelIdList[i].getAttribute('modelName')
        }));
        if(i==0){
            $('#model_generalRes_select').val(modelIdList[i].getAttribute('modelId'));
            modelId = modelIdList[i].getAttribute('modelId');
        }
    }
}

function onModelSelectChange() {
    modelId = $('#model_generalRes_select').find(":selected").val();
    
    loadCharts();
}



function loadActivitiesSelect() {
    $('#activityRes_select').empty();
    var objectList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > ObjectMeasures > Object ');
    for (var i = 0; i < objectList.length; i++) {
        $('#activityRes_select').append($('<option>', {
            value : objectList[i].getAttribute('id'),
            text : objectList[i].getAttribute('name')
        }));
        if(i==0){
            $('#activityRes_select').val(objectList[i].getAttribute('id'));
        }
    }
    onActivitiesSelectChange();
}

function onActivitiesSelectChange() {
    var objectId = $('#activityRes_select').find(":selected").val();
    
    var object = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > ObjectMeasures > Object[id="'+objectId+'"]')[0];
    try {
        $("#numExec_activityRes_lbl").text(object.getAttribute('numberOfExecutions'));
        $("#costs_activityRes_lbl").text(object.getAttribute('costs'));
        $("#totCosts_activityRes_lbl").text(object.getAttribute('totCosts'));
        $("#execTime_activityRes_lbl").text(object.getAttribute('executionTime'));
        $("#totExecTime_activityRes_lbl").text(object.getAttribute('totExecutionTime'));
    } catch(e){}
}

function loadDeadlockPathSelect() {
    $('#totDeadP_generalRes_select').empty();
    var pathList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Paths > Path[deadlocked="true"]');
    for (var i = 0; i < pathList.length; i++) {

        $('#totDeadP_generalRes_select').append($('<option>', {
            value : pathList[i].getAttribute('id'),
            text : pathList[i].getAttribute('id')
        }));
    }
}

function loadLivelockPathSelect() {
    $('#totLiveP_generalRes_select').empty();
    var pathList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Paths > Path[livelocked="true"]');
    for (var i = 0; i < pathList.length; i++) {

        $('#totLiveP_generalRes_select').append($('<option>', {
            value : pathList[i].getAttribute('id'),
            text : pathList[i].getAttribute('id')
        }));
    }
}

function showActivity() {
    var objectId = $('#activityRes_select').find(":selected").val();
    var jsonActivityInfo = generateJsonActivity(objectId);
    if(msgSourceWindow == null) {
        alert("Activity:\n"+JSON.stringify(jsonActivityInfo, null, 2));
    } else {
        msgSourceWindow.postMessage(jsonActivityInfo, "*");
    }
}
function generateJsonActivity(objectId) {
    return {
        command: "show_activity",
        modelId: modelId,
        objList: [[objectId]]
    };
}


function loadGeneralResults() {
    var generalMeasuresNode = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > GeneralMeasures');

    $("#avgCost_generalRes_lbl").text(generalMeasuresNode.find('AverageCosts').text());
    $("#maxCost_generalRes_lbl").text(generalMeasuresNode.find('MaxCosts').text());
    $("#maxCostTrace_generalRes_lbl").text('Trace: ' + generalMeasuresNode.find('MaxCosts')[0].getAttribute("traceId"));
    $("#minCost_generalRes_lbl").text(generalMeasuresNode.find('MinCosts').text());
    $("#minCostTrace_generalRes_lbl").text('Trace: ' + generalMeasuresNode.find('MinCosts')[0].getAttribute("traceId"));
    $("#totCost_generalRes_lbl").text(generalMeasuresNode.find('TotalCosts').text());
    $("#dailyExec_generalRes_lbl").text(generalMeasuresNode.find('AverageExecutionsInOneDay').text());
    $("#avgExecTime_generalRes_lbl").text(generalMeasuresNode.find('AverageExecutionTime').text());
    $("#maxExecTime_generalRes_lbl").text(generalMeasuresNode.find('MaxExecutionTime').text());
    $("#maxExecTimeTrace_generalRes_lbl").text('Trace: ' + generalMeasuresNode.find('MaxExecutionTime')[0].getAttribute("traceId"));
    $("#minExecTime_generalRes_lbl").text(generalMeasuresNode.find('MinExecutionTime').text());
    $("#minExecTimeTrace_generalRes_lbl").text('Trace: ' + generalMeasuresNode.find('MinExecutionTime')[0].getAttribute("traceId"));
    $("#totExecTime_generalRes_lbl").text(generalMeasuresNode.find('TotalExecutionTime').text());
    $("#maxWaitTime_generalRes_lbl").text(generalMeasuresNode.find('MaxWaitingTime').text());
    $("#maxWaitTimeTrace_generalRes_lbl").text('Trace: ' + generalMeasuresNode.find('MaxWaitingTime')[0].getAttribute("traceId"));
    $("#maxMsgWaitTime_generalRes_lbl").text(generalMeasuresNode.find('MaxMessageWaitingTime').text());
    $("#maxMsgWaitTimeTrace_generalRes_lbl").text('Trace: ' + generalMeasuresNode.find('MaxMessageWaitingTime')[0].getAttribute("traceId"));
    $("#minWaitTime_generalRes_lbl").text(generalMeasuresNode.find('MinWaitingTime').text());
    $("#minWaitTimeTrace_generalRes_lbl").text('Trace: ' + generalMeasuresNode.find('MinWaitingTime')[0].getAttribute("traceId"));
    $("#minMsgWaitTime_generalRes_lbl").text(generalMeasuresNode.find('MinMessageWaitingTime').text());
    $("#minMsgWaitTimeTrace_generalRes_lbl").text('Trace: ' + generalMeasuresNode.find('MinMessageWaitingTime')[0].getAttribute("traceId"));
    $("#totDeadT_generalRes_lbl").text(generalMeasuresNode.find('TotalDeadlockedTraces').text());
    $("#totDeadP_generalRes_lbl").text(generalMeasuresNode.find('TotalDeadlockedPaths').text());
    $("#totLiveT_generalRes_lbl").text(generalMeasuresNode.find('TotalLivelockedTraces').text());
    $("#totLiveP_generalRes_lbl").text(generalMeasuresNode.find('TotalLivelockedPaths').text());
    $("#totTerm_generalRes_lbl").text(generalMeasuresNode.find('TotalEndTerminateEvents').text());
    $("#totRuns_generalRes_lbl").text(generalMeasuresNode.find('TotalRuns').text());
    $("#totTraces_generalRes_lbl").text(generalMeasuresNode.find('TotalTraces').text());
    $("#totPaths_generalRes_lbl").text(generalMeasuresNode.find('TotalPaths').text());

    loadDeadlockPathSelect();
    loadLivelockPathSelect();
}

function onDeadPathSelectChange() {
    var pathName = $('#totDeadP_generalRes_select').find(":selected").text();
    loadPathInfos(pathName);
    $('#pathListSelect').val(pathName);
}

function onLivePathSelectChange() {
    var pathName = $('#totLiveP_generalRes_select').find(":selected").text();
    loadPathInfos(pathName);
    $('#pathListSelect').val(pathName);
}

function changeTrace(evt) {
    var traceId = $(evt.target).text()
    if (traceId.indexOf(":") != -1)
        traceId = traceId.substring(7);
    if (traceId == "")
        return;
    $('#traceListSelect').val(traceId);
    onTraceSelectChange();
}

function changePath(evt) {
    var pathId = $(evt.target).text();
    if (pathId == "")
        return;
    $('#pathListSelect').val(pathId);
    onPathSelectChange();
}







var lastPathProbabilitiesLabel = null;
function onOverPathProbabilities(evt) {
    var activePoints = chartPathProbabilities.getSegmentsAtEvent(evt);
    if (activePoints.length == 0)
        return;

    var pathName = activePoints[0].label;

    if (lastPathProbabilitiesLabel != null && pathName == lastPathProbabilitiesLabel)
        return;
    lastPathProbabilitiesLabel = pathName;

    // the following code is executed only on selection change
    $('#pathListSelect').val(pathName);
    loadPathInfos(pathName);
};

function onPathSelectChange() {
    var pathName = $('#pathListSelect').find(":selected").text();
    loadPathInfos(pathName);
}

function loadPathInfos(pathId) {
    var pathNode = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Paths > Path[id="' + pathId + '"]');
    if (pathNode.length != 1) {
        alert("Error retriving the Path with id: " + pathId);
        return;
    }
    pathNode = pathNode[0];

    $("#name_pathProb_lbl").text(pathId);
    $("#prob_pathProb_lbl").text(pathNode.getAttribute("pathProbability") + '%');
    $("#numTraces_pathProb_lbl").text(pathNode.getAttribute("numberOfTraces"));
    $("#isDead_pathProb_lbl").text(pathNode.getAttribute("deadlocked"));
    $("#isLive_pathProb_lbl").text(pathNode.getAttribute("livelocked"));
    $("#isTermin_pathProb_lbl").text(pathNode.getAttribute("terminateEvent"));

    $("#minwt_pathProb_lbl").text(pathNode.getAttribute("minWaitingTime"));
    $("#minsdwt_pathProb_lbl").text(pathNode.getAttribute("minWaitingTimeSD"));
    $("#minwtt_pathProb_lbl").text(pathNode.getAttribute("minWaitingTimeTraceId"));
    $("#minmsgwt_pathProb_lbl").text(pathNode.getAttribute("minMsgWaitingTime"));
    $("#minmsgsdwt_pathProb_lbl").text(pathNode.getAttribute("minMsgWaitingTimeSD"));
    $("#minmsgwtt_pathProb_lbl").text(pathNode.getAttribute("minMsgWaitingTimeTraceId"));
}

var chartPathProbabilities = null;
function generatePathProbabilitiesPie() {

    if (chartPathProbabilities != null)
        chartPathProbabilities.destroy();

    $('#pathListSelect').empty();

    var chartPathProbabilitiesData = [];
    var chartPathProbabilitiesUsedColorList = [];
    var pathList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Paths > Path');

    for (var i = 0; i < pathList.length; i++) {
        var color = randomColorNotIn(chartPathProbabilitiesUsedColorList);
        chartPathProbabilitiesUsedColorList.push(color);
        // var color = getPathColorMap(pathList.length)[i];

        chartPathProbabilitiesData.push({
            value : pathList[i].getAttribute('pathProbability'),
            label : pathList[i].getAttribute('id'),
            color : color,
            highlight : '#E2E2E2'
        });

        $('#pathListSelect').append($('<option>', {
            value : pathList[i].getAttribute('id'),
            text : pathList[i].getAttribute('id')
        }));
    }

    chartPathProbabilities = new Chart($("#canvasPathProbabilities").get(0).getContext("2d")).Doughnut(chartPathProbabilitiesData, {});

    onPathSelectChange();
}

function showPath() {
    var pathName = $('#pathListSelect').find(":selected").val();
    var jsonPathInfo = generateJsonPath(pathName);
    if(msgSourceWindow == null) {
        alert("Path Elements:\n"+JSON.stringify(jsonPathInfo, null, 2));
    } else {
        msgSourceWindow.postMessage(jsonPathInfo, "*");
    }
}
function generateJsonPath(pathId) {
    var tmpObjList = [];
    var xmlObjList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Paths > Path[id="' + pathId + '"] > Set > Object').each(function(){
        var objId = $(this).text();
        var step = $(this).attr('step');
        tmpObjList.push([objId, step]);
    });
    
    var objList = [];
    var finished = false;
    var counter = 1;
    do{
        var parallelObjList = [];
        for(var i=0;i<tmpObjList.length;i++)
            if(tmpObjList[i][1] == counter)
                parallelObjList.push(tmpObjList[i][0]);
        counter++;
        if(parallelObjList.length == 0)
            finished = true;
        else
            objList.push(parallelObjList);
    }while(!finished);
    
    return {
        command: "show_path",
        modelId: modelId,
        objList: objList
    };
}









var lastTraceProbabilitiesLabel = null;
function onOverTraceProbabilities(evt) {
    var activePoints = chartTraceProbabilities.getSegmentsAtEvent(evt);
    if (activePoints.length == 0)
        return;

    var traceName = activePoints[0].label;

    if (lastTraceProbabilitiesLabel != null && traceName == lastTraceProbabilitiesLabel)
        return;
    lastTraceProbabilitiesLabel = traceName;

    // the following code is executed only on selection change
    $('#traceListSelect').val(traceName);
    loadTraceInfos(traceName);
};

function onTraceSelectChange() {
    var traceName = $('#traceListSelect').find(":selected").text();
    loadTraceInfos(traceName);
}

function loadTraceInfos(traceId) {
    var traceNodeList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Traces > Trace[id="' + traceId + '"]');
    if (traceNodeList.length != 1) {
        alert("Error retriving the Trace with id: " + traceId);
        return;
    }
    traceNode = traceNodeList[0];

    $("#name_traceProb_lbl").text(traceId);
    $("#prob_traceProb_lbl").text(traceNode.getAttribute("traceProbability") + '%');
    $("#execTime_traceProb_lbl").text(traceNode.getAttribute("executionTime"));
    $("#costs_traceProb_lbl").text(traceNode.getAttribute("costs"));
    $("#path_traceProb_lbl").text(traceNode.getAttribute("pathId"));
    $("#twt_traceProb_lbl").text(traceNodeList.find("WaitingTimes")[0].getAttribute("total"));
    $("#mwt_traceProb_lbl").text(traceNodeList.find("WaitingTimes")[0].getAttribute("totalOnMessages"));
    $("#sdtwt_traceProb_lbl").text(traceNodeList.find("WaitingTimes")[0].getAttribute("standardDeviation"));
    $("#sdmwt_traceProb_lbl").text(traceNodeList.find("WaitingTimes")[0].getAttribute("standardDeviationOnMessages"));
}



var chartTraceProbabilities = null;
function generateTraceProbabilitiesPie() {

    if (chartTraceProbabilities != null)
        chartTraceProbabilities.destroy();

    $('#traceListSelect').empty();

    var chartTraceProbabilitiesData = [];
    var chartTraceProbabilitiesUsedColorList = [];
    var traceList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Traces > Trace');

    for (var i = 0; i < traceList.length; i++) {
        var color = randomColorNotIn(chartTraceProbabilitiesUsedColorList);
        chartTraceProbabilitiesUsedColorList.push(color);

        chartTraceProbabilitiesData.push({
            value : traceList[i].getAttribute('traceProbability'),
            label : traceList[i].getAttribute('id'),
            color : color,
            highlight : '#E2E2E2'
        });

        $('#traceListSelect').append($('<option>', {
            value : traceList[i].getAttribute('id'),
            text : traceList[i].getAttribute('id')
        }));
    }

    chartTraceProbabilities = new Chart($("#canvasTraceProbabilities").get(0).getContext("2d")).Pie(chartTraceProbabilitiesData, {});

    onTraceSelectChange();
}

function showTrace() {
    var traceName = $('#traceListSelect').find(":selected").val();
    var jsonTraceInfo = generateJsonTrace(traceName);
    if(msgSourceWindow == null) {
        alert("Trace Elements:\n"+JSON.stringify(jsonTraceInfo, null, 2));
    } else {
        msgSourceWindow.postMessage(jsonTraceInfo, "*");
    }
}
function generateJsonTrace(traceId) {
    var objList = [];
    var xmlObjList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Traces > Trace[id="' + traceId + '"] > Steps > Object').each(function(){
        var objId = $(this).text();
        objList.push([objId]);
    });

    return {
        command: "show_trace",
        modelId: modelId,
        objList: objList
    };
}








var chartPathsExecTime = null;
function generatePathsExecTimeInstagram() {
    if (chartPathsExecTime != null)
        chartPathsExecTime.destroy();

    var chartPathsExecTimeData = {
        labels : [],
        datasets : [ {
            label : "Path / Execution Times (h)",
            fillColor : "#CCCCCC",
            strokeColor : "#CCCCCC",
            highlightFill : "#E2E2E2",
            highlightStroke : "#E2E2E2",
            data : []
        } ]
    };

    var traceList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Traces > Trace');

    for (var i = 0; i < traceList.length; i++) {
        var pathId = traceList[i].getAttribute('pathId');
        var execTime = traceList[i].getAttribute('executionTime');

        if ($.inArray(pathId, chartPathsExecTimeData.labels) == -1) {
            chartPathsExecTimeData.labels.push(pathId);
            chartPathsExecTimeData.datasets[0].data.push(adoxxTimeStringToHours(execTime));
        }
    }

    chartPathsExecTime = new Chart($("#canvasPathExecTime").get(0).getContext("2d")).Bar(chartPathsExecTimeData, {});
}

var lastPathExecTimeLabel = null;
function onOverPathExecTime(evt) {
    var activePoints = chartPathsExecTime.getBarsAtEvent(evt);
    if (activePoints.length == 0)
        return;

    var pathName = activePoints[0].label;

    if (lastPathExecTimeLabel != null && pathName == lastPathExecTimeLabel)
        return;
    lastPathExecTimeLabel = pathName;

    // the following code is executed only on selection change
    $('#pathListSelect').val(pathName);
    loadPathInfos(pathName);
};








var chartPathsCosts = null;
function generatePathsCostsInstagram() {
    if (chartPathsCosts != null)
        chartPathsCosts.destroy();

    var chartPathsCostsData = {
        labels : [],
        datasets : [ {
            label : "Path / Costs",
            fillColor : "#CCCCCC",
            strokeColor : "#CCCCCC",
            highlightFill : "#E2E2E2",
            highlightStroke : "#E2E2E2",
            data : []
        } ]
    };

    var traceList = xmlData.find('SimulationResults > PathAnalysis[modelId="'+modelId+'"] > Traces > Trace');

    for (var i = 0; i < traceList.length; i++) {
        var pathId = traceList[i].getAttribute('pathId');
        var costs = traceList[i].getAttribute('costs');

        if ($.inArray(pathId, chartPathsCostsData.labels) == -1) {
            chartPathsCostsData.labels.push(pathId);
            chartPathsCostsData.datasets[0].data.push(costs);
        }
    }

    chartPathsCosts = new Chart($("#canvasPathCosts").get(0).getContext("2d")).Bar(chartPathsCostsData, {});
}

var lastPathCostsLabel = null;
function onOverPathCosts(evt) {
    var activePoints = chartPathsCosts.getBarsAtEvent(evt);
    if (activePoints.length == 0)
        return;

    var pathName = activePoints[0].label;

    if (lastPathCostsLabel != null && pathName == lastPathCostsLabel)
        return;
    lastPathCostsLabel = pathName;

    // the following code is executed only on selection change
    $('#pathListSelect').val(pathName);
    loadPathInfos(pathName);
};







function randomColorNotIn(avoidList) {
    var color = randomColor();
    while ($.inArray(color, avoidList) != -1) {
        color = randomColor();
    }
    return color;
}

function randomColor() {
    return '#' + (0x1000000 + (Math.random()) * 0xffffff).toString(16).substr(1, 6);
}

function adoxxTimeStringToHours(adoxxTime) {
    // YY:DDD:HH:MM:SS
    // 0 1 2 3 4
    var data = adoxxTime.split(":");
    var hours = 0;
    hours += parseInt(data[4]) / 3600;
    hours += parseInt(data[3]) / 60;
    hours += parseInt(data[2]);
    hours += parseInt(data[1]) * 24;
    hours += parseInt(data[0]) * 8760;
    return hours;
}

var formatXml = this.formatXml = function(xml) {
    var reg = /(>)(<)(\/*)/g;
    var wsexp = / *(.*) +\n/g;
    var contexp = /(<.+>)(.+\n)/g;
    xml = xml.replace(reg, '$1\n$2$3').replace(wsexp, '$1\n').replace(contexp, '$1\n$2');
    var pad = 0;
    var formatted = '';
    var lines = xml.split('\n');
    var indent = 0;
    var lastType = 'other';
    var transitions = {
        'single->single' : 0,
        'single->closing' : -1,
        'single->opening' : 0,
        'single->other' : 0,
        'closing->single' : 0,
        'closing->closing' : -1,
        'closing->opening' : 0,
        'closing->other' : 0,
        'opening->single' : 1,
        'opening->closing' : 0,
        'opening->opening' : 1,
        'opening->other' : 1,
        'other->single' : 0,
        'other->closing' : -1,
        'other->opening' : 0,
        'other->other' : 0
    };
    for (var i = 0; i < lines.length; i++) {
        var ln = lines[i];
        var single = Boolean(ln.match(/<.+\/>/));
        var closing = Boolean(ln.match(/<\/.+>/));
        var opening = Boolean(ln.match(/<[^!].*>/));
        var type = single ? 'single' : closing ? 'closing' : opening ? 'opening' : 'other';
        var fromTo = lastType + '->' + type;
        lastType = type;
        var padding = '';
        indent += transitions[fromTo];
        for (var j = 0; j < indent; j++) {
            padding += '    ';
        }
        formatted += padding + ln + '\n';
    }
    return formatted;
};

function getURLParameter(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam)
            return sParameterName[1];
    }
    return null;
}

function getHostname() {
    if (window.location.hostname == '')
        return '127.0.0.1:8080';
    return window.location.hostname + ':' + window.location.port;
}

function getProtocol() {
    if (window.location.protocol == '')
        return 'http:';
    return window.location.protocol;
}

function readSingleFile(e) {
    var file = e.target.files[0];
    if (!file) {
        return;
    }
    $('#labelSpan').text(file.name);
    var reader = new FileReader();
    reader.onload = function(e) {
        fileContent = e.target.result;
        // sessionStorage.setItem('adoxxSimModelContent', e.target.result);
    };
    // reader.readAsArrayBuffer(file);
    reader.readAsText(file);
}

function simulate() {
    var serviceEndpoint = window.location.href.substring(0, window.location.href.split('?')[0].lastIndexOf("/")) +  '/rest/json';

    var endpoint = new URLSearchParams(window.location.search).get('endpoint');
    if (endpoint != null)
        serviceEndpoint = endpoint;
    
    if (fileContent == null)
        return;
    
    //$('#inputModelTxt').html(fileContent);

    $.ajax({
        url : serviceEndpoint,
        type : 'POST',
        data : JSON.stringify({alg:'simulation', params: 'numExecutions=' + $('#numExecutions').val() + '&fullResults='+ $('#fullResultsChk').is(':checked'), modelB64: btoa(fileContent) }),
        dataType : 'text',
        contentType : 'application/json',
        processData : false,
        async : true,
        success : function(data, status) {
            var dataJson = JSON.parse(data);
            if (data.error) {
                alert (data.error);
                return;
            }
            data = atob(dataJson.resultsB64);
            

            if(data.startsWith('<ERROR>')){
                alert(data);
                return;
            }
            $('#graphResults').show();
            $('#simulationResultsTxt').html(formatXml(data));
            xmlData = $(jQuery.parseXML(data));
            loadModelSelect();
            loadCharts();
        },
        error : function(request, status, error) {
            alert('Error: ' + status + ' ' + error);
        }
    });
    
    if(showRawResults() && !endpoint)
        generatePNML();
}


function simulate_old() {
    var serviceEndpoint = window.location.href.substring(0, window.location.href.split('?')[0].lastIndexOf("/"));

    var jsonFormat = new URLSearchParams(window.location.search).get('format') == 'json';
    serviceEndpoint += jsonFormat ? '/rest/json' : '/rest/simulator/pathanalysis?numExecutions=' + $('#numExecutions').val() + '&fullResults=' + $('#fullResultsChk').is(':checked');
    var endpoint = new URLSearchParams(window.location.search).get('endpoint');
    if (endpoint != null)
        serviceEndpoint = endpoint;
    
    if (fileContent == null)
        return;
    
    $('#inputModelTxt').html(fileContent);

    $.ajax({
        url : serviceEndpoint,
        type : 'POST',
        data : jsonFormat ? JSON.stringify({alg:'simulation', params: 'numExecutions=' + $('#numExecutions').val() + '&fullResults='+ $('#fullResultsChk').is(':checked'), modelB64: btoa(fileContent) }) : fileContent,
        dataType : 'text',
        contentType : jsonFormat ? 'application/json' : 'application/xml',
        processData : false,
        async : true,
        success : function(data, status) {
            if(jsonFormat) {
                var dataJson = JSON.parse(data);
                if (data.error) {
                    alert (data.error);
                    return;
                }
                data = atob(dataJson.resultsB64);
            }

            if(data.startsWith('<ERROR>')){
                alert(data);
                return;
            }
            $('#graphResults').show();
            $('#simulationResultsTxt').html(formatXml(data));
            xmlData = $(jQuery.parseXML(data));
            loadModelSelect();
            loadCharts();
        },
        error : function(request, status, error) {
            alert('Error: ' + status + ' ' + error);
        }
    });
    
    if(showRawResults() && !endpoint)
        generatePNML();
}

function generatePNML() {

    var serviceEndpoint = window.location.href.substring(0, window.location.href.split('?')[0].lastIndexOf("/"));

    if (fileContent == null)
        return;

    $.ajax({
        url : serviceEndpoint + '/rest/utils/showpetrinet',
        type : 'POST',
        data : fileContent,
        dataType : 'text',
        contentType : 'application/xml',
        processData : false,
        async : true,
        success : function(data, status) {

            if(data.startsWith('<ERROR>')){
                alert(data);
                return;
            }
            
            $('#petrinetTxt').html(data);
        },
        error : function(request, status, error) {
            alert('Error: ' + status + ' ' + error);
        }
    });
}

function loadCharts(){
    
    loadGeneralResults();
    loadActivitiesSelect();
    generatePathProbabilitiesPie();
    generateTraceProbabilitiesPie();
    generatePathsExecTimeInstagram();
    generatePathsCostsInstagram();
}

function onMessageReceived(event) {
    var origin = event.origin || event.originalEvent.origin;
    fileContent = event.data;
    msgSourceWindow = event.source;
}

//window.addEventListener("message", onMessageReceived, false);
window.onmessage = onMessageReceived;

function skipModelSelection() {
    return getURLParameter("skipModelSelection") == 'true' || getURLParameter("modelURL") != null;
}

function showRawResults() {
    return getURLParameter("debug") == 'true';
}

function processImportModel () {
  var modelURL = getURLParameter("modelURL");
  if (modelURL == null || modelURL == '')
    return;
  $.get(decodeURI(modelURL), function (data) {
    fileContent = data;
  }, 'text');
}

$(document).ready(function() {

    if (!skipModelSelection()) {
        if (window.File && window.FileReader && window.FileList && window.Blob) {
            document.getElementById('file-input').addEventListener('change', readSingleFile, false);
        } else {
            alert('The File APIs are not fully supported in this browser.');
        }
    } else {
        $('#inputDiv').hide();
    }
    
    processImportModel();
    

    $('#rawResults').hide();
    $('#graphResults').hide();
    
    if(showRawResults()){
        $('#rawResults').show();
        $('#graphResults').show();
    }

    document.getElementById('model_generalRes_select').addEventListener('change', onModelSelectChange);
    
    document.getElementById('activityRes_select').addEventListener('change', onActivitiesSelectChange);
    
    document.getElementById('canvasPathProbabilities').addEventListener('mousemove', onOverPathProbabilities);
    document.getElementById('pathListSelect').addEventListener('change', onPathSelectChange);

    document.getElementById('canvasTraceProbabilities').addEventListener('mousemove', onOverTraceProbabilities);
    document.getElementById('traceListSelect').addEventListener('change', onTraceSelectChange);

    document.getElementById('canvasPathExecTime').addEventListener('mousemove', onOverPathExecTime);
    document.getElementById('canvasPathCosts').addEventListener('mousemove', onOverPathCosts);

    document.getElementById('totDeadP_generalRes_select').addEventListener('change', onDeadPathSelectChange);
    document.getElementById('totLiveP_generalRes_select').addEventListener('change', onLivePathSelectChange);
    document.getElementById('maxCostTrace_generalRes_lbl').addEventListener('click', changeTrace);
    document.getElementById('minCostTrace_generalRes_lbl').addEventListener('click', changeTrace);
    document.getElementById('maxExecTimeTrace_generalRes_lbl').addEventListener('click', changeTrace);
    document.getElementById('minExecTimeTrace_generalRes_lbl').addEventListener('click', changeTrace);
    document.getElementById('maxWaitTimeTrace_generalRes_lbl').addEventListener('click', changeTrace);
    document.getElementById('maxMsgWaitTimeTrace_generalRes_lbl').addEventListener('click', changeTrace);
    document.getElementById('minWaitTimeTrace_generalRes_lbl').addEventListener('click', changeTrace);
    document.getElementById('minMsgWaitTimeTrace_generalRes_lbl').addEventListener('click', changeTrace);

    document.getElementById('path_traceProb_lbl').addEventListener('click', changePath);

    document.getElementById('minwtt_pathProb_lbl').addEventListener('click', changeTrace);
    document.getElementById('minmsgwtt_pathProb_lbl').addEventListener('click', changeTrace);
    
    document.getElementById('show_trace_lbl').addEventListener('click', showTrace);
    document.getElementById('show_path_lbl').addEventListener('click', showPath);
    
    document.getElementById('show_object_lbl').addEventListener('click', showActivity);
});