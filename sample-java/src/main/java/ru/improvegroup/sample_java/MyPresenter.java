package ru.improvegroup.sample_java;

import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class MyPresenter extends MvpPresenter<MyView> {
    private int e;
}
