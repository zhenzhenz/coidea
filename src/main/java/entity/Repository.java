package entity;

import config.ApiConfig;
import dev.mtage.util.AssertUtil;
import dal.DALPolicySettingData;

public class Repository {
    private String userId;
    private String repositoryId;
    private Boolean initNew;
    private String serverAddr = ApiConfig.DEFAULT_SERVER_ADDR;
    private DALPolicySettingData dalPolicySettingData;
    private boolean isDal;

    public Repository(String userId, String repositoryId, Boolean initNew, boolean isDal, DALPolicySettingData dalPolicySettingData) {
        AssertUtil.checkNotBlank(userId, "userId is blank");
        AssertUtil.checkNotBlank(repositoryId, "portalId is blank");
        AssertUtil.checkNotNull(dalPolicySettingData, "dalPolicySettingData is null");
        this.isDal = isDal;
        this.dalPolicySettingData = dalPolicySettingData;
        this.initNew = initNew;
        this.userId = userId;
        this.repositoryId = repositoryId;
    }

    public Repository(String userId, String repositoryId, Boolean initNew, String serverAddr, boolean isDal, DALPolicySettingData dalPolicySettingData) {
//        AssertUtil.checkNotBlank(userId, "userId is blank");
//        AssertUtil.checkNotBlank(repositoryId, "repositoryId is blank");
//        AssertUtil.checkNotNull(dalPolicySettingData, "dalPolicySettingData is null");

        this.isDal = isDal;
        this.dalPolicySettingData = dalPolicySettingData;
        this.userId = userId;
        this.repositoryId = repositoryId;
        this.initNew = initNew;
        this.serverAddr = serverAddr;
    }

    public String getUserId() {
        return userId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public Boolean isInitNew() {
        return initNew;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setDalPolicySettingData(DALPolicySettingData dalPolicySettingData) {
        this.dalPolicySettingData = dalPolicySettingData;
    }

    public void setDal(boolean dal) {
        isDal = dal;
    }

    public DALPolicySettingData getDalPolicySettingData() {
        return dalPolicySettingData;
    }

    public boolean isDal() {
        return isDal;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "userId='" + userId + '\'' +
                ", repository='" + repositoryId + '\'' +
                ", initNew=" + initNew +
                ", serverAddr='" + serverAddr + '\'' +
                '}';
    }
}
