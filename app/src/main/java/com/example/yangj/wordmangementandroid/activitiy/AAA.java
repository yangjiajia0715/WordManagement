package com.example.yangj.wordmangementandroid.activitiy;

import java.util.List;
/**
 * {
 * id: 67,
 * type: "SAY_WORD_BY_READ_IMAGE",
 * wordId: 7,
 * title: {
 *    title: "Goodbye.",
 *    audio: "",
 *    image: null,
 *    video: null
 * },
 * options: [ ],
 * answersIndex: [ ],
 * uid: "SAY_WORD_BY_READ_IMAGE:67"
 * }
 */

/**
 * Created by yangjiajia on 2018/6/21.
 */
public class AAA {

    /**
     * id : 9
     * type : CHOOSE_WORD_BY_READ_IMAGE
     * wordId : 1
     * title : null
     * options : ["Halo","hello"]
     * answersIndex : [1]
     * uid : CHOOSE_WORD_BY_READ_IMAGE:9
     */

    private int id;
    private String type;
    private int wordId;
    private Object title;
    private String uid;
    private List<String> options;
    private List<Integer> answersIndex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public Object getTitle() {
        return title;
    }

    public void setTitle(Object title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<Integer> getAnswersIndex() {
        return answersIndex;
    }

    public void setAnswersIndex(List<Integer> answersIndex) {
        this.answersIndex = answersIndex;
    }
}
