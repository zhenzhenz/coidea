package sse.tongji.coidea.dal;


import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import lombok.Getter;
import lombok.Setter;
import sse.tongji.dal.locksystem.CFDOperation;
import sse.tongji.dal.locksystem.CheckPermission;
import sse.tongji.dal.userinfo.DalUser;
import sse.tongji.dal.userinfo.DalUserGroup;
import sse.tongji.dal.userinfo.OperationType;

import java.time.LocalDateTime;

public class DalCore {
    @Setter
    @Getter
    private static String localPath;

    public synchronized static void doCFDbyUserOperation(String siteName, String editingFileName, OperationType operationType, int editingStartOffset, int editingLength, DalPolicySettings setting) {
        //如果项目初始化，鼠标没有点在过任何页面上，localPath == null
        if (localPath == null) {
            return;
        }

        //如果dal打开，并且编辑位置和本地编辑位置一致，更新dal锁
        if (setting.isDalOpen() && localPath.equals(editingFileName)) {
            DalUser userOperation = buildUser(siteName, editingFileName,operationType,editingStartOffset,editingLength,setting);
            DalUserGroup.updateUser(userOperation);
            DalUserGroup.removeTimeOutUser();
            CFDOperation.cfdMethod(userOperation);
        }
    }

    public static boolean doDalPermissionCheck(String siteName, String editingFileName, OperationType operationType, int editingStartOffset, int editingLength, DalPolicySettings setting) {
        DalUser userOperation = buildUser(siteName, editingFileName,operationType,editingStartOffset,editingLength,setting);
        return CheckPermission.doCheckPermission(userOperation);
    }

    private static DalUser buildUser(String siteName, String editingFileName, OperationType operationType, int editingStartOffset, int editingLength, DalPolicySettings setting) {
        DalUser userOperation = new DalUser();
        userOperation.siteName = siteName;
        userOperation.editingFileName = editingFileName;
        userOperation.operationType = operationType;
        userOperation.editingPosition = editingStartOffset;
        userOperation.editingStartOffset = editingStartOffset;
        userOperation.editingLength = editingLength;
        //用户没有打开深度锁定，即深度无限
        if (!setting.isDepthOpen()) {
            userOperation.fieldDepth = Integer.MAX_VALUE;
            userOperation.methodDepth = Integer.MAX_VALUE;
        } else {
            userOperation.fieldDepth = setting.getFieldDepth();
            userOperation.methodDepth = setting.getMethodDepth();
        }
        userOperation.timeoutSecond = setting.getTimeoutSecond();
        userOperation.timestamp = LocalDateTime.now();
        return userOperation;
    }
}
