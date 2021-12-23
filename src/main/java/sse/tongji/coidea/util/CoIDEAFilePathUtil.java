package sse.tongji.coidea.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dev.mtage.error.CommonSysException;
import dev.mtage.eyjaot.client.inter.util.FilePathUtil;
import dev.mtage.eyjaot.core.util.EyjaOtCommonErrorCode;

import java.util.Objects;

/**
 * @author mtage
 * @since 2021/3/20 14:48
 */
public class CoIDEAFilePathUtil extends FilePathUtil {
    public static String getProjectRelativePath(String absolutePath, Project project) {
        if (Objects.isNull(project.getBasePath())) {
            throw new CommonSysException(EyjaOtCommonErrorCode.INVALID_PARAM.getCode(), "empty project base path");
        }
        return getProjectRelativePath(absolutePath, project.getBasePath());
    }

    public static String getProjectRelativePath(VirtualFile file, Project project) {
        if (Objects.isNull(project.getBasePath())) {
            throw new CommonSysException(EyjaOtCommonErrorCode.INVALID_PARAM.getCode(), "empty project base path");
        }
        return getProjectRelativePath(file.getPath(), project.getBasePath());
    }
}
