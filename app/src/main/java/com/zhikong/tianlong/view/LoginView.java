package com.zhikong.tianlong.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.zhikong.tianlong.R;

/**
 * Created by lihh on 2019/5/7.
 */

public class LoginView extends BaseView {

    public LoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void updata(EventType type, Object obj) {

    }

    @Override
    public void initView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.login_view, null);
        this.addView(v);

    }
}
