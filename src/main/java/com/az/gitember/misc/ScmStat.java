package com.az.gitember.misc;

import java.util.Map;

/**
 * Created by igor on 05.03.2019.
 */
public class ScmStat {

    private final Map<String, Integer> total;
    private final Map<String, Integer> logMap;

    public ScmStat(Map<String, Integer> total, Map<String, Integer> logMap) {
        this.total = total;
        this.logMap = logMap;
    }

    public Map<String, Integer> getLogMap() {
        return logMap;
    }

    public Map<String, Integer> getTotal() {
        return total;
    }

}
