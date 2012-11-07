package com.rapidftr.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import com.rapidftr.activity.LoginActivity;
import lombok.Cleanup;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DatabaseSessionIntegrationTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    public Solo solo;
    public DatabaseHelper helper;
    public DatabaseSession session;

    public DatabaseSessionIntegrationTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        helper = new SQLCipherHelper("test_db", "test_key", getActivity());
        session = helper.getSession();

        session.execSQL("DELETE FROM children");
    }

    @Override
    public void tearDown() throws Exception {
        session.close();
        helper.close();
        solo.finishOpenedActivities();
    }

    @Test
    public void shouldGetCountOFAllChildren() {
        @Cleanup Cursor cursor = session.rawQuery("select count(1) from children", new String[]{});
        cursor.moveToNext();
        assertEquals(cursor.getInt(0), 0);
    }

    @Test
    public void ShouldBeAbleToDeleteAChildRecord() {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.DB_CHILD_ID, "id1");
        values.put(DatabaseHelper.DB_CHILD_OWNER, "owner1");
        values.put(DatabaseHelper.DB_CHILD_CONTENT, "content1");
        values.put(DatabaseHelper.DB_CHILD_SYNCED, "false");

        long id = session.insert("children", null, values);
        assertThat(id, is(notNullValue()));

        int deleted = session.delete("children", "id = ?", new String[] { "id1" });
        assertThat(deleted, is(1));
    }

    @Test(expected = Exception.class)
    public void shouldNotBeAbleToAccessDatabaseWithIncorrectDecrypitionKey() {
        helper = new SQLCipherHelper("test_db", "wrong_password", getActivity());
    }

}