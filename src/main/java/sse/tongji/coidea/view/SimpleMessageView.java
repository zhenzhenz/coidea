package sse.tongji.coidea.view;

import com.intellij.openapi.ui.Messages;
import sse.tongji.coidea.config.CoIDEAUIString;

/**
 * 阻塞式对话框提示，不需要Project
 * @author mtage
 * @since 2021/3/18 18:12
 */
public class SimpleMessageView implements IMessageView {
    @Override
    public void messageWithDefaultTitle(String msg) {
        Messages.showInfoMessage(msg, CoIDEAUIString.GENERAL_TITLE);
    }
}
