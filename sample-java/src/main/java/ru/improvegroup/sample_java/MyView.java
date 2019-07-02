package ru.improvegroup.sample_java;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface MyView extends MvpView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void doC();
}
