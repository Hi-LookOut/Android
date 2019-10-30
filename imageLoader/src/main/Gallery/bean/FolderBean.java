package com.example.hi.imageloaderchooseimage.bean;

/**
 * Created by hi on 8/22/2017.
 */
public class FolderBean {
    /**
     * 记录文件夹信息
     */
    //记录同一文件夹第一张图片的路径
    //eg./storage/emulated/0/DCIM/Camera/IMG_20170207_121455.jpg
    private String firstImgPath;

    //记录其图片存放父文件夹的绝对路径，因为我们要对不同文件夹图片单独显示
    //eg. /storage/emulated/0/DCIM/Camera
    private String dir;

    //记录其父文件夹名字
    private String name;

    //记录其父文件夹里的图片总数
    private int count;

    public String getDir() {
        return dir;
    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }



    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf=this.dir.lastIndexOf("/")+1;
        this.name=this.dir.substring(lastIndexOf);
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public void setCount(int count) {
        this.count = count;
    }



}
