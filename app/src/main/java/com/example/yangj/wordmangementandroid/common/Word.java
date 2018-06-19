package com.example.yangj.wordmangementandroid.common;

import java.io.Serializable;

/**
 * 单词
 */
public class Word implements Serializable {

    public int id;
    //单词英文拼写
//    @NotEmpty
    private String englishSpell;

    //单词中文拼写
//    @NotEmpty
    private String chineseSpell;

    // 英文发音
//    @URL
    private String englishPronunciation;

    // 中文发音
//    @URL
    private String chinesePronunciation;

    // 图片
//    @URL
    private String image;

//    @URL
    private String rectangleImage;

    //释义
//    @NotEmpty
    private String meaning;

    //释义语音
//    @URL
    private String meaningAudio;

    //例句
//    @NotEmpty
    private String exampleSentence;

//    @URL
    private String exampleSentenceAudio;


    public String getEnglishSpell() {
        return englishSpell;
    }

    public void setEnglishSpell(String englishSpell) {
        this.englishSpell = englishSpell;
    }

    public String getChineseSpell() {
        return chineseSpell;
    }

    public void setChineseSpell(String chineseSpell) {
        this.chineseSpell = chineseSpell;
    }

    public String getEnglishPronunciation() {
        return englishPronunciation;
    }

    public void setEnglishPronunciation(String englishPronunciation) {
        this.englishPronunciation = englishPronunciation;
    }

    public String getChinesePronunciation() {
        return chinesePronunciation;
    }

    public void setChinesePronunciation(String chinesePronunciation) {
        this.chinesePronunciation = chinesePronunciation;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRectangleImage() {
        return rectangleImage;
    }

    public void setRectangleImage(String rectangleImage) {
        this.rectangleImage = rectangleImage;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getMeaningAudio() {
        return meaningAudio;
    }

    public void setMeaningAudio(String meaningAudio) {
        this.meaningAudio = meaningAudio;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public String getExampleSentenceAudio() {
        return exampleSentenceAudio;
    }

    public void setExampleSentenceAudio(String exampleSentenceAudio) {
        this.exampleSentenceAudio = exampleSentenceAudio;
    }

    @Override
    public String toString() {
        return "Word{" +
                "englishSpell='" + englishSpell + '\'' +
                ", chineseSpell='" + chineseSpell + '\'' +
                ", englishPronunciation='" + englishPronunciation + '\'' +
                ", chinesePronunciation='" + chinesePronunciation + '\'' +
                ", image='" + image + '\'' +
                ", rectangleImage='" + rectangleImage + '\'' +
                ", meaning='" + meaning + '\'' +
                ", meaningAudio='" + meaningAudio + '\'' +
                ", exampleSentence='" + exampleSentence + '\'' +
                ", exampleSentenceAudio='" + exampleSentenceAudio + '\'' +
                '}';
    }
}
