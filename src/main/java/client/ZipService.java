package client;

import com.intellij.openapi.project.Project;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import dev.mtage.util.AssertUtil;
import config.FileConfig;

import entity.inter.ICoProject;
import util.MyLogger;

public class ZipService {
    public  Project project = null;
    public List<String> zipIgnore  = new ArrayList<>();
    private final String ZIPFILE_NAME = ".eyja.zip";
    private final Integer BUFFER_SIZE = 8192;

    private MyLogger log = MyLogger.getLogger(ZipService.class);

    public ZipService(Project project) {
        zipIgnore.add(".idea");
        zipIgnore.add("build");
        zipIgnore.add(".gradle");
        this.project = project;
    }

    public File zipAll() {
        if (Objects.isNull(project)) {
            return null;
        }
        File sourceDirFile = new File(project.getBasePath());
        log.info("当前压缩基本路径 {0}", project.getBasePath());
        File zippedFile = new File(sourceDirFile.getParent() + "/" + ZIPFILE_NAME);
        log.info("压缩结果文件路径 {0}", zippedFile);
        try {
            FileOutputStream fos = new FileOutputStream(zippedFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fos);
            File[] files = sourceDirFile.listFiles();
            if (Objects.nonNull(files)) {
                // 使用相对路径压缩并加上项目名
                Arrays.stream(files).forEach(f -> compress(f, zipOutputStream, project.getName() + "/"));
            }
            log.info("压缩全部项目文件结束");
            // 注意必须先关闭zip stream
            zipOutputStream.close();
            fos.close();
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException {0}", sourceDirFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("IOException {0}", e);
        }
        return zippedFile;
    }

    public void unzip(byte[] data) {
        if (Objects.isNull(project)) {
            return;
        }
        File destDirFile = new File(project.getBasePath()).getParentFile();
        // 先删除原有全部文件
        File[] originFiles = destDirFile.listFiles();
        if (Objects.nonNull(originFiles)) {
            Arrays.stream(originFiles).forEach(f -> {
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    deleteFile(f);
                }
            });
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(data);
        ZipInputStream zipInputStream = new ZipInputStream(byteInputStream);
        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (Objects.nonNull(zipEntry)) {
                if (zipEntry.isDirectory()) {
//                    log.info("zipEntry name: {0}", zipEntry.getName());
                    File dirFile = new File(destDirFile + zipEntry.getName());
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    zipEntry = zipInputStream.getNextEntry();
                    continue;
                }

                File newFile = deCompress(destDirFile, zipEntry);
                File parentFile = newFile.getParentFile();
                if (Objects.nonNull(parentFile) && !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                int length = zipInputStream.read(buffer);
                do {
                    if (length > BUFFER_SIZE) {
                        log.error("文件过大，缓冲区不足 length: {1}", length);
                    }
                    fileOutputStream.write(buffer, 0, length);
                    length = zipInputStream.read(buffer);
                } while (length > 0);
                fileOutputStream.close();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (IOException e) {
            log.error("IOException {0}", e);
        }

    }

    /**
     * 压缩单个文件 由于压缩时应使用相对路径，因此需要有baseDir
     * @param file
     * @param zipOutputStream
     * @param baseDir
     */
    private void zipFile(File file, ZipOutputStream zipOutputStream, String baseDir) {
        AssertUtil.verify(file.isFile(), "zipFile cannot process directory");
        if (!file.exists() || FileConfig.ignoreList.contains(file.getName())) {
            return;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            zipOutputStream.putNextEntry(new ZipEntry(baseDir + file.getName()));
            log.info("压缩文件 {0} ", file.getAbsolutePath());
            int length = 0;
            do {
                length = fileInputStream.read(buffer);
                if (length > BUFFER_SIZE) {
                    log.error("文件 {0} 过大，缓冲区不足 length: {1}", file.getName(), length);
                }
                zipOutputStream.write(buffer, 0, length);
                length = fileInputStream.read(buffer);
            } while (length > 0);
        } catch (IOException e) {
            log.error("文件压缩异常 {0} {1} ", e.getMessage(), e.getCause().getMessage());
        }
    }

    private void zipDirectory(File dirFile, ZipOutputStream zipOutputStream, String baseDir) {
        AssertUtil.verify(dirFile.isDirectory(), "zipDirectory cannot process file");
        if (!dirFile.exists() || FileConfig.ignoreList.contains(dirFile.getName())) {
            return;
        }
        File[] files = dirFile.listFiles();
        if (Objects.isNull(files)) {
            return;
        }
        Arrays.stream(files).forEach(f -> compress(f, zipOutputStream, baseDir + dirFile.getName() + "/"));
    }

    private void compress(File file, ZipOutputStream zipOutputStream, String baseDir) {
        if (file.isDirectory()) {
            zipDirectory(file, zipOutputStream, baseDir);
        } else {
            zipFile(file,zipOutputStream, baseDir);
        }
    }

    private File deCompress(File destDir, ZipEntry zipEntry) {
//        log.info("deCompress zipEntry name {0}", zipEntry.getName());
        File destFile = new File(destDir, zipEntry.getName());
        try {
            String destDirPath = destDir.getCanonicalPath();
            String destFilePath = destFile.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new IOException("Entry is outside of the target dir:" + zipEntry.getName());
            }
        } catch (IOException e) {
            log.error("文件解压缩异常 {0} {1}", e.getMessage(), e.getCause().getMessage());
        }
        return destFile;
    }

    private void deleteFile(File file) {
        AssertUtil.verify(file.isFile(), "deleteFile cannot process directory");
        if (FileConfig.ignoreList.contains(file.getName())) {
            return;
        }
        file.delete();
    }

    private void deleteDir(File dirFile) {
        AssertUtil.verify(dirFile.isDirectory(), "deleteDir cannot process file");
        File[] files = dirFile.listFiles();
        if (Objects.isNull(files) || FileConfig.ignoreList.contains(dirFile.getName())) {
            return;
        }
        Arrays.stream(files).forEach(f -> {
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                deleteFile(f);
            }
        });
    }

}
