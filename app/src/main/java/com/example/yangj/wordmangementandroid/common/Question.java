package com.example.yangj.wordmangementandroid.common;

import java.io.Serializable;
import java.util.List;

/**
 * 题目
 */
public class Question implements Serializable {
    public enum Type {
        MATCH_WORD_IMAGE,
        MATCH_WORD_MEANING,
        CHOOSE_IMAGE_BY_LISTEN_WORD,
        CHOOSE_WORD_BY_LISTEN_SENTENCE,
        CHOOSE_WORD_BY_READ_IMAGE,
        CHOOSE_WORD_BY_READ_SENTENCE,
        SAY_WORD_BY_READ_WORD,
        SAY_WORD_BY_READ_IMAGE,
        SPELL_WORD_BY_READ_IMAGE,
        SPELL_WORD_BY_LISTEN_WORD
    }

    private int id;
    // 题目类型
//    @QuestionType
    private Type type;

//    @NotNull
    private int wordId;

    // 题干
    private QuestionTitle title;

    // 选项
    private List<String> options;

    // 答案
    private List<Integer> answersIndex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public QuestionTitle getTitle() {
        return title;
    }

    public void setTitle(QuestionTitle title) {
        this.title = title;
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

    public String getUid() {
        return type.name() + ":" + getId();
    }

    @Override
    public String toString() {
        return
                "id=" + id +
                "\ntype=" + type +
                "\nwordId=" + wordId +
                "\ntitle=" + title +
                "\noptions=" + options +
                "\nanswersIndex=" + answersIndex;
    }
}
