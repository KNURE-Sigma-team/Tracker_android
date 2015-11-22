package com.nure.sigma.wimk.wimk.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andstepko on 22.11.15.
 */
public class Child {

    public static final String ARGUMENTS_SPLITTER = ";";

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Child(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Child(String childString){
        String[] arguments = childString.split(ARGUMENTS_SPLITTER);
        if(arguments.length == 2){
            id = Integer.parseInt(arguments[0]);
            name = arguments[1];
        }
        else {
            id = -1;
        }
    }

    public static List<Child> parseChildrenList(String childrenString){
        ArrayList<Child> result = new ArrayList<>();
        String childStrings[] = childrenString.split("\n");
        Child child;

        for(String childString : childStrings){
            child = new Child(childString);
            if(child.getId() != -1){
                result.add(child);
            }
        }
        return result;
    }
}
