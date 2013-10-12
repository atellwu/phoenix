(function(w) {
	var app = {
		"openAddNodeModal" : function() {
			$('#addNodeModal').modal('show');
		},
		"cancelAddOfferAdModal" : function() {
			$('#addOfferModel').hide();
			$('#offerAdDiv').show();

			$('#addOfferModalButton').show();
			$('#saveOfferModalButton').hide();
			$('#cancelOfferModalButton').hide();
			$('#searchForm').hide();
		},
		"openDelOfferAdModal" : function(id, subject, imageuri) {
			$("#delOfferAdId").val(id);
			$("#delOfferAdSubject").text(subject);
			$("#delOfferAdImg").attr('src', imageuri);
			$("#delOfferAdModal").modal('show');
		},
		"openModSubjectModal" : function(id, subject) {
			$("#modOfferAdId").val(id);
			$("#modSubject").val(subject);
			$("#modSubjectModal").modal('show');
		},
		"search" : function() {
			$('#offersContainer > p.loading').show();

			var text = $("#searchText").val();
			var url = w.contextpath + '/loadOffers';
			var param = new Object();
			param.searchText = text;
			// 搜索时，不带分类
			// if (typeof catId != 'undefined') {
			// param.catId = catId;
			// } else {// 自动从页面的类型选择器获取
			// param.catId = $("#categorySelect").val();
			// }
			param.pageNum = 1;
			var url = w.contextpath + '/loadOffers';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : alishare.loadOffersDone(param.pageNum),
				error : alishare.httpError
			});
		},
		"delOfferAd" : function() {
			var offerAdId = $("#delOfferAdId").val();
			var url = w.contextpath + '/safe/delOfferAd';
			var param = new Object();
			param.offerAdId = offerAdId;
			param.csrfToken = w.csrfToken;
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : alishare.delOfferAdDone(offerAdId),
				error : alishare.httpError
			});
		},
		"delOfferAdDone" : function(offerAdId) {
			return function(data) {
				if (data.success == false) {
					alishare.appError("出错啦，亲", data.errorMsg);
				} else {
					// 隐藏modal
					$('#delOfferAdModal').modal('hide');
					// 删除表格的对应行
					var id = 'tr_' + offerAdId;
					var obj = $('#' + id);
					obj.fadeOut('slow');
					// obj.remove();
					setTimeout(function() {
						obj.remove();
					}, 500);
					// 删除“已选择商品数组”的对应元素
					alishare.delSelected(offerAdId);
					// 设置个数的提示
					offerAdCount--;
					$("#offerAdTotalCount").text(offerAdCount);
				}
			}
		},
		"modSubject" : function() {
			var offerAdId = $("#modOfferAdId").val();
			var subject = $("#modSubject").val().trim();
			var url = w.contextpath + '/safe/modSubject';
			var param = new Object();
			param.offerAdId = offerAdId;
			param.subject = subject;
			param.csrfToken = w.csrfToken;
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : alishare.modSubjectDone(offerAdId, subject),
				error : alishare.httpError
			});
		},
		"modSubjectDone" : function(offerAdId, subject) {
			return function(data) {
				if (data.success == false) {
					$("#modSubjectErrorDiv").html($("#alert_error").html());
					$("#modSubjectErrorDiv > div > span > span").text(
							data.errorMsg);
				} else {
					// 隐藏modal
					$('#modSubjectModal').modal('hide');
					// 高亮一会儿对应行
					alishare.refresh();
				}
			}
		},
		"loadOfferAds" : function() {
			var url = w.contextpath + '/loadOfferAds';
			$.ajax({
				type : 'POST',
				url : url,
				dataType : "json",
				success : alishare.loadOfferAdsDone,
				error : alishare.httpError
			});
		},
		"loadOfferAdsDone" : function(data) {
			if (data.success == false) {
				alishare.appError("抱歉啦，亲", data.errorMsg);
			} else {
				// 设置个数的提示
				chooseCount = data.offerAds.length;
				offerAdCount = data.offerAds.length;
				$("#offerAdTotalCount").text(offerAdCount);
				$("#chooseTotalCount").text(chooseCount);
				$("#chooseLeftCount").text(20 - chooseCount);

				if (chooseCount <= 0) {
					alishare
							.alertWarn("您没有选择任何商品进行推广，所以推广将无法进行。如果需要推广，请点击“设置推广商品”选择商品。");
					return;
				}
				// 放到数组中
				$.each(data.offerAds, function(i, offerAd) {
					offerAds[offerAd.offerId] = offerAd;
				});
				// 显示出来（图文列表）
				$.each(data.offerAds, function(i, offerAd) {
					$('#offerAdList').append(
							alishare.getListSpan(offerAd, i + 1));
				});
			}
		},
		"delSelected" : function(offerId) {
			var li = $('#offer_' + offerId);
			li.removeClass("selected");
			delete offerAds[offerId];

			var obj = $('#span_' + offerId);
			obj.fadeOut('slow');
			// obj.remove();
			setTimeout(function() {
				$('#span_' + offerId).remove()
			}, 500);

			// 添加变成移除
			var button = li.find('button');
			button.text('添加');
			button.removeClass('btn-primary');

			// 设置个数的提示
			chooseCount--;
			$("#chooseTotalCount").text(chooseCount);
			$("#chooseLeftCount").text(20 - chooseCount);
		},
		"getSecurityImageUrl" : function(imageuri, suffix) {
			if (alishare.endWith(imageuri, '.jpg')
					|| alishare.endWith(imageuri, '.gif')
					|| alishare.endWith(imageuri, '.png')) {
				return imageuri;
			} else {
				return imageuri + suffix;
			}
		},
		/** 图列表 */
		"getListSpan" : function(offerAd, i) {
			var image = alishare.getSecurityImageUrl(offerAd.imageuri,
					'.64x64.jpg');
			var date = $.format.date(offerAd.createTime, "MM.dd HH:mm");
			var price = null;
			if (offerAd.price == '价格面议') {
				price = "价格面议";
			} else if (offerAd.price) {
				price = offerAd.price + offerAd.priceUnit + "/" + offerAd.unit;
			} else {
				price = "价格面议";
			}
			var span = "<tr id='tr_" + offerAd.offerId + "'>";
			span += "<td>" + i + "</td>";
			span += "<td><a target='_blank' href='" + offerAd.detailsurl
					+ "'><img id='img_" + offerAd.offerId + "' src='" + image
					+ "'></a></td>";
			span += "<td>"
					+ offerAd.subject
					+ "&nbsp;&nbsp;&nbsp; <a title='编辑推广标题' href=\"javascript:alishare.openModSubjectModal('"
					+ offerAd.offerId + "','" + offerAd.subject
					+ "')\"><i class=\"icon-pencil\"></i></a> </td>";
			span += "<td>" + price + "</td>";
			span += "<td>" + date + "</td>";
			span += "<td><a target='_blank' href='"
					+ offerAd.detailsurl
					+ "'>查看商品</a><a href=\"javascript:;\" onclick=\"javascript:alishare.openDelOfferAdModal('"
					+ offerAd.offerId + "','" + offerAd.subject + "','" + image
					+ "')\">&nbsp; 取消推广 </a>";
			// span += " <a href=\"javascript:;\" onclick=\"javascript:;\"><span
			// class=\"label\">&nbsp; 一键分享(用jiathis) </span></a>";
			span += "</td></tr>";
			return span;
		},
		"saveOfferAds" : function() {
			var url = w.contextpath + '/safe/saveOfferAds';
			var param = new Object();
			param.csrfToken = w.csrfToken;
			param.offerId = [];
			param.subject = [];
			param.price = [];
			param.priceUnit = [];
			param.unit = [];
			param.detailsurl = [];
			param.imageuri = [];
			$.each(offerAds, function(i, el) {
				param.offerId.push(el.offerId);
				param.subject.push(el.subject);
				param.price.push(el.price);
				param.priceUnit.push(el.priceUnit);
				param.unit.push(el.unit);
				param.detailsurl.push(el.detailsurl);
				param.imageuri.push(el.imageuri);
			});
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				traditional : true,// 使得param里的数组序列化时不带[]
				dataType : "json",
				async : true,
				success : alishare.saveOfferAdsDone,
				error : alishare.httpError
			});
			// 按钮显示保存中...
			// $('#submitBtn').button('loading');
			// $("#alertMessageDiv").html('');
		},
		"saveOfferAdsDone" : function(data) {
			// $('#submitBtn').button('reset');
			if (data.success == false) {
				// alishare.alertError(data.errorMsg);
				// 按钮显示保存中...
				$("#saveOfferMessageDiv").html($("#alert_error").html());
				$("#saveOfferMessageDiv > div > span > span").text(
						data.errorMsg);
			} else {
				alishare.refresh();
				// if (data.warnMsg) {
				// alishare.alertWarn("保存成功，但" + data.warnMsg);
				// } else {
				// alishare.alertSuccess("保存成功!");
				// }

			}
			// $("#submitBtn").text("保存修改");
		},
		"loadCategorys" : function() {
			var url = w.contextpath + '/loadCategorys';
			$.ajax({
				type : 'POST',
				url : url,
				dataType : "json",
				async : true,
				success : alishare.loadCategorysDone,
				error : alishare.httpError
			});
		},
		"loadCategorysDone" : function(data) {
			if (data.success == false) {
				alishare.appError("抱歉啦，亲", data.errorMsg);
			} else {
				var html = "<option value=\"-1\" selected=\"selected\">全部</option>";
				$.each(data.categorys, function(i, el) {
					html += "<option value=\"" + el.id + "\">" + el.name
							+ "</option>";
				});
				$("#categorySelect").html(html);
			}
		},
		"loadOffers" : function(pageNum, catId) {
			$('#offersContainer > p.loading').show();
			var param = new Object();
			if (typeof catId != 'undefined') {
				param.catId = catId;
			} else {// 自动从页面的类型选择器获取
				param.catId = $("#categorySelect").val();
			}
			if (typeof pageNum != 'undefined' && pageNum > 0) {
				param.pageNum = pageNum;
			} else {
				param.pageNum = 1;
			}
			var url = w.contextpath + '/loadOffers';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : alishare.loadOffersDone(param.pageNum),
				error : alishare.httpError
			});
		},
		"loadOffersDone" : function(pageNum) {
			return function(data) {
				$('#offersContainer > p.loading').hide();
				if (data.success == false) {
					alishare.appError("抱歉啦，亲", data.errorMsg);
				} else {
					// 设置offer总页数
					w.offerTotalPage = data.offerTotalPage;
					$('#offerTotalPage').text(data.offerTotalPage);
					// $('#offerTotalCount').text(data.offerTotalCount);
					$('#offerPageNum').text(pageNum);
					// 检查上一页和下一页是否可以点击
					w.pageNum = pageNum;
					if (pageNum >= w.offerTotalPage) {
						$("#offerNextPage").addClass("disabled");
						$("#offerNextPage > a").attr("href", "javascript:;");
					} else {
						$("#offerNextPage").removeClass("disabled");
						$("#offerNextPage > a")
								.attr("href",
										"javascript:alishare.loadOffers(window.pageNum+1)");
					}
					if (pageNum <= 1) {
						$("#offerPrePage").addClass("disabled");
						$("#offerPrePage > a").attr("href", "javascript:;");
					} else {
						$("#offerPrePage").removeClass("disabled");
						$("#offerPrePage > a")
								.attr("href",
										"javascript:alishare.loadOffers(window.pageNum-1)");
					}
					$('#offerPrePage').parent().show();

					// 如果没有offer，则显示没有offer的警告
					if (data.offers.length <= 0) {
						var noOfferDiv = "<div class=\"alert alert-block\"><h3>对不起，没有商品</h3>可能是该分类下没有商品。</div>";
						$('#noOfferDiv').html(noOfferDiv);
						$('#offers').html("");
						return;
					}

					// 显示模板
					var html = "";
					$
							.each(
									data.offers,
									function(i, el) {
										var detailUrl = "http://detail.1688.com/offer/"
												+ el.id + ".html";
										var selected = false;
										var selectedClass = "";
										if (offerAds["" + el.id]) {
											selected = true;
										}
										if (selected) {
											selectedClass += " selected";
										}
										html += "<li class=\"span2"
												+ selectedClass
												+ "\" id=\"offer_" + el.id
												+ "\">";
										html += "<div onclick=\"alishare.selectOffers(this)\" class=\"template\"><span class=\"selectIcon\"></span>";
										html += "<a style=\"height:100px\" class=\"thumbnail\" title=\""
												+ el.subject
												+ "\" price=\""
												+ el.price
												+ "\" priceUnit=\""
												+ el.priceUnit
												+ "\" unit=\""
												+ el.unit
												+ "\" href=\"javascript:;\"><img src=\"";
										if (typeof el.imageUri != 'undefined'
												&& el.imageUri != '') {
											var endStr = ".jpg";
											var imageUri = el.imageUri;
											if (alishare.endWith(imageUri,
													endStr)) {// 如果以.jpg结尾，去掉
												imageUri = imageUri
														.substr(
																0,
																imageUri.length
																		- endStr.length);
											}
											var img = "http://img.china.alibaba.com/"
													+ imageUri + ".summ.jpg"
											html += img + "\"></a>";
										} else {
											html += "http://i02.c.aliimg.com/images/cn/market/trade/list/070423/nopic.gif\"></a>";
										}
										html += "</div>";
										html += "<div style=\"height:38px;overflow:hidden;\" class=\"caption\">";
										if (selected) {
											html += "<button style=\"float:right\" onclick=\"alishare.selectOffers(this)\" class=\"btn btn-mini btn-primary\">移除</button>";
										} else {
											html += "<button style=\"float:right\" onclick=\"alishare.selectOffers(this)\" class=\"btn btn-mini\">添加</button>";
										}
										html += "<a target=\"_blank\" title=\""
												+ el.subject + "\" href=\""
												+ detailUrl + "\"><span>"
												+ el.shortSubject;
										html += "</span></a></div>";
										html += "</li>";
									});
					$("#offers").html(html);
					$('#noOfferDiv').html("");

					loaded = true;
				}
			};
		},
		"selectOffers" : function(obj) {
			// 获取现在点击选择的li
			var curObj = $(obj).parents('li:first');
			var offerId = parseInt(curObj.attr('id').substring(6));

			// 如果是当前点击已有的，则相当于取消选择
			if (curObj.hasClass("selected")) {
				alishare.delSelected(offerId);
				$("#saveOfferMessageDiv").html("");
			} else {
				// 限制20个。
				if (chooseCount >= 20) {
					$("#saveOfferMessageDiv").html($("#alert_error").html());
					$("#saveOfferMessageDiv > div > span > span").text(
							"推广的商品个数不能超过 20 个。");
					return;
				}

				curObj.addClass("selected");

				// 移除变成添加
				var button = $(curObj).find('button');
				button.text('移除');
				button.addClass('btn-primary');

				// 构造offerAd
				var ahref = curObj.find('div > a:first');
				var imageuri = ahref.children('img').attr('src');
				var subject = ahref.attr('title');
				var price = ahref.attr('price');
				var priceUnit = ahref.attr('priceUnit');
				var unit = ahref.attr('unit');
				var detailsurl = curObj.find('div:eq(1) > a:first')
						.attr('href');
				var offerAd = new Object();
				offerAd.offerId = offerId;
				offerAd.imageuri = imageuri;
				offerAd.subject = subject;
				offerAd.price = price;
				offerAd.priceUnit = priceUnit;
				offerAd.unit = unit;
				offerAd.detailsurl = detailsurl;
				offerAds[offerId] = offerAd;

				// 设置个数的提示
				chooseCount++;
				$("#chooseTotalCount").text(chooseCount);
				$("#chooseLeftCount").text(20 - chooseCount);
			}
		},
		"isEmail" : function(email) {
			var regex = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
			return regex.test(email);
		},
		"refresh" : function() {
			w.location.reload();
		},
		"alertError" : function(msg) {
			$("#alertMessageDiv").html($("#alert_error").html());
			$("#alertMessageDiv > div > span > span").text(msg);
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
			alishare.alertErrorModal(title, errorMsg);
		},
		"httpError" : function(xhr, textStatus, errorThrown) {
			alishare.alertErrorModal('抱歉啦，亲', '抱歉，网络发生错误了，请刷新页面试试...');
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
		}
	};
	w.app = app;
}(window || this));
