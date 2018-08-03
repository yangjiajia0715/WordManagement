package com.yangjiajia.test;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.activitiy.FileSelectorActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.startsWith;

/**
 * @author yangjiajia
 * @date 2018/7/30
 */
@RunWith(AndroidJUnit4.class)
public class FileSelectorActivityTest {
    private static final String TAG = "FileSelectorActivityTes";

    @Rule
    public ActivityTestRule mActivityTestRule = new ActivityTestRule<>(FileSelectorActivity.class);
    private MyIdlingResource mMyIdlingResource;

    @Test
    public void test() {
        FileSelectorActivity fileSelectorActivity = (FileSelectorActivity) mActivityTestRule.getActivity();

//        Espresso.onData(hasToString(startsWith("Item Text")))
//                .inAdapterView(withId(R.id.list_view)).atPosition(0)
//                .perform(click());

//        Espresso.onData(withId(R.id.list_view))
//                .atPosition(0)
//                .perform(ViewActions.click());

        Espresso.onData(hasToString(startsWith("单词去")))
                .inAdapterView(withId(R.id.list_view))
                .atPosition(0)
                .perform(ViewActions.longClick());

//        fileSelectorActivity.onBackPressed();

//        Espresso.pressBack();

//        RecyclerViewActions.actionOnItemAtPosition(27, click());

    }

//    @Before
//    public void registerIntentServiceIdlingResource() {
//        Log.d(TAG, "registerIntentServiceIdlingResource: ");
//
//        Activity activity = mActivityTestRule.getActivity();
//        mMyIdlingResource = new MyIdlingResource((FileSelectorActivity) activity);
////        Espresso.registerIdlingResources(mMyIdlingResource);
//        IdlingRegistry.getInstance().register(mMyIdlingResource);
//    }

    @After
    public void unregisterIntentServiceIdlingResource() {
        Log.d(TAG, "unregisterIntentServiceIdlingResource: ");

        IdlingRegistry.getInstance().register(mMyIdlingResource);
    }

    private static class MyIdlingResource implements IdlingResource {
        private ResourceCallback mCallback = null;
        private FileSelectorActivity mFileSelectorActivity;

        MyIdlingResource(FileSelectorActivity activity) {
            mFileSelectorActivity = activity;
        }

        @Override
        public String getName() {
            return "MyIdlingResource";
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            mCallback = callback;
        }

        @Override
        public boolean isIdleNow() {
            Log.d(TAG, "isIdleNow: ");
//            boolean isIdle = mFileSelectorActivity != null;
            boolean isIdle = mFileSelectorActivity != null && mFileSelectorActivity.isReady();
            if (isIdle && mCallback != null) {
                mCallback.onTransitionToIdle();
            }
            return isIdle;
        }
    }

    public static class Item {
        private final int value;

        public Item(int value) {
            this.value = value;
        }

        public String toString() {
            return String.valueOf(value);
        }
    }

    public static Matcher<Object> withValue(final int value) {
        return new BoundedMatcher<Object, Item>(Item.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("has value " + value);
            }

            @Override
            public boolean matchesSafely(Item item) {
                return item.toString().equals(String.valueOf(value));
            }
        };
    }

}
