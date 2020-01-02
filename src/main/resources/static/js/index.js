var restApiUrl = 'http://localhost:8084/logs/';
var dateTimeMask = 'YYYY-MM-DDTHH:mm:ss';

var logRootIds = [];

$.get(restApiUrl + 'sources', function ok(sources) {
    sources.forEach(function (source) {
        setStartDateToNow();
        setEndDateToNow();
        $("#source").append($('<option></option>').attr('value', source.id).text(source.name));
    });
});

function setStartDateToNow() {
    $('#from')[0].value = moment().format(dateTimeMask);
}

function setEndDateToNow() {
    $('#to')[0].value = moment().format(dateTimeMask);
}

function onFindLogRoots() {
    var sourceId = +$('#source')[0].options[$('#source')[0].selectedIndex].value;
    var fromStr = $('#from')[0].value;
    var toStr = $('#to')[0].value;
    if (!(sourceId && fromStr && toStr)) {
        alert('source, from, to parameters should be filled');
        return;
    }

    var fromDate = moment(fromStr, dateTimeMask, true);
    var toDate = moment(toStr, dateTimeMask, true);
    if (!fromDate.isValid() || !toDate.isValid()) {
        alert('Incorrect data format, it should be corresponded to the mask ' + dateTimeMask);
        return;
    }

    logRootIds = [];

    var logRootTable = $('#logRoots')[0];
    for (var i = logRootTable.rows.length - 1; i > 0; i--) {
        logRootTable.deleteRow(i);
    }

    $.get(restApiUrl + 'source/' + sourceId + '/roots?' + 'from=' + fromDate.format(dateTimeMask) + '&' +
        'to=' + toDate.format(dateTimeMask), function ok(logRoots) {

        if (!logRoots.length) {
            logRootTable.setAttribute('hidden', null);
            return;
        }

        logRoots.forEach(function (logRoot) {
            logRootIds.push(logRoot.id);

            appendRow(logRootTable, [
                logRoot.patient,
                logRoot.payloadName,
                logRoot.startDate,
                logRoot.endDate
            ]);
        });

        appendButton(logRootTable);

        logRootTable.removeAttribute('hidden');
    }).fail(function error(resp) {
        var response = resp && resp.responseJSON;
        if (response && response.message && response.status) {
            alert('Error was caused: ' + response.message + ', status = ' + response.status);
        } else {
            alert('Something got wrong: ' + response);
        }
    });

    function appendRow(tbl, logRoot) {
        var row = tbl.insertRow(tbl.rows.length);
        for (var i = 0; i < tbl.rows[0].cells.length; i++) {
            createCell(row.insertCell(i), logRoot[i], 'row');
        }
    }

    function appendButton(tbl) {
        for (var i = 1; i < tbl.rows.length; i++) {
            createButton(tbl.rows[i].insertCell(tbl.rows[i].cells.length));
        }
    }

    function createCell(cell, text, style) {
        var div = document.createElement('div'),
            txt = document.createTextNode(text);
        div.appendChild(txt);
        div.setAttribute('class', style);
        div.setAttribute('className', style);
        cell.appendChild(div);
    }

    function createButton(cell) {
        cell.innerHTML = "<button class=\"button\" onclick=onShowLogs(this)>Show logs</button>"
    }

}

function onShowLogs(cell) {
    var rowElement = cell.parentElement.parentElement;
    var logRootId = logRootIds[rowElement.rowIndex - 1];
    window.open('/logs.html?logRootId=' + logRootId);
}
