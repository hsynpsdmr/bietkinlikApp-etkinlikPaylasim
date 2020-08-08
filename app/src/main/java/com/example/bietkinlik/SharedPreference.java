package com.example.bietkinlik;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    static final String PREF_NAME="Dosya";
    static final String PREF_KEY="Key";

    public void save(Context context,String text){
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= settings.edit();
        editor.putString(PREF_KEY,text);
        editor.commit();
    }

    public String getValue(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        String text = settings.getString(PREF_KEY,null);
        return text;
    }
}
