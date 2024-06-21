package org.gh.afriluck.afriluckussd.dto;

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