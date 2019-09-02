package com.bobby.pictures.actions.abs;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * <HR>
 * 创建者 Bobby
 * <p>
 * 时间 2017/8/15 19:15
 * <p>
 */
public abstract class BaseFragment extends Fragment
{
    protected Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        if (getLayoutResId() != 0)
            return inflater.inflate(getLayoutResId(), container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected abstract int getLayoutResId();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        this.setupViews(view);
    }

    protected abstract void setupViews(View contentView);
}
