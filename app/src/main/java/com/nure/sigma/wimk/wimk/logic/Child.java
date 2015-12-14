package com.nure.sigma.wimk.wimk.logic;

import java.util.ArrayList;
import java.util.List;

public class Child {

    public static final String ARGUMENTS_SPLITTER = ";";
    public static final String AUTHORISED_CHILD_FALSE = "0";

    private String name;
    private int sendingFrequency;
    private int authorised;

    public String getName() {
        return name;
    }

    public int getSendingFrequency() {
        return sendingFrequency;
    }

    public int getAuthorised() {
        return authorised;
    }

    public void setAuthorised(int authorised) {
        this.authorised = authorised;
    }

    public Child(String name, int sendingFrequency, int authorised) {
        this.name = name;
        this.sendingFrequency = sendingFrequency;
        this.authorised = authorised;
    }

    public Child(String childString) {
        String[] arguments = childString.split(ARGUMENTS_SPLITTER);
        if (arguments.length == 3) {
            name = arguments[0];
            try {
                sendingFrequency = Integer.parseInt(arguments[1]);
            } catch (NumberFormatException e) {
                sendingFrequency = -1;
                return;
            }
            try {
                authorised = Integer.parseInt(arguments[2]);
            }
            catch (NumberFormatException e){
                authorised = 0;
            }
        } else {
            sendingFrequency = -1;
        }
    }

    public static List<Child> parseChildrenList(String childrenString) {
        ArrayList<Child> result = new ArrayList<>();
        String childStrings[] = childrenString.split("\n");
        Child child;

        for (String childString : childStrings) {
            child = new Child(childString);
            if (child.getSendingFrequency() != -1) {
                result.add(child);
            }
        }
        return result;
    }
}
