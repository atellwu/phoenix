var payloadStr = "";

function parsePayload() {
	var queryTips = "";

	var deparray = getArrayFromString($("#payload_dependencies").val());
	var oparray = getArrayFromString($("#payload_operators").val());
	var verarray = getArrayFromString($("#payload_versions").val());
	var jointarray = getArrayFromString($("#payload_joints").val());

	for (var idx = 0; idx < deparray.length; idx++) {
		if (idx > 0) {
			payloadStr += "&joint=".concat(jointarray[idx - 1]);
			queryTips += " " + jointarray[idx - 1] + " ";
		}
		payloadStr += "&dependency=".concat(deparray[idx]);
		payloadStr += "&operator=".concat(oparray[idx]);
		payloadStr += "&version=".concat(verarray[idx]);
		queryTips += deparray[idx] + oparray[idx] + verarray[idx];
	}

	var agentversion = $("#payload_agentversion").val();
	var agentoperator = $("#payload_agentoperator").val();

	payloadStr += agentversion == "" ? "" : "&agentversion=".concat(agentversion);
	payloadStr += agentoperator == "" ? "" : "&agentoperator=".concat(agentoperator);
	if (agentversion != "" && agentoperator != "") {
		queryTips += "AgentVersion " + agentoperator + " " + agentversion;
	}

	if (queryTips != "") {
		$("#queryInfo").html(queryTips);
		$("#queryInfo").parent().css({
			"display" : ""
		});
	}
}

function getArrayFromString(str) {
	var sourceArray = str.substring(1, str.length - 1).split(", ");
	var finalArray = [];
	for (var idx = 0; idx < sourceArray.length; idx++) {
		if (sourceArray[idx] != "") {
			finalArray.push(sourceArray[idx]);
		}
	}
	return finalArray;
}

function bind_cmp_evt_handlers() {
	$("input.all-check").click(function() {
		var product = $(this).val();
		var status = $(this).is(":checked");
		$("input[meta='" + product + "']").attr("checked", status);
		set_submit_status(product, status);
	});
	$("input[type='checkbox']").click(function() {
		var product = $(this).attr("meta");
		if (product != undefined) {
			var status = $("input:checked[meta='" + product + "']").length > 1;
			set_submit_status(product, status);
		}
	});
}

function set_submit_status(product, status) {
	var submit = $("input:submit[meta='" + product + "']");
	submit.attr("disabled", !status);
	if (!status) {
		submit.addClass("disabled");
	} else {
		submit.removeClass("disabled");
	}
}

$(function() {
	parsePayload();
	$("a.toProject").attr("href", function() {
		return $(this).attr("href") + payloadStr;
	});
	$('#myTab a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
		var table = $.fn.dataTable.fnTables(true);
		if (table.length > 0) {
			$(table).dataTable().fnAdjustColumnSizing();
		}
		bind_cmp_evt_handlers();
	});
	$('table').dataTable({
		"sScrollY" : "400px",
		"bPaginate" : false,
		"oLanguage" : {
			"sInfo" : "Total Domains：_TOTAL_"
		},
		"sDom" : "<'row-fluid'<'span6'f><'span6'i>>t<'row-fluid'<'span6'l><'span6'p>>",
		"aoColumns" : [ {
			"bSortable" : false
		}, null, null, null, null, null, null ],
		"aaSorting" : [ [ 1, 'asc' ] ]
	});
	bind_cmp_evt_handlers();
});
