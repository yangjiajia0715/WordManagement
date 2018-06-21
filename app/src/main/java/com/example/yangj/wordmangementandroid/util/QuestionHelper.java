package com.example.yangj.wordmangementandroid.util;

import java.util.Random;

/**
 * 错题选项生成逻辑
 * Created by yangjiajia on 2018/6/21.
 */
public class QuestionHelper {
    private static Random sRandom = new Random();

//    public static List<String> createOptions(WordLoad wordLoad, List<Integer> answers, boolean firstSpell) {
//        int grade = 2;//1- 9年级，年级越高顺序越乱
//        List<String> options = new ArrayList<>();
//        sRandom.nextInt();
//        List<String> rightOptions = wordLoad.rightOptions;
//        List<String> wrongOption1 = wordLoad.wrongOption1;
//        if (rightOptions == null || rightOptions.size() != 2) {
//            break;
//        }
//        if (wrongOption1 == null || wrongOption1.size() != 2) {
//            break;
//        }
//        List<String> wrongOption2 = wordLoad.wrongOption2;
//        if (wrongOption2 == null || wrongOption2.size() != 2) {
//            break;
//        }
//
//        List<Integer> answerIndex1 = new ArrayList<>();
//        List<String> options1 = new ArrayList<>();
//        switch (i % 5) {//前五种都是顺序：1 2，1 3，1 4，2 3，2 4
//            case 0://1 2，1 3，1 4，2 3，2 4
//                answerIndex1.add(0);
//                answerIndex1.add(1);
//                options1.add(rightOptions.get(0));
//                options1.add(rightOptions.get(1));
//                options1.add(wrongOption1.get(0));
//                options1.add(wrongOption1.get(1));
//                break;
//            case 1://1 2，1 3，1 4，2 3，2 4
//                answerIndex1.add(0);
//                answerIndex1.add(2);
//                options1.add(rightOptions.get(0));
//                options1.add(wrongOption1.get(0));
//                options1.add(rightOptions.get(1));
//                options1.add(wrongOption1.get(1));
//                break;
//            case 2://1 2，1 3，1 4，2 3，2 4
//                answerIndex1.add(0);
//                answerIndex1.add(3);
//                options1.add(rightOptions.get(0));
//                options1.add(wrongOption1.get(0));
//                options1.add(wrongOption1.get(1));
//                options1.add(rightOptions.get(1));
//                break;
//            case 3://1 2，1 3，1 4，2 3，2 4
//                answerIndex1.add(1);
//                answerIndex1.add(2);
//                options1.add(wrongOption1.get(0));
//                options1.add(rightOptions.get(0));
//                options1.add(rightOptions.get(1));
//                options1.add(wrongOption1.get(1));
//                break;
//            case 4://1 2，1 3，1 4，2 3，2 4
//                answerIndex1.add(1);
//                answerIndex1.add(3);
//                options1.add(wrongOption1.get(0));
//                options1.add(rightOptions.get(0));
//                options1.add(wrongOption1.get(1));
//                options1.add(rightOptions.get(1));
//                break;
//            case 5://倒序：
//                answerIndex1.add(1);
//                answerIndex1.add(3);
//                options1.add(wrongOption1.get(0));
//                options1.add(rightOptions.get(0));
//                options1.add(wrongOption1.get(1));
//                options1.add(rightOptions.get(1));
//                break;
//            case 6://倒序：
//                answerIndex1.add(1);
//                answerIndex1.add(3);
//                options1.add(wrongOption1.get(0));
//                options1.add(rightOptions.get(0));
//                options1.add(wrongOption1.get(1));
//                options1.add(rightOptions.get(1));
//                break;
//            case 7://倒序：
//                answerIndex1.add(1);
//                answerIndex1.add(3);
//                options1.add(wrongOption1.get(0));
//                options1.add(rightOptions.get(0));
//                options1.add(wrongOption1.get(1));
//                options1.add(rightOptions.get(1));
//                break;
//            default:
//                break;
//        }
//
//        return options;
//    }
}
