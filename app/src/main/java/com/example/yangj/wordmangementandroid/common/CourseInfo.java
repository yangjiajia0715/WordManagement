package com.example.yangj.wordmangementandroid.common;

import java.io.Serializable;
import java.util.List;

/**
 * 课程信息
 * Created by yangjiajia on 2018/6/27.
 */
public class CourseInfo implements Serializable {

    /**
     * id : 4
     * name : 一年级下
     * grade : ONE
     * semesterType : SPRING_SEMESTER
     * introduction : English
     * cover : http://wordstorage.cookids.com.cn/courseware/cover/1-2.jpg
     * detail : null
     * createdDateTime : null
     * publishedDateTime : null
     * unpublishedDateTime : null
     * payType : CHARGE
     * enable : true
     * purchasable : true
     * features : null
     * editionId : 1
     * plans : [{"words":[94,95,96]},{"words":[94,95,97]},{"words":[96,97,98]},{"words":[98,94,96]},{"words":[95,97,98]},{"words":[99,100,104]},{"words":[101,103,104]},{"words":[118,119,113]},{"words":[100,102,41]},{"words":[41,105,103]},{"words":[107,108,111]},{"words":[107,111,112]},{"words":[108,112,109]},{"words":[109,105,103]},{"words":[108,107,102]},{"words":[113,114,115]},{"words":[116,117,114]},{"words":[116,117,114]},{"words":[120,121,122]},{"words":[123,110,122]}]
     */

    private int id;
    private String name;
    private String grade;
    private String semesterType;
    private String introduction;
    private String cover;
    private Object detail;
    private Object createdDateTime;
    private Object publishedDateTime;
    private Object unpublishedDateTime;
    private String payType;
    private boolean enable;
    private boolean purchasable;
    private Object features;
    private int editionId;
    private List<PlansBean> plans;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSemesterType() {
        return semesterType;
    }

    public void setSemesterType(String semesterType) {
        this.semesterType = semesterType;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    public Object getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Object createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Object getPublishedDateTime() {
        return publishedDateTime;
    }

    public void setPublishedDateTime(Object publishedDateTime) {
        this.publishedDateTime = publishedDateTime;
    }

    public Object getUnpublishedDateTime() {
        return unpublishedDateTime;
    }

    public void setUnpublishedDateTime(Object unpublishedDateTime) {
        this.unpublishedDateTime = unpublishedDateTime;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isPurchasable() {
        return purchasable;
    }

    public void setPurchasable(boolean purchasable) {
        this.purchasable = purchasable;
    }

    public Object getFeatures() {
        return features;
    }

    public void setFeatures(Object features) {
        this.features = features;
    }

    public int getEditionId() {
        return editionId;
    }

    public void setEditionId(int editionId) {
        this.editionId = editionId;
    }

    public List<PlansBean> getPlans() {
        return plans;
    }

    public void setPlans(List<PlansBean> plans) {
        this.plans = plans;
    }

    public static class PlansBean implements Serializable{
        private List<Integer> words;

        public List<Integer> getWords() {
            return words;
        }

        public void setWords(List<Integer> words) {
            this.words = words;
        }
    }
}
