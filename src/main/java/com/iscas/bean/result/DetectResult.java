package com.iscas.bean.result;

import com.iscas.bean.assertion.Assertion;

import java.util.List;

public class DetectResult {
    private int id;
    private int injectionTime;
    private String[] IPS;
    private List<Assertion> failed;
    private String timestamp;
}
