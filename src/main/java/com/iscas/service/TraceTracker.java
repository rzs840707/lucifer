package com.iscas.service;

import com.iscas.bean.Span;

import java.util.List;

public interface TraceTracker {
    public List<Span> sample(int num, int lookback);

    public List<Span> sampleErr(int num, int lookback);

}
