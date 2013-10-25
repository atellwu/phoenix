package com.dianping.phoenix.environment.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ResourceUtils {

    /**
     * for all elements of java.class.path get a Collection of resources Pattern pattern = Pattern.compile(".*"); gets all resources
     * 
     * @param pattern the pattern to match
     * @return the resources in the order they are found(key是完全路径，value是字节码) 文件优先级低的在前面，优先级高的在后面（文件名一样的话，后面覆盖前面）
     * @throws IOException
     */
    public static Map<String, byte[]> getResources(final Pattern pattern) throws IOException {
        final Map<String, byte[]> retval = new LinkedHashMap<String, byte[]>();
        final String classPath = System.getProperty("java.class.path", ".");

        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for (int i = classPathElements.length - 1; i >= 0; i--) {//优先级高的classpath放后面，因为Map.putAll后面会覆盖前面
            String element = classPathElements[i];
            retval.putAll(getResources(element, pattern));
        }
        return retval;
    }

    private static Map<String, byte[]> getResources(final String element, final Pattern pattern) throws IOException {
        final Map<String, byte[]> retval = new LinkedHashMap<String, byte[]>();
        final File file = new File(element);
        if (file.isDirectory()) {
            retval.putAll(getResourcesFromDirectory(file, pattern));
        } else {
            retval.putAll(getResourcesFromJarFile(file, pattern));
        }
        return retval;
    }

    @SuppressWarnings("rawtypes")
    private static Map<String, byte[]> getResourcesFromJarFile(final File file, final Pattern pattern) throws IOException {
        final Map<String, byte[]> retval = new LinkedHashMap<String, byte[]>();
        ZipFile zf;
        try {
            zf = new ZipFile(file);
        } catch (final ZipException e) {
            throw new Error(e);
        } catch (final IOException e) {
            throw new Error(e);
        }
        final Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
            final ZipEntry zipEntry = (ZipEntry) e.nextElement();

            final String fileName = zipEntry.getName();
            final boolean accept = pattern.matcher(getSimpleName(fileName)).matches();
            if (accept) {
                InputStream zis = zf.getInputStream(zipEntry);
                byte[] buf = new byte[(int) zipEntry.getSize()];
                zis.read(buf, 0, (int) zipEntry.getSize());
                retval.put(fileName, buf);
            }
        }
        try {
            zf.close();
        } catch (final IOException e1) {
            throw new Error(e1);
        }
        return retval;
    }

    private static Map<String, byte[]> getResourcesFromDirectory(final File directory, final Pattern pattern) throws IOException {
        final Map<String, byte[]> retval = new LinkedHashMap<String, byte[]>();
        final File[] fileList = directory.listFiles();
        for (final File file : fileList) {
            if (file.isDirectory()) {
                retval.putAll(getResourcesFromDirectory(file, pattern));
            } else {
                final String fileName = file.getName();
                final boolean accept = pattern.matcher(fileName).matches();
                if (accept) {
                    retval.put(file.getAbsolutePath(), toByte(file));
                }
            }
        }
        return retval;
    }

    private static String getSimpleName(String fileName) {
        int index = fileName.lastIndexOf('/');
        if (index != -1) {
            return fileName.substring(index + 1);
        }
        return fileName;
    }

    private static byte[] toByte(File file) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }

    public static void main(String[] args) throws IOException {
        Pattern PATTERN = Pattern.compile("spring-task-3\\.1\\.xsd");
        System.out.println(ResourceUtils.getResources(PATTERN));
    }
}
