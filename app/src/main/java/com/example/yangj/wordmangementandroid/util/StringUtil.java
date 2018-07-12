package com.example.yangj.wordmangementandroid.util;

/**
 * Created by yangjiajia on 2018/7/10.
 */
public class StringUtil {
    public static String replaceAll(String sentence){
        sentence = sentence.replaceAll("’s ", "'s ");
        sentence = sentence.replaceAll("’re ", "'re ");
        sentence = sentence.replaceAll("’m ", "'m ");
        sentence = sentence.replaceAll("’t ", "'t ");
        return sentence.trim();
    }
}
