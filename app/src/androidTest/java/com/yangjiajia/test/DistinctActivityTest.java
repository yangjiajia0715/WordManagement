package com.yangjiajia.test;

import android.app.Activity;
import android.support.test.espresso.IdlingResource;
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
    private IdingResourceA mIdingResourceA;

    @Test
    public void test() {
        Activity activity = mActivityTestRule.getActivity();

//        Espresso.onView(ViewMatchers.withText("知道了"))
//                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

//        ViewMatchers.isNotChecked();

    }

//    @Before
//    public void regist(){
//        mIdingResourceA = new IdingResourceA();
//        IdlingRegistry.getInstance().register(mIdingResourceA);
//    }
//
//    @After
//    public void unregist(){
//        IdlingRegistry.getInstance().unregister(mIdingResourceA);
//    }

    public  class IdingResourceA implements IdlingResource{
        ResourceCallback mResourceCallback;
        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean isIdleNow() {

            if (mResourceCallback != null) {
                mResourceCallback.onTransitionToIdle();

            }
            return false;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            mResourceCallback = callback;
        }

    }

}
