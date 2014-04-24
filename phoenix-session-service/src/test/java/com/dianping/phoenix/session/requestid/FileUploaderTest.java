package com.dianping.phoenix.session.requestid;

import java.io.IOException;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

public class FileUploaderTest extends ComponentTestCase {
	public void test() throws IOException {
		FileUploader uploader = lookup(FileUploader.class);

		Threads.forGroup("Phoenix").start(uploader);
	}
}
