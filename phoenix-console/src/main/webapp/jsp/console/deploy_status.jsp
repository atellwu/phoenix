<%@ page contentType="text/plain; charset=utf-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="ctx" type="com.dianping.phoenix.console.page.deploy.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.phoenix.console.page.deploy.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.phoenix.console.page.deploy.Model" scope="request" />
[
<c:forEach var="deploy" items="${model.deploys}" varStatus="outerStatus">
{
"id":${deploy.id},
"status":"${deploy.status}", 
"hosts": [
<c:forEach var="entry" items="${deploy.hosts}" varStatus="innerStatus">
		<c:set var="host" value="${entry.value}" />
		{
		"host": "${host.ip}",
		"offset": ${host.offset},
		"progress": ${host.progress},
		"step": "${host.currentStep}",
		"status": "${host.status}",
		"log": "${host.log}"
		}
		<c:if test="${not innerStatus.last}">,</c:if>
	</c:forEach>
	]
}
<c:if test="${not outerStatus.last}">,</c:if>
</c:forEach>
]
