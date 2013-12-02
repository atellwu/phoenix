/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-18
 * 
 */
package com.dianping.phoenix.lb.dao.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.dao.ModelStore;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.model.transform.DefaultMerger;
import com.dianping.phoenix.lb.model.transform.DefaultSaxParser;
import com.dianping.phoenix.lb.service.GitService;
import com.dianping.phoenix.lb.utils.ExceptionUtils;

/**
 * @author Leo Liang
 * 
 */
public class LocalFileModelStoreImpl extends AbstractModelStore implements ModelStore {

    private static final Logger                  log                     = Logger.getLogger(LocalFileModelStoreImpl.class);

    private String                               baseDir;
    private static final String                  BASE_CONFIG_FILE_SUFFIX = "_base";
    private static final String                  TAG_DIR                 = "tag";
    private static final String                  CONFIG_FILE_PREFIX      = "slb_";
    private static final String                  XML_SUFFIX              = ".xml";
    private static final String                  TAGID_SEPARATOR         = "_";
    private static final String                  TAGID_SPLITTER          = "-";
    private ConcurrentMap<String, AtomicInteger> tagMetas                = new ConcurrentHashMap<String, AtomicInteger>();
    @Autowired
    private GitService                           gitService;
    private ConfigManager                        configManager;

    private enum FileOP {
        SAVE("Save"), DEL("Delete");
        String name;

        private FileOP(String name) {
            this.name = name;
        }
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public void setGitService(GitService gitService) {
        this.gitService = gitService;
    }

    @Override
    protected void initCustomizedMetas() {
        initTagMetas();
        if (gitService != null) {
            try {
                configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
                gitService.clone(configManager.getModelStoreBaseDir(), baseDir, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
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
                        SlbModelTree tmpSlbModelTree = DefaultSaxParser.parse(xml);

                        if (fileName.endsWith(BASE_CONFIG_FILE_SUFFIX + XML_SUFFIX)) {
                            baseConfigMeta = new ConfigMeta(fileName, tmpSlbModelTree);
                        } else {
                            for (Map.Entry<String, VirtualServer> entry : tmpSlbModelTree.getVirtualServers()
                                    .entrySet()) {
                                virtualServerConfigFileMapping.put(entry.getKey(), new ConfigMeta(fileName,
                                        tmpSlbModelTree));
                            }
                        }

                        new DefaultMerger().merge(slbModelTree, tmpSlbModelTree);
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

    protected void save(String key, SlbModelTree slbModelTree) throws IOException, BizException {
        doSave(new File(baseDir, key), slbModelTree);
    }

    private void doSave(File file, SlbModelTree slbModelTree) throws IOException, BizException {
        FileUtils.writeStringToFile(file, slbModelTree.toString());
        saveToGit(file, FileOP.SAVE);
    }

    private void saveToGit(File file, FileOP op) throws BizException {
        if (gitService != null) {
            gitService.commitAllChanges(baseDir,
                    String.format("%s file %s", op.name, StringUtils.removeStart(file.getAbsolutePath(), baseDir)));
            gitService.push(configManager.getModelGitUrl(), baseDir);
        }
    }

    protected boolean delete(String key) throws BizException {
        File file = new File(baseDir, key);
        boolean success = file.delete();
        saveToGit(file, FileOP.DEL);
        return success;
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

                    List<File> tags = listAllTagFiles(vsName);

                    for (File tag : tags) {
                        String fileName = tag.getName();
                        Integer tagId = extractTagIdInt(vsName, fileName);
                        if (tagId != null) {
                            String xml = FileUtils.readFileToString(tag);
                            SlbModelTree tmpSlbModelTree = DefaultSaxParser.parse(xml);
                            for (Map.Entry<String, VirtualServer> entry : tmpSlbModelTree.getVirtualServers()
                                    .entrySet()) {
                                tagMetas.putIfAbsent(entry.getKey(), new AtomicInteger(0));
                                if (tagMetas.get(entry.getKey()).intValue() < tagId) {
                                    tagMetas.get(entry.getKey()).set(tagId);
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
    protected String saveTag(String key, String vsName, SlbModelTree slbModelTree) throws IOException, BizException {
        tagMetas.putIfAbsent(vsName, new AtomicInteger(0));
        int tagId = tagMetas.get(vsName).incrementAndGet();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        File tagFile = new File(new File(getTagFileBase(vsName), sdf.format(new Date())), getTagFileName(key, tagId));
        FileUtils.forceMkdir(tagFile.getParentFile());
        doSave(tagFile, slbModelTree);
        return convertToStrTagId(vsName, tagId);
    }

    private String convertToStrTagId(String vsName, int tagId) {
        return vsName + TAGID_SPLITTER + tagId;
    }

    private Integer extractTagIdInt(String vsName, String fileName) {
        String vsFileNamePrefix = CONFIG_FILE_PREFIX + vsName + XML_SUFFIX;
        if (fileName.startsWith(vsFileNamePrefix)) {
            int tagIdStart = fileName.lastIndexOf(TAGID_SEPARATOR);
            if (tagIdStart == vsFileNamePrefix.length()) {
                String tagIdStr = fileName.substring(tagIdStart + 1);
                if (StringUtils.isNumeric(tagIdStr)) {
                    return Integer.valueOf(tagIdStr);
                }
            }
        }
        return null;
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
    protected SlbModelTree loadTag(String key, String vsName, String tagId) throws IOException, SAXException {
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

    private List<File> listAllTagFiles(String vsName) {
        List<File> tagFiles = new ArrayList<File>();
        File tagFileBase = getTagFileBase(vsName);
        if (tagFileBase.exists() && tagFileBase.isDirectory()) {
            File[] tagFolders = tagFileBase.listFiles();
            for (File tagFolder : tagFolders) {
                if (tagFolder.isDirectory()) {
                    File[] tmpTagFiles = tagFolder.listFiles(new FileFilter() {

                        @Override
                        public boolean accept(File file) {
                            return file.isFile();
                        }
                    });
                    if (tmpTagFiles != null && tmpTagFiles.length > 0) {
                        tagFiles.addAll(Arrays.asList(tmpTagFiles));
                    }
                }

            }
        }

        return tagFiles;
    }

    @Override
    protected List<String> doListTagIds(final String vsName) throws IOException {
        List<File> tagFiles = listAllTagFiles(vsName);

        List<String> tagIds = new ArrayList<String>();
        for (File tagFile : tagFiles) {
            String fileName = tagFile.getName();
            int tagIdStart = fileName.lastIndexOf(TAGID_SEPARATOR);
            if (tagIdStart != -1) {
                String tagIdStr = fileName.substring(tagIdStart + 1);
                if (StringUtils.isNumeric(tagIdStr)) {
                    tagIds.add(convertToStrTagId(vsName, Integer.parseInt(tagIdStr)));
                }
            }
        }

        Collections.sort(tagIds, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(StringUtils.removeStart(o1, vsName)).compareTo(
                        Integer.valueOf(StringUtils.removeStart(o2, vsName)));
            }
        });
        return tagIds;
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

    @Override
    protected String doFindLatestTagId(String virtualServerName, List<String> tagIds) {
        Integer latestTagIdInt = null;
        for (String tagId : tagIds) {
            Integer tagIdInt = convertFromStrTagId(virtualServerName, tagId);

            if (tagIdInt != null) {
                if (latestTagIdInt == null) {
                    latestTagIdInt = tagIdInt;
                } else if (tagIdInt > latestTagIdInt) {
                    latestTagIdInt = tagIdInt;
                }
            }
        }

        return latestTagIdInt == null ? null : convertToStrTagId(virtualServerName, latestTagIdInt);
    }

    @Override
    protected void doRemoveTag(String virtualServerName, String tagId) throws BizException {
        Integer expectedTagId = convertFromStrTagId(virtualServerName, tagId);

        if (expectedTagId != null) {
            List<File> tagFiles = listAllTagFiles(virtualServerName);
            for (File tagFile : tagFiles) {
                if (expectedTagId == extractTagIdInt(virtualServerName, tagFile.getName())) {
                    FileUtils.deleteQuietly(tagFile);
                    saveToGit(tagFile, FileOP.DEL);
                    return;
                }
            }
        }

        ExceptionUtils.throwBizException(MessageID.TAG_REMOVE_NOT_FOUND, tagId);
    }

}
