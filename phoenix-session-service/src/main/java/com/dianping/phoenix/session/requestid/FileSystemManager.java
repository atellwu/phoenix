package com.dianping.phoenix.session.requestid;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.phoenix.configure.ConfigManager;

public class FileSystemManager implements Initializable {
	@Inject
	private ConfigManager m_configManager;

	private String m_defaultBaseDir;

	private Configuration m_config;

	public FileSystem getFileSystem(StringBuilder basePath) throws IOException {
		String serverUri = m_configManager.getHdfsServerUri();
		FileSystem fs;

		if (serverUri == null || !serverUri.startsWith("hdfs:")) {
			// use local HDFS
			fs = FileSystem.getLocal(m_config);
			basePath.append(m_defaultBaseDir).append("/");
		} else {
			URI uri = URI.create(serverUri);
			fs = FileSystem.get(uri, m_config);
			basePath.append("phoenix");
		}

		return fs;
	}

	// prepare file /etc/krb5.conf
	// prepare file /data/appdatas/cat/cat.keytab
	// prepare mapping [host] => [ip] at /etc/hosts
	// put core-site.xml at / of classpath
	// use "hdfs://dev80.hadoop:9000/user/cat" as example. Notes: host name can't
	// be an ip address
	private Configuration getHdfsConfiguration() throws IOException {
		Configuration config = new Configuration();
		Map<String, String> properties = m_configManager.getHdfsProperties();
		String authentication = properties.get("hadoop.security.authentication");

		config.setInt("io.file.buffer.size", 8192);

		for (Map.Entry<String, String> property : properties.entrySet()) {
			config.set(property.getKey(), property.getValue());
		}

		if ("kerberos".equals(authentication)) {
			// For MAC OS X
			// -Djava.security.krb5.realm=OX.AC.UK
			// -Djava.security.krb5.kdc=kdc0.ox.ac.uk:kdc1.ox.ac.uk
			System.setProperty("java.security.krb5.realm",
			      getProperty(properties, "java.security.krb5.realm", "DIANPING.COM"));
			System.setProperty("java.security.krb5.kdc", getProperty(properties, "java.security.krb5.kdc", "192.168.7.80"));

			UserGroupInformation.setConfiguration(config);
		}

		return config;
	}

	private String getProperty(Map<String, String> properties, String name, String defaultValue) {
		String value = properties.get(name);

		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_defaultBaseDir = m_configManager.getHdfsLocalBaseDir();

		try {
			m_config = getHdfsConfiguration();
			SecurityUtil.login(m_config, "dfs.cat.keytab.file", "dfs.cat.kerberos.principal");
		} catch (IOException e) {
			Cat.logError(e);
		}
	}
}
