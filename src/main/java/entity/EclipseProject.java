package entity;

import entity.inter.ICoProject;

public class EclipseProject implements ICoProject {
    private String basePath;
    private String name;
    public EclipseProject(String basePath, String name) {
        this.basePath = basePath;
        this.name = name;
    }

    public EclipseProject() {
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setName(String name) {
        this.name = name;
    }
}
