package com.example.yangj.wordmangementandroid.util;

import com.example.yangj.wordmangementandroid.common.Question;

/**
 * Created by yangjiajia on 2018/6/21.
 */
public class QuestionType2Chinese {
    public static String getChinese(Question.Type type) {
        switch (type) {
            case MATCH_WORD_IMAGE:
                return "词图关联";
            case MATCH_WORD_MEANING:
                return "词意关联";
            case CHOOSE_IMAGE_BY_LISTEN_WORD:
                return "听词选图";
            case CHOOSE_WORD_BY_LISTEN_SENTENCE://核心，创建一个会自动创建其他
                return "听文选词";
//            case SAY_WORD_BY_READ_WORD:
//                return "";
//            case CHOOSE_WORD_BY_READ_SENTENCE:
//                return "";
//            case SAY_WORD_BY_READ_IMAGE:
//                return "看词说词";
            case SPELL_WORD_BY_READ_IMAGE:
                return "看图拼写";
            case SPELL_WORD_BY_LISTEN_WORD:
                return "听词拼写";
            default:
                return "未知类型！";
        }
    }
}
