package io.github.javajump3r.jjdynmap.dynmap.dynmapscreen;

import java.util.function.Function;

public interface TwoSideOperation<T> {
    Function<T,T> getForwardFunction();
    Function<T,T> getBackwardFunction();
}
