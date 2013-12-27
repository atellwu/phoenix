package com.dianping.phoenix.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.unidal.helper.Files;
import org.unidal.helper.Files.IO;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.data.warehouse.LogTypeEnum;
import com.dianping.data.warehouse.MarinLog;
import com.dianping.data.warehouse.MarinPrinter;

public class IntegrationTest extends ComponentTestCase {

	private DefaultHttpClient m_hc = new DefaultHttpClient();
	
	public static void main(String[] args) throws Exception {
	   new IntegrationTest().startServer();
   }

	public void startServer() throws Exception {
		File baseDir = new File("/data/appdatas/phoenix/record-done/");
		Files.forDir().delete(baseDir, true);
		findDpLogFile().delete();

		Server server = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		webapp.setDescriptor("src/test/webapp/WEB-INF/web.xml");
		webapp.setResourceBase("src/test/webapp/p");
		webapp.setClassLoader(this.getClass().getClassLoader());
		server.setHandler(webapp);

		server.start();
		
		System.in.read();
		
		System.exit(0);
	}

	@SuppressWarnings("unchecked")
//	@Test
	public void test() throws Exception {

		File baseDir = new File("/data/appdatas/phoenix/record-done/");
		Files.forDir().delete(baseDir, true);
		findDpLogFile().delete();

		Server server = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		webapp.setDescriptor("src/test/webapp/WEB-INF/web.xml");
		webapp.setResourceBase("src/test/webapp/p");
		webapp.setClassLoader(this.getClass().getClassLoader());
		server.setHandler(webapp);

		server.start();
		
		sendRequest("1", null);
		sendRequest("2", "1");
		sendRequest("3", "1");
		sendRequest("3", "2");
		sendRequest("2", "3");

		Thread.sleep(4000);
		List<String> recLines = new ArrayList<String>();

		boolean fromProject = true;
		if (fromProject) {
			File recFile = findRecordFile(baseDir);
			if (recFile == null) {
				fail("No record file found under " + baseDir.getCanonicalPath());
			} else {
				recLines = FileUtils.readLines(recFile, "utf-8");
			}
		} else {
			File recFile1 = findRecordFile(new File(
			      "/Volumes/HDD2/MountainLion/Downloads/tomcat/apache-tomcat-6.0.36/webapps/target"));
			File recFile2 = findRecordFile(new File(
			      "/Volumes/HDD2/MountainLion/Downloads/tomcat/apache-tomcat-6.0.36/webapps/target2"));
			
			if(recFile1 != null) {
				recLines.addAll(FileUtils.readLines(recFile1, "utf-8"));
			}
			
			if(recFile2 != null) {
				recLines.addAll(FileUtils.readLines(recFile2, "utf-8"));
			}
		}
		
		// request id is increment by nature
		Collections.sort(recLines);
		
		List<String> dpLogLines = FileUtils.readLines(findDpLogFile(), "utf-8");

		assertEquals(4, recLines.size());
		assertEquals(5, dpLogLines.size());

		String req1Id = (dpLogReqId(dpLogLines.get(0)));
		String req2Id = (dpLogReqId(dpLogLines.get(1)));
		String req3Id = (dpLogReqId(dpLogLines.get(2)));
		String req4Id = (dpLogReqId(dpLogLines.get(3)));
		String req5Id = (dpLogReqId(dpLogLines.get(4)));

		HashSet<String> reqIdSet = new HashSet<String>();
		reqIdSet.add(req1Id);
		reqIdSet.add(req2Id);
		reqIdSet.add(req3Id);
		reqIdSet.add(req4Id);
		reqIdSet.add(req5Id);
		assertEquals(5, reqIdSet.size());

		String recLine1 = recLines.get(0);
		assertEquals(req2Id, recReqId(recLine1));
		assertEquals(req1Id, recReferReqId(recLine1));

		String recLine2 = recLines.get(1);
		assertEquals(req3Id, recReqId(recLine2));
		assertEquals(req1Id, recReferReqId(recLine2));

		String recLine3 = recLines.get(2);
		assertEquals(req4Id, recReqId(recLine3));
		assertEquals(req2Id, recReferReqId(recLine3));

		String recLine4 = recLines.get(3);
		assertEquals(req5Id, recReqId(recLine4));
		assertEquals(req4Id, recReferReqId(recLine4));
		
	}

	private File findDpLogFile() {
		return new File("/data/applogs/xen/logs/test.log");
	}

	private String recReferReqId(String line) {
		return line.split("\t")[1];
	}

	private String recReqId(String line) {
		return line.split("\t")[0];
	}

	private String dpLogReqId(String line) {
		String kv = line.split(new String(new char[] { 5 }))[6];
		return kv.split(new String(new char[] { 4 }))[1];
	}

	@SuppressWarnings("unchecked")
	private File findRecordFile(File baseDir) throws IOException {
		Collection<File> files = FileUtils.listFiles(baseDir, new IOFileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile();
			}

			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isFile();
			}

		}, new IOFileFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}

			@Override
			public boolean accept(File file) {
				return true;
			}
		});

		File recFile = null;
		for (File file : files) {
			if (file.isFile()) {
				if (recFile == null) {
					recFile = file;
				} else {
					throw new RuntimeException("Multiple record file found under " + baseDir.getCanonicalPath());
				}
			}
		}
		return recFile;
	}

	private String sendRequest(String url, String referUrl) throws Exception {
		String urlPrefix = "http://127.0.0.1:8080/";
		HttpGet req = new HttpGet(urlPrefix + url);

		if (referUrl != null) {
			req.setHeader("Referer", urlPrefix + referUrl);
		}

		HttpResponse res = m_hc.execute(req);

		return IO.INSTANCE.readFrom(res.getEntity().getContent(), "utf-8");
	}

	private static class LogHandler {
		private static MarinPrinter printer = null;

		public static MarinPrinter newInstance() {
			if (null == printer) {
				init();
			}

			return printer;
		}

		private static synchronized void init() {
			if (null == printer) {
				printer = new MarinPrinter();
				printer.setBusiness("xen");
				printer.setFileName("test");
				printer.setType(LogTypeEnum.FILE);
				printer.init();
			}
		}
	}

	public static class ExtryServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			MarinPrinter printer = LogHandler.newInstance();
			MarinLog log = new MarinLog();
			log.putString("by", "marsqing");
			printer.print(log);

			resp.getWriter().write("<html><body><a href='yy'>yy</a></body></html>");
		}

	}

}
