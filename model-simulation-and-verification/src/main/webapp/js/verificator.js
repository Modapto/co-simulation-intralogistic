var appname = 'model-simulation-verification';
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
    var verificationResultList = xmlData.find('VerificationResults > FormalVerificationResult');
    for (var i = 0; i < verificationResultList.length; i++) {
        $('#model_generalRes_select').append($('<option>', {
            value : verificationResultList[i].getAttribute('modelId'),
            text : verificationResultList[i].getAttribute('modelName')
        }));
        if(i==0){
            modelId = verificationResultList[i].getAttribute('modelId');
            $('#model_generalRes_select').val(modelId);
            loadStatusResult();
        }
    }
}

var objectListXml = null;
function loadObjectListXml(){
    var serviceEndpoint = getProtocol() + '//' + getHostname() + '/'+appname;
    var host = getURLParameter("host");
    if (host != null)
        serviceEndpoint = host + '/'+appname;
    var endpointUrl = serviceEndpoint + '/rest/utils/getmodellist';
    
    if (fileContent == null)
        return;
    
    $.ajax({
        url : endpointUrl,
        type : 'POST',
        data : fileContent,
        dataType : 'text',
        contentType : 'application/xml',
        processData : false,
        async : false,
        success : function(data, status) {

            if(data.startsWith('<ERROR>')){
                alert(data);
                return;
            }
            
            objectListXml = $(jQuery.parseXML(data));
        },
        error : function(request, status, error) {
            alert('Error: ' + status + ' ' + error);
        }
    });
}

function loadReachabilityModelSelect() {
    $('#reachability_model_select').empty();
    var modelList = objectListXml.find('Models > Model');
    for (var i = 0; i < modelList.length; i++) {
        $('#reachability_model_select').append($('<option>', {
            value : modelList[i].getAttribute('id'),
            text : modelList[i].getAttribute('name')
        }));
        if(i==0){
            $('#reachability_model_select').val(modelList[i].getAttribute('id'));
            loadReachabilityObjectSelect();
        }
    }
}

function onReachabilityModelSelectChange() {
    loadReachabilityObjectSelect();
}

function loadReachabilityObjectSelect() {
    var modId = $('#reachability_model_select').find(":selected").val();
    
    $('#reachability_object_select').empty();
    var objList = objectListXml.find('Models > Model[id="'+modId+'"] > Object');
    for (var i = 0; i < objList.length; i++) {
        $('#reachability_object_select').append($('<option>', {
            value : objList[i].getAttribute('id'),
            text : objList[i].getAttribute('name')
        }));
        if(i==0){
            $('#reachability_object_select').val(objList[i].getAttribute('id'));
        }
    }
}

function loadPathModelSelect() {
    $('#path_model_select').empty();
    var modelList = objectListXml.find('Models > Model');
    for (var i = 0; i < modelList.length; i++) {
        $('#path_model_select').append($('<option>', {
            value : modelList[i].getAttribute('id'),
            text : modelList[i].getAttribute('name')
        }));
        if(i==0){
            $('#path_model_select').val(modelList[i].getAttribute('id'));
            loadPathObjectSelect();
        }
    }
}

function onPathModelSelectChange() {
    loadPathObjectSelect();
}

function loadPathObjectSelect() {
    var modId = $('#path_model_select').find(":selected").val();
    
    $('#path_objectFrom_select').empty();
    $('#path_objectTo_select').empty();
    var objList = objectListXml.find('Models > Model[id="'+modId+'"] > Object');
    for (var i = 0; i < objList.length; i++) {
        $('#path_objectFrom_select').append($('<option>', {
            value : objList[i].getAttribute('id'),
            text : objList[i].getAttribute('name')
        }));
        $('#path_objectTo_select').append($('<option>', {
            value : objList[i].getAttribute('id'),
            text : objList[i].getAttribute('name')
        }));
        if(i==0){
            $('#path_objectFrom_select').val(objList[i].getAttribute('id'));
            $('#path_objectTo_select').val(objList[i].getAttribute('id'));
        }
    }
}

function onModelSelectChange() {
    modelId = $('#model_generalRes_select').find(":selected").val();
    loadStatusResult();
}

function loadStatusResult() {
    var status = xmlData.find('VerificationResults > FormalVerificationResult[modelId="'+modelId+'"] > Status').text();
    var description = xmlData.find('VerificationResults > FormalVerificationResult[modelId="'+modelId+'"] > Description').text();
    $("#status_lbl").text(status);
    $("#description_lbl").text(description);
    
    var i=1;
    $('#counterexample_select').empty();
    xmlData.find('VerificationResults > FormalVerificationResult[modelId="'+modelId+'"] > CounterExampleTrace').each(function(){
        var name = '';
        $(this).find('Step').last().find('Object').each(function(){
            name += $(this).attr('name') + ' ';
        });

        $('#counterexample_select').append($('<option>', {
            value : i,
            text : name
        }));
        i++;
    });
}

function showCounterExample() {
    var counterexampleIndex = $('#counterexample_select').find(":selected").val();
    if(counterexampleIndex == null)
        return;
    var jsonCounterExampleInfo = generateJsonCounterExample(counterexampleIndex);
    if(msgSourceWindow == null) {
        var counterExampleNames = '';
        jsonCounterExampleInfo.objNameList.forEach(function (name, i) {
          counterExampleNames += i + ' - ' + name + '\n';
        });
        alert("CounterExample Elements Trace:\n"+counterExampleNames);
    } else {
        msgSourceWindow.postMessage(jsonCounterExampleInfo, "*");
    }
}
function generateJsonCounterExample(counterexampleIndex) {
    var objList = [];
    var objNameList = [];
    var xmlObjList = xmlData.find('VerificationResults > FormalVerificationResult[modelId="'+modelId+'"] > CounterExampleTrace').eq(counterexampleIndex-1).find('Step').each(function(){
        var parallelObjectList = [];
        var parallelObjectName = '   ';
        var objId = $(this).find('Object').each(function(){
            parallelObjectList.push($(this).attr('id'));
            parallelObjectName += $(this).attr('name') + ' | ';
        });
        objList.push(parallelObjectList);
        objNameList.push(parallelObjectName.substr(0, parallelObjectName.length-3));
    });

    return {
        command: "show_counterexample",
        modelId: modelId,
        objList: objList,
        objNameList: objNameList
    };
}

function enableDisableDeadlockType(enable) {
    $('#checkAllDeadlocklbl').css('color', enable?'black':'#ccc');
    $('#checkAllDeadlockChk').prop('disabled', !enable);
}

function enableDisableUnboundnessType(enable) {
    $('#checkAllUnboundnesslbl').css('color', enable?'black':'#ccc');
    $('#checkAllUnboundnessChk').prop('disabled', !enable);
}

function enableDisableReachabilityType(enable) {
    $('#reachabilitylbl').css('color', enable?'black':'#ccc');
    $('#reachability_model_select').prop('disabled', !enable);
    $('#reachability_negate_chk').prop('disabled', !enable);
    $('#reachability_object_select').prop('disabled', !enable);
    $('#reachability_inAnyCase_chk').prop('disabled', !enable);
}

function enableDisablePathType(enable) {
    $('#pathlbl').css('color', enable?'black':'#ccc');
    $('#path_model_select').prop('disabled', !enable);
    $('#path_negateFrom_chk').prop('disabled', !enable);
    $('#path_objectFrom_select').prop('disabled', !enable);
    $('#path_negateTo_chk').prop('disabled', !enable);
    $('#path_objectTo_select').prop('disabled', !enable);
    $('#path_inAnyCase_chk').prop('disabled', !enable);
}


function onVerificationTypeChange() {
    var type = $('input:radio[name="verificationType"]').filter(':checked').val();
    if(type == null) {
        enableDisableDeadlockType(false);
        enableDisableUnboundnessType(false);
        enableDisableReachabilityType(false);
        enableDisablePathType(false);
    }
    if(type == 'deadlock') {
        enableDisableDeadlockType(true);
        enableDisableUnboundnessType(false);
        enableDisableReachabilityType(false);
        enableDisablePathType(false);
    }
    if(type == 'unboundness') {
        enableDisableDeadlockType(false);
        enableDisableUnboundnessType(true);
        enableDisableReachabilityType(false);
        enableDisablePathType(false);
    }
    if(type == 'reachability') {
        enableDisableDeadlockType(false);
        enableDisableUnboundnessType(false);
        enableDisableReachabilityType(true);
        enableDisablePathType(false);
    }
    if(type == 'path') {
        enableDisableDeadlockType(false);
        enableDisableUnboundnessType(false);
        enableDisableReachabilityType(false);
        enableDisablePathType(true);
    }
}

function setVerificationType(type) {
    $('input:radio[name="verificationType"]').filter('[value="'+type+'"]').attr('checked', true);
    onVerificationTypeChange();
}

function hideVerificationTypesExcept(typeToShow) {
    $('#deadlock_tr').hide();
    $('#unboundness_tr').hide();
    $('#reachability_tr').hide();
    $('#path_tr').hide();
    $('#'+typeToShow+'_tr').show();
}

function initialVerificationType() {
    var providedType = getURLParameter("verificationType");
    if(providedType == null)
        setVerificationType('deadlock');
    else{
        setVerificationType(providedType);
        hideVerificationTypesExcept(providedType);
    }
}





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
        setFileContent(e.target.result);
        // sessionStorage.setItem('adoxxSimModelContent', e.target.result);
    };
    // reader.readAsArrayBuffer(file);
    reader.readAsText(file);
}

function setFileContent(content) {
    fileContent = content;
    
    loadObjectListXml();
    loadReachabilityModelSelect();
    loadPathModelSelect();
    onPathModelSelectChange();
    onReachabilityModelSelectChange();
}

function verify() {
    var serviceEndpoint = getProtocol() + '//' + getHostname() + '/'+appname;
    var host = getURLParameter("host");
    if (host != null)
        serviceEndpoint = host + '/'+appname;

    if (fileContent == null)
        return;
    
    var endpointUrl = '';
    var verificationType = $('input:radio[name="verificationType"]').filter(':checked').val();
    if(verificationType == 'deadlock')
        endpointUrl = serviceEndpoint + '/rest/verificator/deadlock?checkAllDeadlock=' + $('#checkAllDeadlockChk').is(':checked');
    if(verificationType == 'unboundness')
        endpointUrl = serviceEndpoint + '/rest/verificator/unboundness?checkAllUnboundedness=' + $('#checkAllUnboundnessChk').is(':checked');
    if(verificationType == 'reachability')
        endpointUrl = serviceEndpoint + '/rest/verificator/reachability?bpObjectId=' + $('#reachability_object_select').find(":selected").val() + '&inAnyCase=' + $('#reachability_inAnyCase_chk').is(':checked') + '&negate=' + $('#reachability_negate_chk').is(':checked');
    if(verificationType == 'path')
        endpointUrl = serviceEndpoint + '/rest/verificator/path?bpFromObjectId=' + $('#path_objectFrom_select').find(":selected").val() + '&bpToObjectId=' + $('#path_objectTo_select').find(":selected").val() + '&inAnyCase=' + $('#path_inAnyCase_chk').is(':checked') + '&negateFrom=' + $('#path_negateFrom_chk').is(':checked') + '&negateTo=' + $('#path_negateTo_chk').is(':checked');
    
    if(endpointUrl == '')
        return;
    
    $('#inputModelTxt').html(fileContent);

    $.ajax({
        url : endpointUrl,
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
            
            $('#resultsDiv').show();
            
            $('#verificationResultsTxt').html(data);
            xmlData = $(jQuery.parseXML(data));
            
            loadModelSelect();
        },
        error : function(request, status, error) {
            alert('Error: ' + status + ' ' + error);
        }
    });
    
    if(showRawResults())
        generatePNML();
}

function generatePNML() {

    var serviceEndpoint = getProtocol() + '//' + getHostname() + '/'+appname;
    var host = getURLParameter("host");
    if (host != null)
        serviceEndpoint = host + '/'+appname;

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


function onMessageReceived(event) {
    var origin = event.origin || event.originalEvent.origin;
    setFileContent(event.data);
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
    setFileContent(data);
  });
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
    $('#resultsDiv').hide();
    
    if(showRawResults()){
        $('#rawResults').show();
        $('#resultsDiv').show();
    }
    
    initialVerificationType();
    
    $('input:radio[name="verificationType"]').on('change', onVerificationTypeChange);
    
    document.getElementById('model_generalRes_select').addEventListener('change', onModelSelectChange);
    document.getElementById('show_counterexample_lbl').addEventListener('click', showCounterExample);
    document.getElementById('model_generalRes_select').addEventListener('change', onModelSelectChange);
    document.getElementById('reachability_model_select').addEventListener('change', onReachabilityModelSelectChange);
    document.getElementById('path_model_select').addEventListener('change', onPathModelSelectChange);
});