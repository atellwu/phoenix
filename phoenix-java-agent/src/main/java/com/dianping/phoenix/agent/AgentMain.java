package com.dianping.phoenix.agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

public class AgentMain {
	private static final String AGENT_BASE = "META-INF/agent";

	private static final String AGENT_PROPERTIES = AGENT_BASE + "/agent.properties";

	private static boolean s_initialized = false;

	private Map<String, URL> m_map = new HashMap<String, URL>();

	private AgentMain() {
		try {
			List<URL> resources = Collections.list(getClass().getClassLoader().getResources(AGENT_PROPERTIES));

			for (URL resource : resources) {
				handleAgentProperties(resource);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void agentmain(String agentArgs, Instrumentation instrument) throws Exception {
		premain(agentArgs, instrument);
	}

	public static void attach() throws Exception {
		if (!s_initialized) {
			if (isDynamicAttachSupported()) {
				String agentJar = getAgentJar();

				for (com.sun.tools.attach.VirtualMachineDescriptor vmd : com.sun.tools.attach.VirtualMachine.list()) {
					com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(vmd);

					vm.loadAgent(agentJar);
				}
			} else {
				throw new IllegalStateException("Dynamic attach is not supported, use -javaagent:<jar> instead!");
			}
		}
	}

	private static String getAgentJar() throws IOException {
		URL url = Thread.currentThread().getContextClassLoader()
		      .getResource(AgentMain.class.getName().replace('.', '/') + ".class");
		String agentJar = null;

		if (url.getProtocol().equals("jar") && url.getPath().startsWith("file:")) {
			String path = url.getPath();
			int pos = path.indexOf('!');

			if (pos > 0) {
				agentJar = path.substring("file:".length(), pos);
			}
		}

		if (agentJar == null) { // for test purpose
			if (new File("target/agent.jar").exists()) {
				agentJar = new File("target/agent.jar").getCanonicalPath();
			} else {
				throw new IllegalStateException("Please run 'mvn package' to prepare agent jar!");
			}
		}

		return agentJar;
	}

	private static boolean isDynamicAttachSupported() {
		try {
			Class.forName("com.sun.tools.attach.VirtualMachine");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static Transformer newTransformer() {
		return new AgentMain().new Transformer();
	}

	public static void premain(String agentArgs, Instrumentation instrument) throws Exception {
		String agentJar = getAgentJar();

		instrument.addTransformer(newTransformer(), true);
		instrument.appendToBootstrapClassLoaderSearch(new JarFile(agentJar));
		s_initialized = true;
	}

	private byte[] getTransformedBytecode(String className) throws IOException {
		URL url = m_map.get(className);

		if (url != null) {
			return readAll(url.openStream());
		}

		return null;
	}

	private void handleAgentProperties(URL url) throws IOException {
		Properties properties = new Properties();
		InputStream in = url.openStream();

		try {
			properties.load(in);

			for (Object key : properties.keySet()) {
				prepareClasss((String) key);
			}
		} finally {
			in.close();
		}
	}

	private void prepareClasss(String className) {
		String path = AGENT_BASE + "/" + className;
		URL url = Thread.currentThread().getContextClassLoader().getResource(path);

		if (url != null && validateClass(url)) {
			m_map.put(className.replace('.', '/'), url);
		} else {
			System.err.println(String.format("Resource(%s) not found!", path));
		}
	}

	private byte[] readAll(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);
		byte[] content = new byte[4096];

		try {
			while (true) {
				int size = is.read(content);

				if (size == -1) {
					break;
				} else {
					baos.write(content, 0, size);
				}
			}
		} finally {
			is.close();
		}

		return baos.toByteArray();
	}

	private boolean shouldTransfrom(String className) {
		return m_map.containsKey(className);
	}

	private boolean validateClass(URL url) {
		// TODO validate the class byte code before loading it
		return true;
	}

	class Transformer implements ClassFileTransformer {
		public byte[] transform(ClassLoader l, String className, Class<?> c, ProtectionDomain pd, byte[] b)
		      throws IllegalClassFormatException {
			if (shouldTransfrom(className)) {
				try {
					byte[] bc = getTransformedBytecode(className);

					return bc;
				} catch (Throwable e) {
					// ignore it, but logged to console
					new RuntimeException("Unable to transform class:" + className + "!", e).printStackTrace();
				}
			}

			// no transform
			return null;
		}
	}
}
