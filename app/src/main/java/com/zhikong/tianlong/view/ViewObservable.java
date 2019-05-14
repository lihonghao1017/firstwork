package com.zhikong.tianlong.view;

/**
 * Created by lihh on 2019/5/7.
 */

public interface ViewObservable {
    public void addObserver(ViewObserver observer);

    public void deleteObserver(ViewObserver observer);

    public void notifyObservers(ViewObserver status, Object obj);
}
