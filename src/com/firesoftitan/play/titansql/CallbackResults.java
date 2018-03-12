package com.firesoftitan.play.titansql;

import java.util.HashMap;
import java.util.List;

public abstract class CallbackResults {
    public CallbackResults()
    {

    }
    public void onResult(List<HashMap<String, ResultData>> results) {}
}
