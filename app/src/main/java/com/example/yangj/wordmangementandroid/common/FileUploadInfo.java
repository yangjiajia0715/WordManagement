package com.example.yangj.wordmangementandroid.common;

/**
 * FileUploadInfo
 * Created by yangjiajia on 2017/4/28 0028.
 */

public class FileUploadInfo implements BaseInfo {
    /**
     * id : 6156
     * createTime : 1493365965975
     * updateTime : 1493365965975
     * fileName : IMG_20161025_155722.jpg
     * fileType :
     * fileSuffix : jpg
     * fileSize : 1265089
     */

    private int id;
    private long createTime;
    private long updateTime;
    private String fileName;
    private String fileType;
    private String fileSuffix;
    private int fileSize;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "FileUploadInfo{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSuffix='" + fileSuffix + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
