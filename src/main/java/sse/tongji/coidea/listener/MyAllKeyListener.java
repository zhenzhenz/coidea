package sse.tongji.coidea.listener;

import com.google.common.collect.Lists;
import com.intellij.ide.IdeEventQueue;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalFilePresenter;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.awt.event.KeyEvent.*;


/**
 * 键盘按下历经KEY_PRESSED KEY_TYPED KEY_RELEASED 三个阶段
 * 在KEY_TYPED后，KEY_RELEASED前，文档内容变化开始，beforeDocumentChange会被调用
 * @author mtage
 * @since 2021/3/23 13:49
 */
public class MyAllKeyListener implements IdeEventQueue.EventDispatcher {
    private final MyLogger log = MyLogger.getLogger(MyAllKeyListener.class);
    private final LocalFilePresenter localFilePresenter;

    private final List<Integer> ignoredKeys = Lists.newArrayList(VK_LEFT, VK_RIGHT, VK_UP, VK_DOWN, VK_ESCAPE,
                                                        VK_CONTROL, VK_SHIFT, VK_CAPS_LOCK, VK_META, VK_HELP, VK_ALT, VK_UNDEFINED);
    private final List<Integer> keyMapKeys = List.of(VK_S, VK_A, VK_C);
    private final List<Integer> ignoredKeyMapModifiers = List.of(VK_CONTROL);

    public MyAllKeyListener(LocalFilePresenter localFilePresenter) {
        this.localFilePresenter = localFilePresenter;
        ignoredKeys.addAll(IntStream.range(VK_F1, VK_F24 + 1).boxed().collect(Collectors.toList()));
    }

    @Override
    public boolean dispatch(@NotNull AWTEvent e) {
        if (e instanceof KeyEvent) {
            KeyEvent keyEvent = (KeyEvent) e;
            if (keyEvent.getID() != KEY_PRESSED) {
                return false;
            }
            log.info("key code={0} char={1} id={2} event={3}",
                    keyEvent.getKeyCode(), keyEvent.getKeyChar(),
                    keyEvent.getID(), keyEvent);
            if (ignoredKeys.contains(keyEvent.getKeyCode())) {
                return false;
            }
            if (keyMapKeys.contains(keyEvent.getKeyCode()) && ignoredKeyMapModifiers.contains(keyEvent.getModifiersEx())) {
                return false;
            }
            try {
                boolean lockResult = localFilePresenter.tryAcquireSemaphore(300, TimeUnit.MILLISECONDS);
                if (!lockResult) {
                    log.error("本地获取锁失败...");
                    localFilePresenter.releaseSemaphore();
                    return true;
                }
            } catch (InterruptedException interruptedException) {
                log.error("InterruptedException ", interruptedException);
            }
        }
        return false;
    }
}
