<%@ page contentType="text/html; charset=utf-8"%>

<%@ taglib prefix="a" uri="http://www.dianping.com/phoenix/console"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>

<jsp:useBean id="ctx" type="com.dianping.phoenix.console.page.home.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.phoenix.console.page.home.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.phoenix.console.page.home.Model" scope="request" />

<a:layout>
	<res:useCss value="${res.css.local['DT_bootstrap.css']}" target="head-css" />
	<res:useCss value="${res.css.local['bootstrap-select.min.css']}" target="head-css" />

	<ul class="breadcrumb">
		<li><a href="${model.webapp}/console/home">Home</a><span class="divider">/</span></li>
		<c:choose>
			<c:when test="${payload.type=='phoenix-agent'}">
				<li><a class="toParent" href="${model.webapp}/console/home?type=phoenix-agent">Agent</a><span class="divider">/</span></li>
			</c:when>
			<c:otherwise>
				<li><a class="toParent" href="${model.webapp}/console/home?type=phoenix-kernel">Kernel</a><span class="divider">/</span></li>
			</c:otherwise>
		</c:choose>
		<li class="active">Projects</li>
	</ul>
	<form class="form-horizontal" method="post" action="${model.moduleUri}/home">
		<input type="hidden" name="type" value="${payload.type}" />
		<input type="hidden" name="op" value="cross-deploy" />

		<c:set var="product" value="${model.product}" />

		<div class="container-fluid">
			<div class="span4" style="height: 500px; overflow: auto;">
				<ul class="nav nav-tabs nav-stacked" style="margin-top: 20px">
					<c:forEach var="domain" items="${model.domains}">
						<li><a href="#nav-${domain.name}">
								<input type="checkbox" style="margin-bottom: 3px;" meta="nav-check:${domain.name}">
								${domain.name}
							</a></li>
					</c:forEach>
				</ul>
			</div>
			<div class="span8" style="height: 500px; overflow: auto;">
				<c:forEach var="domain" items="${model.domains}">
					<h4 style="text-transform: capitalize;" id="nav-${domain.name}">${domain.name}</h4>
					<hr style="margin: 0 0 10px 0">
					<table id="host-nav" class="table table-striped table-condensed table-bordered">
						<thead>
							<tr>
								<th width="2%"><input type="checkbox" meta="domain-check-all:${domain.name}" /></th>
								<th width="12%">IP</th>
								<th width="2%">ST</th>
								<th width="33%">Hostname</th>
								<th width="10%">Env</th>
								<th width="15%">Kernel</th>
								<th width="15%">App</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="host" items="${domain.hosts}" varStatus="status">
								<tr>
									<td><input class="host-check" type="checkbox" name="host" value="${domain.name}:${host.value.ip}" meta="host-check:${domain.name}:${host.value.ip}"></td>
									<td>${host.value.ip}</td>
									<td><c:if test="${host.value.phoenixAgent.status=='ok'}">
											<div class="z6 a-f-e" title="可用"></div>
										</c:if> <c:if test="${host.value.phoenixAgent.status!='ok'}">
											<div class="u6 a-f-e" title="不可用"></div>
										</c:if></td>
									<td>${host.value.hostname}</td>
									<td>${host.value.env}</td>
									<c:choose>
										<c:when test="${fn:length(host.value.container.apps) eq 0}">
											<td>N/A</td>
											<td>N/A</td>
										</c:when>
										<c:otherwise>
											<c:set var="isShowed" value="false"></c:set>
											<c:forEach var="app" items="${host.value.container.apps}">
												<c:if test="${app.name==domain.name}">
													<c:set var="isShowed" value="true"></c:set>
													<td><c:choose>
															<c:when test="${fn:length(app.kernel.version) eq 0}">
																	N/A
																</c:when>
															<c:otherwise>
																	${app.kernel.version}
																</c:otherwise>
														</c:choose></td>
													<td><c:choose>
															<c:when test="${fn:length(app.version) eq 0}">
																	N/A
																</c:when>
															<c:otherwise>
																	${app.version}
																</c:otherwise>
														</c:choose></td>
												</c:if>
											</c:forEach>
											<c:if test="${isShowed == false}">
												<td>N/A</td>
												<td>N/A</td>
											</c:if>
										</c:otherwise>
									</c:choose>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</c:forEach>
			</div>
		</div>

		<br>

		<div class="alert alert-error" style="visibility: hidden; display: none;">
			<button type="button" class="close">&times;</button>
			<strong>Error !</strong> Please select at least one host before next step!
		</div>

		<div class="well well-small">
			<a class="btn btn-success btn-small" id="check-all">全选</a>
			<a class="btn btn-success btn-small" id="check-all-first">灰度选择</a>
			<a class="btn btn-danger btn-small" id="uncheck-all">重置</a>
			<!-- <button class="btn btn-primary btn-small pull-right" id="submit">下一步</button> -->
			<a class="btn btn-primary btn-small pull-right" data-toggle="modal" id="next">下一步</a>
		</div>


		<div id="policy-select" class="modal hide fade" aria-hidden="true">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4>Select Deploy Policies</h4>
			</div>
			<div class="modal-body">
				<div class="control-group">
					<label class="control-label" for="policy-version">版本号 (${payload.type})</label>
					<div class="controls">
						<select name="plan.version" class="selectpicker" id="policy-version"> ${w:showOptions(model.deliverables, payload.plan.version, 'warVersion', 'warVersion')}
						</select>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="policy-deploy-method">部署方式</label>
					<div class="controls">
						<select name="plan.policy" class="selectpicker" id="policy-deploy-method">${w:showOptions(model.policies, payload.plan.policy, 'id', 'description')}
						</select>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="policy-error-handle">错误处理</label>
					<div class="controls">
						<label class="radio" id="policy-error-handle">
							<input type="radio" name="plan.abortOnError" value="true" ${payload.plan.abortOnError==true?'checked':''}>
							中断后续发布
						</label>
						<label class="radio" id="policy-error-handle">
							<input type="radio" name="plan.abortOnError" value="false" ${payload.plan.abortOnError==false?'checked':''}>
							继续后续发布
						</label>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="policy-deploy-control">发布控制</label>
					<div class="controls">
						<label class="radio" id="policy-deploy-control">
							<input type="radio" name="plan.autoContinue" value="false" checked onchange="disableTxt('txt_deployInterval')">
							手动控制
						</label>
						<label class="radio" id="policy-deploy-control">
							<input type="radio" name="plan.autoContinue" value="true" onchange="enableTxt('txt_deployInterval')" style="margin-top: 7px">
							发布间隔(秒):&nbsp;
							<input type="text" id="txt_deployInterval" style="width: 50px;" name="plan.deployInterval" value="0">
						</label>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="policy-error-handle">冒烟测试服务</label>
					<div class="controls">
						<label class="radio" id="policy-skip-test">
							<input type="radio" name="plan.skipTest" value="false" ${payload.plan.skipTest==false?'checked':''}>
							打开
						</label>
						<label class="radio" id="policy-skip-test">
							<input type="radio" name="plan.skipTest" value="true" ${payload.plan.skipTest==true?'checked':''}>
							关闭
						</label>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<input type="submit" class="btn btn-primary" value="Deploy" />
			</div>
		</div>
	</form>
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="datatable-js" />
	<res:useJs value="${res.js.local['bootstrap-select.min.js']}" target="b-select-js" />
	<res:useJs value="${res.js.local['cross-domain.js']}" target="cross-domain-js" />
	<res:jsSlot id="datatable-js" />
	<res:jsSlot id="b-select-js" />
	<res:jsSlot id="cross-domain-js" />
</a:layout>