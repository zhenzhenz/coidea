package util;

import org.apache.commons.io.FilenameUtils;

public class FilePathUtil {
    /**
     * /project/file => project/file
     * @param projectAbsolutePath
     * @return
     */
    public static String getProjectRelativePath(String projectAbsolutePath) {
        return FilenameUtils.separatorsToUnix(projectAbsolutePath).substring(1);
    }
}