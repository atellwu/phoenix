function setCookie(name, value) {
	var exp = new Date();
	exp.setTime(exp.getTime() + 12 * 60 * 60 * 1000);
	document.cookie = name + "=" + escape(value) + ";expires=" + exp.toGMTString();
}
function getCookie(name) {
	var arr = document.cookie.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));
	if (arr != null)
		return unescape(arr[2]);
	return null;
}
function delCookie(name) {
	var exp = new Date();
	exp.setTime(exp.getTime() - 1);
	var cval = getCookie(name);
	if (cval != null)
		document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
}

var ready_to_deploy = {};

$(function() {
	$("table").dataTable({
		"bFilter" : false,
		"bInfo" : false,
		"bLengthChange" : false,
		"bPaginate" : false,
		"aoColumns" : [ {
			"bSortable" : false
		}, null, {
			"bSortable" : false
		}, null, {
			"bSortable" : false
		}, null, {
			"bSortable" : false
		} ],
		"aaSorting" : [ [ 3, 'asc' ] ]
	});
	$('.selectpicker').selectpicker();
	bind_cmp_evt_handlers();
});

function add_to_list(domain, host, list) {
	if (list[domain] == undefined) {
		list[domain] = [ host ];
	} else {
		list[domain].push(host);
	}
}

function bind_cmp_evt_handlers() {
	$("#check-all").click(function() {
		if ($(this).hasClass("active")) {
			$(":checkbox").attr("checked", false);
			$(this).removeClass("active");
		} else {
			$(".check-control").removeClass("active");
			$(this).addClass("active");
			$("input[type='checkbox']").attr("checked", true);
		}
	});

	$("#check-all-first").click(
			function() {
				if ($(this).hasClass("active")) {
					$(":checkbox").attr("checked", false);
					$(this).removeClass("active");
				} else {
					$(".check-control").removeClass("active");
					$(this).addClass("active");

					$(":checkbox").attr("checked", false);
					// $(":checkbox[meta^='nav-check']").attr("checked", true);

					var version = $("#policy-version").val();
					$("tbody").each(
							function() {
								$(this).find("tr[status='ok'] td:nth-child(6)[version!='" + version + "']")
										.parent("tr").first().find(":checkbox").attr("checked", true);
								if ($(this).find(":checkbox:checked").length > 0) {
									var domain = $(this).parent().attr("id").substring(9);
									$(":checkbox[meta='nav-check:" + domain + "']").attr("checked", true);
								}
							});
				}
			});

	$("#check-all-rest").click(
			function() {
				if ($(this).hasClass("active")) {
					$(":checkbox").attr("checked", false);
					$(this).removeClass("active");
				} else {
					$(".check-control").removeClass("active");
					$(this).addClass("active");

					var version = $("#policy-version").val();
					$("tbody").each(
							function() {
								$(this).find("tr[status='ok'] td:nth-child(6)[version!='" + version + "']")
										.parent("tr").find(":checkbox").attr("checked", true);
								if ($(this).find(":checkbox:checked").length > 0) {
									var domain = $(this).parent().attr("id").substring(9);
									$(":checkbox[meta='nav-check:" + domain + "']").attr("checked", true);
								}
							});
				}
			});

	$("#next").click(function() {
		if ($(":checkbox[meta^='host-check']:checked").length > 0) {
			$("#policy-select").modal();

			ready_to_deploy = {};
			$(":checkbox[meta^='host-check:']:checked").each(function() {
				var metas = $(this).attr("meta").split(":");
				var domain = metas[1];
				var host = metas[2];
				add_to_list(domain, host, ready_to_deploy);
			});
		} else {
			$(".alert").css("visibility", "visible");
			$(".alert").css("display", "block");
		}
	});

	$(":checkbox").click(function() {
		$(".check-control").removeClass("active");

		var metas = $(this).attr("meta").split(":");
		var type = metas[0];
		var domain = metas[1];
		var checked = $(this).is(":checked");
		switch (type) {
		case "nav-check":
			on_nav_checked(domain, checked);
			break;
		case "domain-check-all":
			on_domain_all_checked(domain, checked);
			break;
		case "host-check":
			on_host_checked(domain, checked);
			break;
		}
	});

	$(".close").click(function() {
		$(".alert").css("visibility", "hidden");
		$(".alert").css("display", "none");
	});
}

function on_nav_checked(domain, checked) {
	$(":checkbox[meta='domain-check-all:" + domain + "']").attr("checked", checked);
	$(":checkbox[meta^='host-check:" + domain + ":']").attr("checked", checked);
}

function on_domain_all_checked(domain, checked) {
	$(":checkbox[meta^='host-check:" + domain + ":']").attr("checked", checked);
	$(":checkbox[meta='nav-check:" + domain + "']").attr("checked", checked);
}

function on_host_checked(domain, checked) {
	var all_checked = true;
	var has_checked = false;
	$(":checkbox[meta^='host-check:" + domain + "']").each(function() {
		all_checked = Boolean(all_checked & $(this).is(":checked"));
		has_checked = $(this).is(":checked") ? true : has_checked;
	});
	$(":checkbox[meta='domain-check-all:" + domain + "']").attr("checked", all_checked);
	$(":checkbox[meta='nav-check:" + domain + "']").attr("checked", has_checked);
}