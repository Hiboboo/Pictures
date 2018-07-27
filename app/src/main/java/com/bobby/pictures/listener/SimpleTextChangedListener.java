package com.bobby.pictures.listener;

import android.text.Editable;
import android.text.TextWatcher;

/**
 *
 *
 * Created by Bobby on 2018/07/27.
 */
public abstract class SimpleTextChangedListener implements TextWatcher
{
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }

    @Override
    public void afterTextChanged(Editable s)
    {

    }
}
