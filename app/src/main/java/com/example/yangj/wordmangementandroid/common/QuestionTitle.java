package com.example.yangj.wordmangementandroid.common;

public class QuestionTitle {
    // 文字
//    @NotEmpty
    private String title;

    // 音频
    private String audio;

    // 图片
    private String image;

    // 视频
    private String video;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    @Override
    public String toString() {
        return "QuestionTitle{" +
                "title='" + title + '\'' +
                ", audio='" + audio + '\'' +
                ", image='" + image + '\'' +
                ", video='" + video + '\'' +
                '}';
    }
}
