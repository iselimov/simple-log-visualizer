

var logRestApiUrl = 'http://localhost:8084/logs/';
var logRootId = new URL(window.location).searchParams.get('logRootId');
var log;

var widthSize = 600;
var nodesAmountPerWidth = 3;

var heightSize = 400;
var nodesAmountPerHeight = 12;

$.get(logRestApiUrl + 'root/' + logRootId + '/hierarchy', function ok(logsHierarchy) {

    var fullWidth = Math.ceil(logsHierarchy.depth / nodesAmountPerWidth) * widthSize;
    var fullHeight = Math.ceil(logsHierarchy.breadth / nodesAmountPerHeight) * heightSize;
    var margin = {top: 20, right: 200, bottom: 20, left: 200},
        width = fullWidth - margin.right - margin.left,
        height = fullHeight - margin.top - margin.bottom;

    var i = 0,
        duration = 750,
        root;

    var tree = d3.layout.tree()
        .size([height, width]);

    var diagonal = d3.svg.diagonal()
        .projection(function (d) {
            return [d.y, d.x];
        });

    var svg = d3.select("body").append("svg")
        .attr("width", width + margin.right + margin.left)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    root = logsHierarchy.root;
    root.x0 = height / 2;
    root.y0 = 0;

    update(root);

    d3.select(self.frameElement).style("height", "500px");

    function update(source) {

        var nodes = tree.nodes(root).reverse(),
            links = tree.links(nodes);

        var node = svg.selectAll("g.node")
            .data(nodes, function (d) {
                return d.id || (d.id = ++i);
            });

        var nodeEnter = node.enter().append("g")
            .attr("class", "node")
            .attr("transform", function (d) {
                return "translate(" + source.y0 + "," + source.x0 + ")";
            })
            .on("click", click);

        function click(res) {
            $("#name")[0].innerText = 'Name: ' + res.name;

            if (res.description) {
                $("#description")[0].innerText = 'Description: ' + res.description;
            } else {
                $("#description")[0].innerText = null;
            }
            if (res.exception) {
                $("#exception")[0].innerText = 'Exception: ' + res.exception;
            } else {
                $("#exception")[0].innerText = null;
            }

            $("#startDate")[0].innerText = 'Start date: ' + res.startDate;
            $("#endDate")[0].innerText = 'End date: ' + res.endDate;
            $("#timing")[0].innerText = 'Timing: ' + res.timing;

            log = res;

            // x grows from top to bottom, y grows from left to right
            $("#logSummary")[0].style.marginLeft = Math.ceil(res.y) + 'px';
            $("#logSummary")[0].style.marginTop = Math.ceil(res.x) + 'px';
            $("#logSummary")[0].showModal();
        }

        nodeEnter.append("circle")
            .attr("r", 1e-6)
            .style("fill", function (d) {
                if (d.exception) {
                    return "red";
                }
                if (!d.endDate) {
                    return "yellow";
                }
                return "white";
            });

        nodeEnter.append("text")
            .attr("x", function (d) {
                return d.children || d._children ? -13 : 13;
            })
            .attr("dy", ".35em")
            .attr("text-anchor", function (d) {
                return d.children || d._children ? "end" : "start";
            })
            .text(function (d) {
                return d.name;
            })
            .style("fill-opacity", 1e-6);

        var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function (d) {
                return "translate(" + d.y + "," + d.x + ")";
            });

        nodeUpdate.select("circle")
            .attr("r", 10)
            .style("fill", function (d) {
                if (d.exception) {
                    return "red";
                }
                if (!d.endDate) {
                    return "yellow";
                }
                return "white";
            });

        nodeUpdate.select("text")
            .style("fill-opacity", 1);

        var link = svg.selectAll("path.link")
            .data(links, function (d) {
                return d.target.id;
            });

        link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", function (d) {
                var o = {x: source.x0, y: source.y0};
                return diagonal({source: o, target: o});
            });

        link.transition()
            .duration(duration)
            .attr("d", diagonal);
    }
}).fail(function error(resp) {
    var response = resp && resp.responseJSON;
    if (response && response.message && response.status) {
        alert('Error was caused: ' + response.message + ', status = ' + response.status);
    } else {
        alert('Something got wrong: ' + response);
    }
});

function showInGraylog() {
    var url = logRestApiUrl + log.id + '/query?from=' + log.startDate;
    if (log.endDate) {
        url += '&to=' + log.endDate;
    }

    $.get(url, function ok(filterQueryUrl) {
        window.open(filterQueryUrl);
    }).fail(function error(resp) {
        var response = resp && resp.responseJSON;
        if (response && response.message && response.status) {
            alert('Error was caused: ' + response.message + ', status = ' + response.status);
        } else {
            alert('Something got wrong: ' + response);
        }
    });
}
