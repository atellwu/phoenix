package com.dianping.phoenix.dev.core.tools.velocity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.dianping.phoenix.dev.core.tools.wms.WorkspaceConstants;

public class PhoenixResourceLoader extends ClasspathResourceLoader {

	private static File wsDir;

	public static void setWsDir(File wsDir) {
		PhoenixResourceLoader.wsDir = wsDir;
	}

	@Override
	public InputStream getResourceStream(String name) throws ResourceNotFoundException {

		File resourceOnFS = new File(new File(wsDir, WorkspaceConstants.PHOENIX_CONFIG_FOLDER), name);
		if (resourceOnFS.isFile()) {
			try {
				return new FileInputStream(resourceOnFS);
			} catch (FileNotFoundException e) {
				// ignore it
			}
		}
		return super.getResourceStream(name);
	}

}
