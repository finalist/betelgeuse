package com.gamaray.arex;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.gamaray.arex.ARExplorerTest \
 * com.gamaray.arex.tests/android.test.InstrumentationTestRunner
 */
public class ARExplorerTest extends ActivityInstrumentationTestCase<ARExplorer> {

    public ARExplorerTest() {
        super("com.gamaray.arex", ARExplorer.class);
    }

}
