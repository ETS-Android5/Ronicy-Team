package com.adeasy.advertise;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.adeasy.advertise.ui.getintouch.AboutUsActivity;
import com.adeasy.advertise.ui.getintouch.BugsActionsActivity;
import com.adeasy.advertise.ui.getintouch.GetInTouchActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class GetInTouchIntentTesting2 {

    @Rule
    public ActivityScenarioRule<GetInTouchActivity> activityRule = new ActivityScenarioRule<>(GetInTouchActivity.class);

    @Before
    public void setup()
    {
        Intents.init();
    }

    @Test
    public void verifyIntent()
    {
        onView(withId(R.id.bugReportBtn)).perform(click());
        intended(hasComponent(BugsActionsActivity.class.getName()));
    }

    @After
    public void tearDown()
    {
        Intents.release();
    }
}
