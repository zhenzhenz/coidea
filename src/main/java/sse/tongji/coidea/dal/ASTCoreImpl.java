package sse.tongji.coidea.dal;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import sse.tongji.coidea.util.CoIDEAFilePathUtil;
import sse.tongji.dal.astoperation.ASTCore;
import sse.tongji.dal.editingoperation.EditingOperation;
import sse.tongji.dal.locksystem.BasicRegion;
import sse.tongji.dal.locksystem.CodeType;
import sse.tongji.dal.locksystem.RegionType;
import sse.tongji.dal.userinfo.DalUser;
import sse.tongji.dal.userinfo.DalUserGroup;

import java.util.*;

public class ASTCoreImpl implements ASTCore {
    private Project project;
    private DalUser dalUser;
    private EditingOperation editingOperation;
    private VirtualFile virtualFile;
    private PsiFile psiFile;
    private PsiElement psiElement;
    private PsiMethod originMethod;

    public ASTCoreImpl(Project project) {
        this.project = project;
    }

    @Override
    public BasicRegion detectTargetedRegion(EditingOperation editingOperation) {
        //准备工作
        originMethod = null;
        int offset = editingOperation.getEditingPosition();
        this.editingOperation = editingOperation;
        this.virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(CoIDEAFilePathUtil.getStandardAbsolutePath(editingOperation.getEditingFileName(), project.getBasePath()));
        Document document = FileDocumentManager.getInstance().getCachedDocument(virtualFile);
        this.psiFile = PsiFileFactory.getInstance(project).createFileFromText(Language.findLanguageByID("JAVA"), document.getText());
        //找到offset的元素
        this.psiElement = psiFile.findElementAt(offset);
        //根据元素找到对应区域
        return findBasicRegion(psiElement);
    }

    @Override
    public List<BasicRegion> deriveDependedRegionSet() {
//        System.out.println(editingOperation.getSiteName());
//        System.out.println(DalUserGroup.getDalUserGroup());
        this.dalUser = DalUserGroup.getDalUserBySiteName(editingOperation.getSiteName());
        List<BasicRegion> result = new ArrayList<>();
        if (originMethod != null) {
            result = findDependedRegionByDepth(originMethod, dalUser.getFieldDepth(), dalUser.getMethodDepth());
            System.out.println("--------" + result.size());
        }
        return result;
    }

    @Override
    public List<BasicRegion> deriveAwarenessRegionSet() {
        List<BasicRegion> result = new ArrayList<>();
        if (originMethod != null) {
            result = findDependedRegionByDepth(originMethod, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        return result;
    }

    public List<BasicRegion> findDependedRegionByDepth(PsiMethod rootMethod, int fieldDepth, int methodDepth) {
        List<BasicRegion> result = new ArrayList<>();
        List<BasicRegion> methodResult = new ArrayList<>();
        List<BasicRegion> fieldResult = new ArrayList<>();
        Queue<PsiMethod> q = new LinkedList<>();
        q.add(rootMethod);
        while (!q.isEmpty()) {
            PsiMethod nowMethod = q.poll();
            if (nowMethod == null) {
                break;
            }
            List<PsiField> pfields = new ArrayList<>();
            if (fieldDepth > 0) {
                pfields = findFieldInMethod(nowMethod);
                fieldDepth--;
            }
            for (PsiField field : pfields) {
                if (field != null) {
                    BasicRegion fieldRegion = getFieldBasicRegion(field);
                    if (!fieldResult.contains(fieldRegion)) {
                        fieldResult.add(fieldRegion);
                    }
                }
            }
            List<PsiMethod> pmethods = new ArrayList<>();
            if (methodDepth > 0) {
                pmethods = findMethodsInMethod(nowMethod);
                methodDepth--;
            }
            for (PsiMethod method : pmethods) {
                if (method != null) {
                    BasicRegion methodRegion = getMethodBasicRegion(method);
                    if (!methodResult.contains(methodRegion)) {
                        methodResult.add(methodRegion);
                        q.add(method);
                    }
                }
            }
        }
        result.addAll(fieldResult);
        result.addAll(methodResult);
        return result;
    }

    private List<PsiMethod> findMethodsInMethod(PsiMethod psiMethod) {
        List<PsiMethod> result = new ArrayList<>();
        Collection<PsiMethodCallExpression> callMethods = PsiTreeUtil.findChildrenOfType(psiMethod, PsiMethodCallExpression.class);
        for (PsiMethodCallExpression callMethod : callMethods) {
            PsiMethod findedMethod = (PsiMethod) callMethod.getMethodExpression().resolve();
            result.add(findedMethod);
        }
        return result;
    }

    private List<PsiField> findFieldInMethod(PsiMethod psiMethod) {
        List<PsiField> result = new ArrayList<>();
        //PsiReferenceExpression
        Collection<PsiReferenceExpression> childOfRefer = PsiTreeUtil.findChildrenOfType(psiMethod, PsiReferenceExpression.class);
        for (PsiReferenceExpression refer : childOfRefer) {
            //因为找不到直接找引用全局字段的方法
            //所以方案是，先将所以的字段列出来，找到方法内的所有引用，如果引用了字段，就加入结果
            //TODO
//            if (psiFields.contains(refer.resolve())) {
            //TODO
            if (PsiTreeUtil.instanceOf(refer.resolve(), PsiField.class)) {
                result.add((PsiField) refer.resolve());
            }
//            }
//            System.out.println("Refer.resolve() : " + refer.resolve().getText() + "Containing File :" + refer.resolve().getContainingFile().getVirtualFile());
        }
        return result;
    }

    public BasicRegion findBasicRegion(PsiElement psiElement) {
        PsiField containingField = PsiTreeUtil.getParentOfType(psiElement, PsiField.class);
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
        //如果是Field
        if (containingField != null) {
            return getFieldBasicRegion(containingField);
        }
        //如果是method
        if (containingMethod != null) {
            originMethod = containingMethod;
            return getMethodBasicRegion(containingMethod);
        }
        //如果是OpenArea
        return getOpenAreaBasicRegion(psiElement);
    }

    private BasicRegion getFieldBasicRegion(PsiField psiField) {
        String fieldName = psiField.getNameIdentifier().getText();
        BasicRegion result = new BasicRegion(fieldName, RegionType.BASICAREA);
        //field=1
        result.setCodeType(CodeType.FIELD);
        result.setRegionFileName(getPath(psiField.getContainingFile().getVirtualFile().getPath()));
        result.setStartOffset(psiField.getTextRange().getStartOffset());
        result.setEndOffset(psiField.getTextRange().getEndOffset());
//        System.out.println(fieldName);
        return result;
    }

    private BasicRegion getMethodBasicRegion(PsiMethod psiMethod) {
        String methodName = psiMethod.getNameIdentifier().getText();
//        System.out.println(methodName);
        PsiParameterList methodParameterList = psiMethod.getParameterList();
        //将方法名和方法参数拼接成regionid
        String methodfinalName = buildMethodName(methodName, methodParameterList);

        BasicRegion result = new BasicRegion(methodfinalName, RegionType.BASICAREA);
        result.setCodeType(CodeType.METHOD);
        result.setRegionFileName(getPath(psiMethod.getContainingFile().getVirtualFile().getPath()));
        result.setStartOffset(psiMethod.getTextRange().getStartOffset());
        result.setEndOffset(psiMethod.getTextRange().getEndOffset());
        return result;
    }

    private BasicRegion getOpenAreaBasicRegion(PsiElement psiElement) {
        BasicRegion openArea = new BasicRegion("OpenArea", RegionType.OPENAREA);
        openArea.setRegionFileName(getPath(psiElement.getContainingFile().getVirtualFile().getPath()));
        return openArea;
    }

    private String buildMethodName(String methodName, PsiParameterList psiParameterList) {
        //如果参数不为空 拼接
        if (!psiParameterList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(methodName);
            PsiParameter[] paraArray = psiParameterList.getParameters();
            for (PsiParameter psiParameter : paraArray) {
                sb.append("/");
                sb.append(psiParameter.getTypeElement().getText());
            }
            return sb.toString();
        }
        //参数空 直接返回name
        return methodName;
    }
//
    private String getPath(String path) {
        if (path.equals("/foo.bar")) {
            return editingOperation.getEditingFileName();
        } else {
            return CoIDEAFilePathUtil.getProjectRelativePath(path, project);
        }
    }
}
