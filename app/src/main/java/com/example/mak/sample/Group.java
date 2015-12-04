package com.example.mak.sample;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mak on 12/3/15.
 */
public class Group {

    public String string;
    public final List<String> children = new ArrayList<String>();

    public Group(String string) {
        this.string = string;
    }

}