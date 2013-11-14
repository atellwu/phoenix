(function(w) {
	var app = {
		"openAddParamModal" : function() {
			$('#addParamKey').val('');
			$('#addParamValue').val('');
			$('#addParamModal').modal('show');
			$('#addParamKey').focus();
		},
		"openAddPoolModal" : function() {
			$('#addPoolName').val('');
			$('#addPoolModal').modal('show');
			$('#addPoolName').focus();
		},
		"backPool" : function() {
			$('div[pool]').hide();
			$('div[pool=' + name + ']').show();
		},
		"isEmail" : function(email) {
			var regex = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
			return regex.test(email);
		},
		"refresh" : function() {
			w.location.reload();
		},
		"alertError" : function(msg, divId) {
			if (!divId) {
				divId = "alertMessageDiv";
			}
			console.log(divId);
			$("#" + divId).html($("#alert_error").html());
			$("#" + divId + " > div > span > span").text(msg);
		},
		"alertSuccess" : function(msg) {
			$("#alertMessageDiv").html($("#alert_success").html());
			$("#alertMessageDiv > div > span > span").text(msg);
		},
		"alertWarn" : function(msg) {
			$("#alertMessageDiv").html($("#alert_warn").html());
			$("#alertMessageDiv > div > span > span").text(msg);
		},
		"appError" : function(title, errorMsg) {
			app.alertErrorModal(title, errorMsg);
		},
		"httpError" : function(xhr, textStatus, errorThrown) {
			app.alertErrorModal('抱歉啦，亲', '抱歉，网络发生错误了，请刷新页面试试...');
		},
		"alertErrorModal" : function(title, errorMsg) {
			// 显示错误消息
			$('#errorMsg > div[class="modal-header"] > h3').text(title);
			$('#errorMsg > div[class="modal-body"] > p').text(errorMsg);
			$('#errorMsg').modal('show');
		},
		"endWith" : function(s, endStr) {
			if (s == null || s == "" || s.length == 0
					|| endStr.length > s.length)
				return false;
			if (s.substring(s.length - endStr.length) == endStr)
				return true;
			else
				return false;
			return true;
		},
		"startWith" : function(s, preStr) {
			if (s == null || s == "" || s.length == 0
					|| preStr.length > s.length)
				return false;
			if (s.substr(0, preStr.length) == preStr)
				return true;
			else
				return false;
			return true;
		},
		"refresh" : function() {
			w.location.reload();
		},
		"bookmarkPage" : function(url, title) {
			try {
				if (!url) {
					url = window.location
				}
				if (!title) {
					title = document.title
				}
				var browser = navigator.userAgent.toLowerCase();
				if (window.sidebar) { // Mozilla, Firefox, Netscape
					window.sidebar.addPanel(title, url, "");
				} else if (window.external) { // IE or chrome
					if (browser.indexOf('chrome') == -1) { // ie
						window.external.AddFavorite(url, title);
					} else { // chrome
						alert('请按 Ctrl和D 快捷键进行收藏');
					}
				} else if (window.opera && window.print) { // Opera -
					// automatically
					// adds to sidebar if
					// rel=sidebar in the tag
					return true;
				} else if (browser.indexOf('konqueror') != -1) { // Konqueror
					alert('请按 Ctrl和B 快捷键进行收藏');
				} else if (browser.indexOf('webkit') != -1) { // safari
					alert('请按 Ctrl和B 快捷键进行收藏');
				} else {
					alert('您的浏览器不支持该操作，请您点击浏览器的“收藏”菜单进行添加。');
				}
			} catch (err) {
				alert('您的浏览器不支持该操作，请您点击浏览器的“收藏”菜单进行添加。');
			}
		},
		"onHashChange" : function() {
			var hash = window.location.hash;
			if (hash.length > 0) {
				// 去掉#号
				hash = hash.substring(1);
				var j, r;
				$.each(hash.split('&'), function(i, part) {
					var keyValue = part.split('=');
					if (keyValue[0] == 'j') {
						j = keyValue[1];
						rundemo_app.changeJavaCodeFile(keyValue[1]);
					} else if (keyValue[0] == 'r') {
						r = keyValue[1];
						rundemo_app.changeResourceFile(keyValue[1]);
					}
				});
				if (j == 0 && r == 0) {
					window.location.hash = "";
				}
			} else {
				rundemo_app.changeJavaCodeFile(0);
				rundemo_app.changeResourceFile(0);
			}
		}
	};
	w.app = app;
}(window || this));
