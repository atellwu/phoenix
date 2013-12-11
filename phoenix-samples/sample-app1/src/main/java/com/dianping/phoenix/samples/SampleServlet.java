package com.dianping.phoenix.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class SampleServlet extends HttpServlet {

	private static final long serialVersionUID = 8766647945778232606L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();
		DefaultResourceLoader rl = new DefaultResourceLoader(cl);
		Resource r1;
		PrintWriter wr = response.getWriter();

		URL resource = cl.getResource("/WEB-INF/file_to_display.txt");
		try {
			wr.write(resource.toString());
			wr.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// try {
		// r1 = rl.getResource("classpath:/WEB-INF/file_to_display.txt");
		// show(r1, wr);
		// } catch (Exception e) {
		// response.getWriter().write(e.getMessage());
		// response.getWriter().flush();
		// e.printStackTrace();
		// }
		// try {
		//
		// r1 = rl.getResource("classpath:/file_to_display.txt");
		// show(r1, wr);
		// } catch (Exception e) {
		// response.getWriter().write(e.getMessage());
		// response.getWriter().flush();
		// e.printStackTrace();
		// // TODO: handle exception
		// }
		//
		// try {
		// r1 = rl.getResource("classpath:../file_to_display.txt");
		// show(r1, wr);
		// } catch (Exception e) {
		// response.getWriter().write(e.getMessage());
		// response.getWriter().flush();
		// e.printStackTrace();
		// // TODO: handle exception
		// }
		//
		// try {
		// System.out.println(cl.getResourceAsStream("../file_to_display.txt"));
		// System.out.println(cl.getResourceAsStream("/file_to_display.txt"));
		//
		// } catch (Exception e) {
		// response.getWriter().write(e.getMessage());
		// response.getWriter().flush();
		// e.printStackTrace();
		// }
	}

	private void show(Resource r, PrintWriter wr) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(r.getInputStream()));
		String s;
		while ((s = br.readLine()) != null) {
			System.out.println(s);
			wr.write(s);
		}
		wr.flush();
	}

}
