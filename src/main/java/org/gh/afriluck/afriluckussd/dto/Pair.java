package org.gh.afriluck.afriluckussd.dto;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Pair<F, S> {
    private final F key;
    private final S value;

    public Pair(F key, S value) {
        this.key = key;
        this.value = value;
    }

    public F getKey() {
        return key;
    }

    public S getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}