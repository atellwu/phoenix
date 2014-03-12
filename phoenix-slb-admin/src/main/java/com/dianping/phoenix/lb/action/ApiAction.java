package com.dianping.phoenix.lb.action;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.executor.TaskExecutor;
import com.dianping.phoenix.lb.facade.PoolFacade;
import com.dianping.phoenix.lb.model.entity.Member;
import com.dianping.phoenix.lb.utils.JsonBinder;

/**
 * @author wukezhu
 */
@Component("apiAction")
@Scope("prototype")
public class ApiAction extends MenuAction {

	private static final Logger LOG = LoggerFactory.getLogger(ApiAction.class);

	private static final long serialVersionUID = -1084994778030229218L;

	private String poolName;

	@Autowired
	private PoolFacade poolFacade;

	/**
	 * usage:<br>
	 * post http://localhost:8080/api/pool/localhost/addMember <br>
	 * 
	 * post内容(要求是json格式)：<br>
	 * [{"name":"a", "ip":"125.2.3.2"}] <br>
	 * 其中name是节点名称，ip是节点地址，另外还有可选属性，端口port(默认是8080)，权重weight（默认是100），最大失败次数maxFails（默认是3），失败超时时间failTimeout（2s），状态state（枚举值：ENABLED,
	 * DISABLED, FORCED_OFFLINE）
	 * 
	 */
	@SuppressWarnings("unchecked")
	public String addMember() throws Exception {
		try {
			String membersJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
			if (StringUtils.isBlank(membersJson)) {
				throw new IllegalArgumentException("member 参数不能为空！");
			}

			List<Member> members = JsonBinder.getNonNullBinder().fromJson(membersJson, List.class, Member.class);

			poolFacade.addMember(poolName, members);

			dataMap.put("errorCode", ERRORCODE_SUCCESS);
		} catch (IllegalArgumentException e) {
			dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
			dataMap.put("message", e.getMessage());
			LOG.error("Param Error: " + e.getMessage());
		} catch (Exception e) {
			dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
			dataMap.put("message", e.getMessage());
			LOG.error(e.getMessage(), e);
		}
		return SUCCESS;
	}

	/**
	 * usage:<br>
	 * post http://localhost:8080/api/pool/localhost/delMember <br>
	 * 
	 * post内容(要求是json格式)：<br>
	 * ["a","b"] , 其中a，b都是节点名称 <br>
	 * 
	 * 响应结果：<br>
	 * 正确示例： {"errorCode":0} <br>
	 * 错误示例： {"message":"Pool localhost has no member.","errorCode":-1} <br>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public String delMember() throws Exception {
		try {
			String membersJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
			if (StringUtils.isBlank(membersJson)) {
				throw new IllegalArgumentException("member 参数不能为空！");
			}

			List<String> members = JsonBinder.getNonNullBinder().fromJson(membersJson, List.class, String.class);

			poolFacade.delMember(poolName, members);

			dataMap.put("errorCode", ERRORCODE_SUCCESS);
		} catch (IllegalArgumentException e) {
			dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
			dataMap.put("message", e.getMessage());
			LOG.error("Param Error: " + e.getMessage());
		} catch (Exception e) {
			dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
			dataMap.put("message", e.getMessage());
			LOG.error(e.getMessage(), e);
		}
		return SUCCESS;
	}

	public String deploy() throws Exception {
		try {
			TaskExecutor taskExecutor = poolFacade.deploy(poolName);

			DeployTaskBo deployTaskBo = taskExecutor.getDeployTaskBo();

			dataMap.put("taskId", deployTaskBo.getTask().getId());

			while (!deployTaskBo.getTask().getStatus().isCompleted()) {
				TimeUnit.MILLISECONDS.sleep(10);
			}

			if (deployTaskBo.getTask().getStatus().isNotSuccess()) {
				throw new RuntimeException("Task Failed. see detail at http://slb.dp/deploy/task/"
				      + deployTaskBo.getTask().getId());
			}

			dataMap.put("errorCode", ERRORCODE_SUCCESS);
		} catch (IllegalArgumentException e) {
			dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
			dataMap.put("message", e.getMessage());
			LOG.error("Param Error: " + e.getMessage());
		} catch (Exception e) {
			dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
			dataMap.put("message", e.getMessage());
			LOG.error(e.getMessage(), e);
		}
		return SUCCESS;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

}
