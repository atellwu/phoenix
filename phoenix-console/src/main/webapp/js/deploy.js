var continuous_err_times = 0;
var MAX_CON_ERR_TIMES = 10;
var status;
var DeployStatus = {
	FAILED : "failed",
	SUCCESS : "successful",
	DEPLOYING : "deploying",
	WARNING : "warning",
	CANCELLING : "cancelling",
	PAUSING : "pausing",
	UNKNOWN : "unknown"
};

$(function() {
	if (!is_deploy_finished()) {
		setTimeout(fetch_deploy_status, 500);
	}
	bind_cmp_evt_handlers();
});

function bind_cmp_evt_handlers() {
	$(".host_status").click(function() {
		var id_ip = $(this).attr("id").split(":");
		var id = id_ip[0];
		var ip = id_ip[1];

		$(".deploy-header").hide();
		$(".terminal").hide();
		$(".host_status").removeClass("selected");
		$(this).addClass("selected");
		$("#header-" + id).show();
		$("#log-" + id + "-" + ip.replace(/\./g, "\\.")).show();
	});
}

function fetch_deploy_status() {
	var hostArr = [];
	$(".host_status").map(function() {
		hostArr.push($(this).attr("id") + ":" + $(this).attr("data-offset"));
	});
	if (hostArr.length > 0) {
		$.ajax("", {
			data : $.param({
				"op" : "status",
				"progress" : hostArr.join(",")
			}, true),
			type : "POST",
			dataType : "json",
			cache : false,
			success : function(result) {
				continuous_err_times = 0;
				if (result != null) {
					$.each(result, function(index, deploy) {
						$.each(deploy.hosts, function(index, obj) {
							var deploy_id = deploy.id;
							var host = obj.host.replace(/\./g, "\\.");
							update_host_status(deploy_id, host, obj);
							update_host_log(deploy_id, host, obj);
						});
						update_deploy_status(deploy.id, deploy.status);
						if (result.status != status) {
							status = result.status;
							setButtonStatus();
						}
					});
					for ( var deploy_status in result) {
					}
				}
			},
			error : function(xhr, errstat, err) {
				continuous_err_times++;
			},
			complete : function() {
				if (!is_deploy_finished() && continuous_err_times < MAX_CON_ERR_TIMES) {
					setTimeout(fetch_deploy_status, 1000);
				}
			}
		});
	}
}

function is_deploy_finished() {
	var finished = true;
	$("[id^=deploy_status_]").each(function() {
		var status = $(this).text();
		if (status == DeployStatus.DEPLOYING || status == DeployStatus.PAUSING || status == DeployStatus.CANCELLING) {
			finished = false;
		}
	});
	return finished;
}

function update_host_status(id, host, data) {
	var host_status = data.status;
	var host_div = $("#" + id + "\\:" + host);
	var $hostProgress = host_div.find(".progress");
	var $hostBar = host_div.find(".bar");
	var $hostStep = host_div.find(".step");
	if ("pending" == host_status) {
		$hostBar.css("width", "0%");
		$hostStep.text("");
	} else {
		$hostBar.css("width", data.progress + "%");
		$hostStep.text(data.step);
	}
	host_div.attr("data-offset", data.offset);
	$hostProgress.removeClass().addClass("pull-right progress");
	if ("successful".equalsIgnoreCase(host_status)) {
		$hostProgress.addClass("progress-success");
	} else if ("failed".equalsIgnoreCase(host_status)) {
		$hostProgress.addClass("progress-danger");
	} else if ("doing".equalsIgnoreCase(host_status)) {
		$hostProgress.addClass("progress-striped active");
	} else if ("cancelled".equalsIgnoreCase(host_status)) {
		$hostProgress.addClass("progress-cancelled");
	} else if ("warning".equalsIgnoreCase(host_status)) {
		$hostProgress.addClass("progress-warning");
	}
}

function update_host_log(id, host, data) {
	// TODO data.log是否已经做了换行到<br />的转换，或者是使用数组形式
	if (data.log != null && data.log != "") {
		var $logContainer = $("#log-" + id + "-" + host);
		$logContainer.append("<div class=\"terminal-like\">" + data.log + "</div>");
		$logContainer.scrollTop($logContainer.get(0).scrollHeight);
	}
}

function update_deploy_status(id, status) {
	$("#deploy_status_" + id).text(status);
	var color;
	switch (status) {
	case 'successful':
		color = 'label-success';
		break;
	case 'failed':
		color = 'label-failed';
		break;
	case 'deploying':
		color = "label-doing";
		break;
	case 'cancelled':
		color = "label-cancelled";
		break;
	case 'warning':
		color = "label-warning";
		break;
	default:
		color = "";
	}
	$("#deploy_status_" + id).attr("class", "pull-right label " + color);
}