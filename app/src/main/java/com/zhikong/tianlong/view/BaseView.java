package com.zhikong.tianlong.view;

import android.content.Context;
import android.util.AttributeSet;

import com.mapbar.scale.ScaleLinearLayout;

/**
 * Created by lihh on 2019/5/7.
 */

public abstract class BaseView extends ScaleLinearLayout implements ViewObserver{
    private BaseView parentView;

    public BaseView(Context context) {
        super(context);
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public abstract void initView();

    public BaseView getParentView() {
        return parentView;
    }

    public void setParentView(BaseView view) {
        this.parentView = view;
    }


}
