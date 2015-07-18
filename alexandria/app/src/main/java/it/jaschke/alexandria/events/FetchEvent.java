package it.jaschke.alexandria.events;

import android.support.annotation.Nullable;

/**
 * Created by Jose on 7/4/15.
 */
public class FetchEvent {
    public static final int FETCH_ERROR = -1;
    public static final int FETCH_ALREADY_EXISTS = -2;
    public static final int FETCH_INVALID_ISBN = -3;
    public static final int FETCH_SUCCESS = 1;

    private String title;
    private int resultCode;

    public FetchEvent(int code, @Nullable String title){
        this.resultCode = code;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getResultCode() {
        return resultCode;
    }
}
