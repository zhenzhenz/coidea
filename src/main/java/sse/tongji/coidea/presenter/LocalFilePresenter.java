package sse.tongji.coidea.presenter;

import dev.mtage.eyjaot.client.OtClient;
import dev.mtage.eyjaot.client.inter.EditOperationSourceEnum;
import dev.mtage.eyjaot.client.inter.presenter.GeneralLocalFilePresenter;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.operation.SimpleDeleteTextOperation;
import dev.mtage.eyjaot.core.operation.SimpleInsertTextOperation;

/**
 * @author mtage
 * @since 2021/3/17 19:14
 */
public class LocalFilePresenter extends GeneralLocalFilePresenter {
    @Override
    public void close() {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void onInsert(SimpleInsertTextOperation operation, EditOperationSourceEnum source, CoUser coUser) {

    }

    @Override
    public void onDelete(SimpleDeleteTextOperation operation, EditOperationSourceEnum source, CoUser coUser) {

    }

    @Override
    public void onRewrite(String content) {

    }
}
