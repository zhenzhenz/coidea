package sse.tongji.coidea.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import dev.mtage.eyjaot.client.inter.view.ConnConfigurationInput;
import dev.mtage.eyjaot.client.inter.view.IConnConfigureView;

import java.util.Objects;

/**
 * @author mtage
 * @since 2021/3/17 19:48
 */
public class ConnConfigurationDialog implements IConnConfigureView {
    private Project localProject;
    private ConnConfigurationInput connConfigurationInput;

    @Override
    public ConnConfigurationInput readConfigurationInput() {
        if (Objects.isNull(connConfigurationInput)) {
            this.show();
        }
        return this.connConfigurationInput;
    }

    @Override
    public void show() {
        String userName = Messages.showInputDialog(
                localProject,
                "What is your name?",
                "NewCoIdea: Input your name",
                Messages.getQuestionIcon()
        );

        Pair<String, Boolean> pair = Messages.showInputDialogWithCheckBox(
                "What the repo-Id?",
                "NewCoIdea: Input the repo-Id",
                "new repo",
                false,
                true,
                Messages.getQuestionIcon(),
                "",
                null
        );

        this.connConfigurationInput = ConnConfigurationInput.builder()
                .userName(userName)
                .repoId(pair.first)
                .newRepo(pair.second)
                .openDal(false)
                .build();
    }
}
