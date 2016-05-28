package dev.chdu.picker.bean;

/**
 * Created on 5/28/2016.
 */
public class FolderBean {

    private String dir;
    private String firstImagePath;
    private String name;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = dir.lastIndexOf("/");
        name = dir.substring(lastIndexOf + 1);
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public String getName() {
        return name;
    }
}
