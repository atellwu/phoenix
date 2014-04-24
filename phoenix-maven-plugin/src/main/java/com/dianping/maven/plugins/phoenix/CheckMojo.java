package com.dianping.maven.plugins.phoenix;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.unidal.helper.Files;
import org.unidal.helper.Splitters;
import org.unidal.helper.Urls;

/**
 * A phoenix mojo focuses on following objectives:
 * <ul>
 * <li>if a project's dependencies meets production rules.</li>
 * </ul>
 * 
 * @goal check
 * @requiresDependencyResolution compile
 */
public class CheckMojo extends AbstractMojo {
	/**
	 * Current project
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject m_project;

	/**
	 * Optional default dependencies to check.
	 * <p>
	 * 
	 * @parameter expression="${dependencies}"
	 */
	private List<String> dependencies;

	/**
	 * Optional remote configured dependencies to check. it will override the default dependencies.
	 * <p>
	 * 
	 * @parameter expression="${checklist}" default-value="http://code.dianpingoa.com/api/v3/artifacts"
	 */
	private String checklist;

	/**
	 * Verbose information or not
	 * 
	 * @parameter expression="${verbose}" default-value="false"
	 */
	private boolean verbose;

	private Map<String, String> m_verisons = new LinkedHashMap<String, String>();

	private boolean checkVersion(Artifact artifact) {
		String key = artifact.getGroupId() + ":" + artifact.getArtifactId();
		String version = artifact.getVersion();
		String thresholdVersion = m_verisons.get(key);

		return validateVersion(key, version, thresholdVersion);
	}

	private void checkVersions() throws MojoFailureException {
		Set<Artifact> artifacts = m_project.getArtifacts();
		boolean valid = true;

		for (Artifact artifact : artifacts) {
			if (!checkVersion(artifact)) {
				valid = false;
			}
		}

		if (valid) {
			if (verbose) {
				getLog().info(String.format("All dependencies(%s) are valid!", artifacts.size()));
			}
		} else { // when all is done
			throw new MojoFailureException("Failed to check versions!");
		}
	}

	private String downloadChecklist() {
		if (verbose) {
			getLog().info(String.format("Downloading checklist from %s ...", checklist));
		}

		try {
			InputStream in = Urls.forIO().connectTimeout(2000).readTimeout(2000).openStream(checklist);
			String content = Files.forIO().readFrom(in, "utf-8").trim();

			if (content.length() >= 2 && content.startsWith("\"") && content.endsWith("\"")) {
				content = content.substring(1, content.length() - 1);
			}

			if (verbose) {
				getLog().info(String.format("%s bytes downloaded.", content.length()));
				getLog().info("");
			}
			return content;
		} catch (Exception e) {
			getLog().warn(String.format("Failed when downloading checklist from %s, IGNORED", checklist));
			getLog().warn(e);
		}

		return null;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		prepareVersions();
		printVersions();
		checkVersions();
	}

	private boolean isNotEmpty(String str) {
		return str != null && str.length() > 0;
	}

	private void prepareVersionMap(String artifact) {
		List<String> parts = Splitters.by(':').split(artifact);
		String groupId = parts.size() > 0 ? parts.get(0) : null;
		String artifactId = parts.size() > 1 ? parts.get(1) : null;
		String version = parts.size() > 2 ? parts.get(2) : null;

		if (isNotEmpty(groupId) && isNotEmpty(artifactId) && isNotEmpty(version)) {
			String key = groupId + ":" + artifactId;

			m_verisons.put(key, version);
		} else {
			getLog().warn(String.format("Invalid artifact(%s)!", artifact));
		}
	}

	private void prepareVersions() {
		// from gitpub
		if (checklist != null) {
			String content = downloadChecklist();

			if (content != null) {
				List<String> list = Splitters.by(',').noEmptyItem().trim().split(content);

				for (String item : list) {
					prepareVersionMap(item);
				}
			}
		}

		// from plugin confiugration
		if (dependencies != null) {
			for (String dependency : dependencies) {
				prepareVersionMap(dependency);
			}
		}
	}

	private void printVersions() {
		if (verbose) {
			getLog().info(String.format("%s artifacts version to be checked:", m_verisons.size()));

			for (Map.Entry<String, String> e : m_verisons.entrySet()) {
				getLog().info(e.getKey() + ":" + e.getValue());
			}

			getLog().info("");
		}
	}

	private boolean validateVersion(String key, String current, String threshold) {
		if (threshold == null) {
			return true;
		}

		if (verbose) {
			getLog().info(String.format("Checking %s:%s for threshold(%s) ...", key, current, threshold));
		}

		ArtifactVersion currentVersion = new DefaultArtifactVersion(current);
		ArtifactVersion thresholdVersion = new DefaultArtifactVersion(threshold);
		boolean valid = currentVersion.compareTo(thresholdVersion) >= 0;

		if (!valid) {
			getLog().error(String.format("Expected version of dependency(%s) is %s or above, " //
			      + "but was %s!", key, threshold, current));
		}

		return valid;
	}
}
