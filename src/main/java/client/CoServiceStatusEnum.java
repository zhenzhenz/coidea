package client;

public enum CoServiceStatusEnum {

    /**
     * 未创建
     */
    UNCREATED,

    /**
     * 已创建实例 未初始化
     */
    CREATED,
    /**
     * 项目初始化已完成
     */
    INITED,
    /**
     * 已连接至服务器开始协作
     */
    CONNECTED,

}
