package com.dianping.phoenix.console.page.deploy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.dianping.phoenix.deploy.DeployConstant;
import com.dianping.phoenix.deploy.model.entity.DeployModel;
import com.dianping.phoenix.deploy.model.entity.HostModel;
import com.dianping.phoenix.deploy.model.entity.SegmentModel;
import com.dianping.phoenix.deploy.model.transform.BaseVisitor;

class StatusModelVisitor extends BaseVisitor {
	private DeployModel m_model;

	private Map<Integer, Map<String, Integer>> m_map;

	public StatusModelVisitor(Map<Integer, Map<String, Integer>> map) {
		m_map = map != null ? map : Collections.<Integer, Map<String, Integer>> emptyMap();
	}

	private String escape(String str) {
		int len = str.length();
		StringBuilder sb = new StringBuilder(len + 32);

		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);

			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\r':
				break;
			case '\n':
				sb.append("<br>");
				break;
			default:
				sb.append(ch);
				break;
			}
		}

		return sb.toString();
	}

	public DeployModel getModel() {
		return m_model;
	}

	@Override
	public void visitDeploy(DeployModel other) {
		m_model = new DeployModel();
		m_model.mergeAttributes(other);

		super.visitDeploy(other);

		HostModel summary = m_model.findHost(DeployConstant.SUMMARY);
		int size = other.getHosts().size();
		int progress = 0;

		if (summary == null) { // it always has summary
			Integer offset = m_map.get(other.getId()).get(DeployConstant.SUMMARY);

			summary = new HostModel(DeployConstant.SUMMARY);
			summary.setOffset(offset == null ? 0 : offset);
			m_model.addHost(summary);
		}

		for (HostModel host : other.getHosts().values()) {
			if (!host.getIp().equals(DeployConstant.SUMMARY)) {
				int p = 0;

				for (SegmentModel segment : host.getSegments()) {
					if (segment.getTotalTicks() > 0) {
						p = segment.getCurrentTicks() * 100 / segment.getTotalTicks();
					}
				}

				progress += p;
			}
		}

		summary.setProgress(progress / (size > 1 ? size - 1 : 1));
		summary.setStatus(other.getStatus());
		m_model.setStatus(other.getStatus());
		m_model.setPlan(other.getPlan());
	}

	@Override
	public void visitHost(HostModel other) {
		String ip = other.getIp();
		List<SegmentModel> segments = other.getSegments();
		int size = segments.size();
		Integer index = m_map.get(m_model.getId()).get(ip);
		StringBuilder sb = new StringBuilder(1024);
		int progress = 0;
		String step = null;
		String status = null;

		if (index == null) {
			index = 0;
		}

		for (int i = 0; i < index && i < size; i++) {
			SegmentModel segment = segments.get(i);

			if (segment.getStep() != null) {
				step = segment.getStep();
			}
		}

		for (int i = index; i < size; i++) {
			SegmentModel segment = segments.get(i);

			if (segment.getTotalTicks() > 0) {
				progress = segment.getCurrentTicks() * 100 / segment.getTotalTicks();
			}

			if (segment.getStep() != null) {
				step = segment.getStep();
			}

			if (segment.getStatus() != null) {
				status = segment.getStatus();
			}

			String text = segment.getText();

			if (text != null) {
				sb.append(text.trim()).append("\r\n");
			}
		}

		if (index < size) {
			HostModel host = new HostModel().setIp(ip);

			host.setProgress(progress);
			host.setCurrentStep(step);
			host.setStatus(status);
			host.setLog(escape(sb.toString()));
			host.setOffset(size);
			m_model.addHost(host);
		}
	}
}