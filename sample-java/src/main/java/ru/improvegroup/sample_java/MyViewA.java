package ru.improvegroup.sample_java;

import moxy.presenter.InjectPresenter;

public class MyViewA implements MyView {

    @InjectPresenter
    MyPresenter myPresenterA;

    @Override
    public void doC() {

    }
}
