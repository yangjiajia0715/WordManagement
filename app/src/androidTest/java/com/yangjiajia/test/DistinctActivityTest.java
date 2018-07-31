package com.yangjiajia.test;

import android.app.Activity;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.yangj.wordmangementandroid.activitiy.DistinctActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author yangjiajia
 * @date 2018/7/31
 */
@RunWith(AndroidJUnit4.class)
public class DistinctActivityTest {

    @Rule
    public ActivityTestRule mActivityTestRule = new ActivityTestRule<>(DistinctActivity.class);

    @Test
    public void test() {
        Activity activity = mActivityTestRule.getActivity();

        Espresso.onView(ViewMatchers.withText("知道了"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

}
