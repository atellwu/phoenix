<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="http://www.dianping.com/phoenix/console"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.phoenix.console.page.deploy.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.phoenix.console.page.deploy.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.phoenix.console.page.deploy.Model" scope="request" />

<a:layout>
	<w:error code="*">
		<h2>Error occurred: \${code} for deploy(${payload.id})</h2>
	</w:error>

	<c:set var="deploy" value="${model.deploy}" />

	<c:forEach var="deploy" items="${model.deploys}" varStatus="status">
		<section id="header-${deploy.id}" class="deploy-header <c:if test="${!status.first}">hide</c:if>">
			<div>
				<strong style="color: #08C;">部署方式</strong>: ${w:showResult(model.policies, deploy.plan.policy, 'id', 'description')}&emsp;<strong style="color: #08C;">错误处理</strong>:
				${w:translate(deploy.plan.abortOnError, 'true|false', '中断后续发布|继续后续发布', '')}&emsp; <strong style="color: #08C;">发布控制</strong>:
				<c:choose>
					<c:when test="${deploy.plan.autoContinue}">
	                    	自动(${deploy.plan.deployInterval}秒)
	                    </c:when>
					<c:otherwise>
	                    	手动
	                    </c:otherwise>
				</c:choose>
				&emsp;<strong style="color: #08C;">冒烟测试服务</strong>: ${w:translate(deploy.plan.skipTest, 'false|true', '打开|关闭', '')}&emsp;<strong style="color: #08C;">类别</strong>: <span
					style="text-transform: capitalize;">${deploy.plan.warType.name}</span>&emsp; <strong style="color: #08C;">版本</strong>: ${deploy.plan.version}
			</div>
		</section>
	</c:forEach>
	<hr>
	<div class="row-fluid">
		<div class="span4">
			<div class="accordion" id="deploy-collapses" style="height: 430px; overflow: auto;">
				<c:forEach var="deploy" items="${model.deploys}" varStatus="nav_status">
					<c:if test="${nav_status.first}">
						<c:set var="first_deploy" value="${deploy}"></c:set>
					</c:if>
					<div class="accordion-group">
						<div class="accordion-heading">
							<a class="accordion-toggle btn" style="text-align: left;" data-toggle="collapse" data-parent="#deploy-collapses" href="#deploy-${deploy.id}"
								meta="${deploy.id}:${deploy.domain}">
								<span class="label label-info" style="text-transform: capitalize;">${deploy.id}: ${deploy.domain}</span><span
									class="pull-right label ${
                                        	deploy.status eq 'successful' ? 'label-success' 
	                                        	: (deploy.status eq 'failed' ? 'label-failed' 
	                                        	: (deploy.status eq 'doing' ? 'label-doing' 
	                                        	: (deploy.status eq 'cancelled' ? 'label-cancelled' 
	                                        	: (deploy.status eq 'warning' ? 'label-warning' 
	                                        	: ''))))
	                                        }"
									id="deploy_status_${deploy.id}">${deploy.status}</span>
							</a>
						</div>
						<div id="deploy-${deploy.id}" class="accordion-body collapse <c:if test="${nav_status.first}">in</c:if>">
							<div class="accordion-inner">
								<ul class="nav nav-tabs nav-stacked" style="margin-bottom: 0;">
									<c:forEach var="entry" items="${deploy.hosts}">
										<c:set var="host" value="${entry.value}" />
										<li class="host_status" id="${deploy.id}:${host.ip}" data-offset="${host.offset}"><a href="#">
												${host.ip}
												<div
													class="pull-right progress ${
                                        	host.status eq 'successful' ? 'progress-success' 
	                                        	: (host.status eq 'failed' ? 'progress-danger' 
	                                        	: (host.status eq 'doing' ? 'progress-striped active' 
	                                        	: (host.status eq 'cancelled' ? 'progress-cancelled' 
	                                        	: (host.status eq 'warning' ? 'progress-warning' 
	                                        	: ''))))
	                                        }">
													<div class="bar" style="width: ${host.progress}%;">
														<div class="step" style="width: 200px; color: #000000;">${host.currentStep}</div>
													</div>
												</div>
											</a></li>
									</c:forEach>
								</ul>
							</div>
						</div>
					</div>
				</c:forEach>
			</div>

			<div class="row" style="margin-top: 5px;">
				<p class="pull-right">
					<span class="label label-pending">pending&nbsp;&nbsp;&nbsp;</span> <span class="label label-doing">doing&nbsp;&nbsp;</span> <span class="label label-warning">warning</span> <span
						class="label label-cancelled">cancelled</span> <span class="label label-success">success</span> <span class="label label-failed">failed&nbsp;</span>
				</p>
			</div>
			<div>
				<span id="current_selected" class="label label-inverse" style="margin-top: 7px; text-transform: capitalize;" meta="${first_deploy.id}">${first_deploy.domain}</span>
				<button id="ctrl_cancel" class="btn btn-danger btn-small pull-right" style="margin: 0 0 0 5px;">Cancel Rest</button>
				<button id="ctrl_continue" class="btn btn-primary btn-small pull-right" style="margin: 0 0 0 5px;">Continue</button>
				<button id="ctrl_pause" class="btn btn-warning btn-small pull-right" style="margin: 0 0 0 5px;">Pause</button>
			</div>
		</div>
		<div class="span8">
			<div class="row-fluid">
				<c:set var="terminal_idx" value="first" />
				<c:forEach var="deploy" items="${model.deploys}">
					<c:forEach var="entry" items="${deploy.hosts}">
						<c:set var="host" value="${entry.value}" />
						<div data-spy="scroll" data-offset="0" style="height: 510px; line-height: 20px; overflow: auto;" class="terminal terminal-like <c:if test="${terminal_idx ne 'first'}">hide</c:if>"
							id="log-${deploy.id}-${host.ip}">
							<c:forEach var="segment" items="${host.segments}">
								<c:if test="${not empty segment.encodedText}">
									<div class="terminal-like">${segment.encodedText}</div>
								</c:if>
							</c:forEach>
						</div>
						<c:set var="terminal_idx" value="non-first" />
					</c:forEach>
				</c:forEach>
			</div>
		</div>
	</div>

	<res:useJs value="${res.js.local.deploy_js}" target="deploy-js" />
	<res:useCss value='${res.css.local.deploy_css}' target="head-css" />
	<res:jsSlot id="deploy-js" />
	<res:cssSlot id="deploy-css" />
</a:layout>