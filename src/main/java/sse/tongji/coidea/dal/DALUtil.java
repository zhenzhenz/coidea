package sse.tongji.coidea.dal;

import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import sse.tongji.dal.editingoperation.DALSettingMessage;
import sse.tongji.dal.editingoperation.EditingOperation;
import sse.tongji.dal.editingoperation.OperationType;

import java.time.LocalDateTime;

/**
 * @className: DALUtil
 * @description: TODO 类描述
 * @author: Wenhua Xu
 * @date: 2022/1/2
 **/
public class DALUtil {

    public static EditingOperation buildDalEditingOperation(String siteName, OperationType operationType, int editingPosition, int editingLength, String path) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String editingFileName = path;
        EditingOperation editingOperation = new EditingOperation(siteName, operationType, localDateTime, editingFileName, editingPosition, editingLength);
        return editingOperation;
    }

    public static DALSettingMessage buildDalSettingMessage(String siteName, DalPolicySettings dalPolicySettings) {
        DALSettingMessage dalSettingMessage = new DALSettingMessage(siteName,
                dalPolicySettings.getTimeoutSecond(),
                dalPolicySettings.isDalOpen(),
                dalPolicySettings.isDepthOpen(),
                dalPolicySettings.getFieldDepth(),
                dalPolicySettings.getMethodDepth(),
                2);
        return dalSettingMessage;
    }
}
