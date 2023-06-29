package io.github.JumperOnJava.jjdynmap.dynmap.dynmapscreen;

import java.util.function.Function;

public interface TwoSideOperation<T> {
    Function<T,T> getForwardFunction();
    Function<T,T> getBackwardFunction();
}
