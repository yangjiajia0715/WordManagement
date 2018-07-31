package com.yangjiajia.test;

import android.app.Activity;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.activitiy.HomeActiivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * @author yangjiajia
 * @date 2018/7/30
 */
@RunWith(AndroidJUnit4.class)
//@LargeTest
public class HomeActivityInstrumentationTest {
    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            HomeActiivity.class);

    @Test
    public void sayHello(){
        Activity activity = mActivityRule.getActivity();
//        mActivityRule.
//        mActivityRule.launchActivity();
        //通过id找到edittext，在里面输入2并关闭输入法

        Espresso.onView(withId(R.id.btn_word_district))
                .perform(click());
//                .check(ViewAssertions.matches(ViewMatchers.withText("哈哈哈")));

//        Espresso.onData(withId(R.id.btn_begin_check));

//        isDisplayed();

//        RecyclerViewActions.scrollToPosition(1);

//                .perform(typeText("2"), closeSoftKeyboard());
//        //通过id找到edittext，在里面输入5并关闭输入法
//        Espresso.onView(withId(R.id.editText2)).perform(typeText("5"), closeSoftKeyboard());
//        //通过id找到button，执行点击事件
//        Espresso.onView(withId(R.id.button)).perform(click());
//        //通过id找到textview，并判断是否与文本匹配
//        Espresso.onView(withId(R.id.textView)).check(matches(withText("计算结果：6")));
//        Matcher<View> viewMatcher = ViewMatchers.withText(R.string.app_name);
//        Espresso.onView(withId(R.id.btn_word_district)).check(matches(withText("计算结果：7")));
//        onView(withText("Say hello!"))
//                .perform(click());


    }
}
