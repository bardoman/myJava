package com.mydomain;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


public class MyAntTask extends Task {
    private String msg;

    // The method executing the task
    public void execute() throws BuildException {
        System.out.println(msg);
    }

    // The setter for the "message" attribute
    public void setMessage(String msg) {
        this.msg = msg;
    }

    public void doSquare(int n)
    {
        this.msg =Integer.toString(n *n);
    }
}
