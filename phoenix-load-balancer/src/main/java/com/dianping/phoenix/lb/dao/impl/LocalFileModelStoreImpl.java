/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-18
 * 
 */
package com.dianping.phoenix.lb.dao.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.dianping.phoenix.lb.dao.ModelStore;
import com.dianping.phoenix.lb.model.configure.entity.Configure;
import com.dianping.phoenix.lb.model.configure.entity.VirtualServer;
import com.dianping.phoenix.lb.model.configure.transform.DefaultMerger;
import com.dianping.phoenix.lb.model.configure.transform.DefaultSaxParser;

/**
 * @author Leo Liang
 * 
 */
public class LocalFileModelStoreImpl extends AbstractModelStore implements ModelStore {

    private static final Logger                  log                     = Logger.getLogger(LocalFileModelStoreImpl.class);

    private String                               baseDir;
    private static final String                  BASE_CONFIG_FILE_SUFFIX = "_base";
    private static final String                  TAG_DIR                 = "tag";
    private static final String                  CONFIG_FILE_PREFIX      = "configure_";
    private static final String                  XML_SUFFIX              = ".xml";
    private static final String                  TAGID_SEPARATOR         = "_";
    private static final String                  TAGID_SPLITTER          = "-";
    private ConcurrentMap<String, AtomicInteger> tagMetas                = new ConcurrentHashMap<String, AtomicInteger>();

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    protected void initCustomizedMetas() {
        initTagMetas();
    }

    @Override
    protected void initConfigMetas() {
        try {
            File baseDirFile = new File(baseDir);
            if (baseDirFile.exists() && baseDirFile.isDirectory()) {

                Collection<File> subFiles = FileUtils.listFiles(baseDirFile, new String[] { "xml" }, false);

                for (File subFile : subFiles) {
                    String fileName = subFile.getName();
                    if (fileName.startsWith(CONFIG_FILE_PREFIX)) {
                        String xml = FileUtils.readFileToString(subFile);
                        Configure tmpConfigure = DefaultSaxParser.parse(xml);

                        if (fileName.endsWith(BASE_CONFIG_FILE_SUFFIX + XML_SUFFIX)) {
                            baseConfigMeta = new ConfigMeta(fileName, tmpConfigure);
                        } else {
                            for (Map.Entry<String, VirtualServer> entry : tmpConfigure.getVirtualServers().entrySet()) {
                                virtualServerConfigFileMapping.put(entry.getKey(), new ConfigMeta(fileName,
                                        tmpConfigure));
                            }
                        }

                        new DefaultMerger().merge(configure, tmpConfigure);
                    }
                }

            } else {
                if (!baseDirFile.exists()) {
                    FileUtils.forceMkdir(baseDirFile);
                } else {
                    throw new IOException(String.format("%s already exists but not a dir.", baseDir));
                }
            }
        } catch (Exception e) {
            log.error("Init local file model's configMetas store failed.");
            throw new RuntimeException("Init local file model's configMetas store failed.", e);
        }
    }

    protected void save(String key, Configure configure) throws IOException {
        doSave(new File(baseDir, key), configure);
    }

    private void doSave(File file, Configure configure) throws IOException {
        FileUtils.writeStringToFile(file, configure.toString());
    }

    protected boolean delete(String key) {
        return new File(baseDir, key).delete();
    }

    protected String convertToKey(String virtualServerName) {
        return CONFIG_FILE_PREFIX + virtualServerName + XML_SUFFIX;
    }

    protected void initTagMetas() {
        try {
            File baseDirFile = new File(baseDir, TAG_DIR);
            if (baseDirFile.exists() && baseDirFile.isDirectory()) {

                File[] virtualServers = baseDirFile.listFiles();

                for (File subFile : virtualServers) {
                    String vsName = subFile.getName();

                    File[] tagFolders = new File(baseDirFile, vsName).listFiles();
                    List<File> tags = new ArrayList<File>();

                    for (File tagFolder : tagFolders) {
                        File[] files = tagFolder.listFiles();
                        if (files != null && files.length > 0) {
                            tags.addAll(Arrays.asList(files));
                        }
                    }

                    for (File tag : tags) {
                        String fileName = tag.getName();
                        if (fileName.startsWith(CONFIG_FILE_PREFIX)) {
                            int tagIdStart = fileName.lastIndexOf(TAGID_SEPARATOR);
                            int extStart = fileName.lastIndexOf(XML_SUFFIX);
                            if (tagIdStart != -1 && extStart != -1 && extStart < tagIdStart) {
                                String tagIdStr = fileName.substring(tagIdStart + 1);
                                if (StringUtils.isNumeric(tagIdStr)) {
                                    String xml = FileUtils.readFileToString(tag);
                                    Configure tmpConfigure = DefaultSaxParser.parse(xml);
                                    int tagId = Integer.valueOf(tagIdStr);
                                    for (Map.Entry<String, VirtualServer> entry : tmpConfigure.getVirtualServers()
                                            .entrySet()) {
                                        tagMetas.putIfAbsent(entry.getKey(), new AtomicInteger(0));
                                        if (tagMetas.get(entry.getKey()).intValue() < tagId) {
                                            tagMetas.get(entry.getKey()).set(tagId);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                if (!baseDirFile.exists()) {
                    FileUtils.forceMkdir(baseDirFile);
                } else {
                    throw new IOException(String.format("%s already exists but not a dir.", baseDir));
                }
            }
        } catch (Exception e) {
            log.error("Init local file model's configMetas store failed.");
            throw new RuntimeException("Init local file model's configMetas store failed.", e);
        }
    }

    @Override
    protected String saveTag(String key, String vsName, Configure configure) throws IOException {
        tagMetas.putIfAbsent(vsName, new AtomicInteger(0));
        int tagId = tagMetas.get(vsName).incrementAndGet();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        File tagFile = new File(new File(getTagFileBase(vsName), sdf.format(new Date())), getTagFileName(key, tagId));
        FileUtils.forceMkdir(tagFile.getParentFile());
        doSave(tagFile, configure);
        return convertToStrTagId(vsName, tagId);
    }

    private String convertToStrTagId(String vsName, int tagId) {
        return vsName + TAGID_SPLITTER + tagId;
    }

    private Integer convertFromStrTagId(String vsName, String tagId) {
        if (tagId.startsWith(vsName + TAGID_SPLITTER)) {
            String[] tagIdSplits = tagId.split(TAGID_SPLITTER);
            if (tagIdSplits != null && tagIdSplits.length == 2 && StringUtils.isNumeric(tagIdSplits[1])) {
                return Integer.valueOf(tagIdSplits[1]);
            }
        }
        return null;
    }

    private String getTagFileName(String key, int tagId) {
        return key + TAGID_SEPARATOR + tagId;
    }

    @Override
    protected Configure loadTag(String key, String vsName, String tagId) throws IOException, SAXException {
        Integer tagIdInt = convertFromStrTagId(vsName, tagId);
        if (tagIdInt != null) {
            File tagBase = getTagFileBase(vsName);
            String tagFileName = getTagFileName(key, tagIdInt);
            File tagFile = null;
            for (File subFolder : tagBase.listFiles()) {
                if (tagFile == null) {
                    for (File tmpTagFile : subFolder.listFiles()) {
                        if (tmpTagFile.getName().equals(tagFileName)) {
                            tagFile = tmpTagFile;
                            break;
                        }
                    }
                }
            }

            if (tagFile != null) {
                return DefaultSaxParser.parse(FileUtils.readFileToString(tagFile));
            }
        }
        return null;
    }

    private File getTagFileBase(String vsName) {
        return new File(new File(baseDir, TAG_DIR), vsName);
    }

    @Override
    protected List<String> doListTagIds(String vsName) throws IOException {
        File tagFileBase = getTagFileBase(vsName);
        if (tagFileBase.exists() && tagFileBase.isDirectory()) {
            File[] tagFolders = tagFileBase.listFiles();
            List<String> fileNames = new ArrayList<String>();
            for (File tagFolder : tagFolders) {
                String[] tagFiles = tagFolder.list();
                if (tagFiles != null && tagFiles.length > 0) {
                    fileNames.addAll(Arrays.asList(tagFiles));
                }
            }

            List<String> tagIds = new ArrayList<String>();
            for (String fileName : fileNames) {
                int tagIdStart = fileName.lastIndexOf(TAGID_SEPARATOR);
                if (tagIdStart != -1) {
                    String tagIdStr = fileName.substring(tagIdStart + 1);
                    if (StringUtils.isNumeric(tagIdStr)) {
                        tagIds.add(convertToStrTagId(vsName, Integer.parseInt(tagIdStr)));
                    }
                }
            }
            return tagIds;
        }
        return null;
    }

    @Override
    protected String doFindPrevTagId(String virtualServerName, String currentTagId, List<String> tagIds) {
        Integer currentTagIdInt = convertFromStrTagId(virtualServerName, currentTagId);

        if (currentTagIdInt != null) {

            Integer prevTagId = null;

            for (String lastTagId : tagIds) {
                Integer lastTagIdInt = convertFromStrTagId(virtualServerName, lastTagId);
                if (lastTagIdInt != null) {
                    if (lastTagIdInt < currentTagIdInt) {
                        if (prevTagId == null) {
                            prevTagId = lastTagIdInt;
                        } else {
                            if (lastTagIdInt > prevTagId) {
                                prevTagId = lastTagIdInt;
                            }
                        }
                    }
                }
            }
            return prevTagId == null ? null : convertToStrTagId(virtualServerName, prevTagId);
        }

        return null;
    }
}
