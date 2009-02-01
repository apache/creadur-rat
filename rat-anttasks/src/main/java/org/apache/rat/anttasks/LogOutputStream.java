package org.apache.rat.anttasks;

import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.LineOrientedOutputStream;


public class LogOutputStream extends LineOrientedOutputStream {
    private ProjectComponent pc;
    private int level;

    public LogOutputStream(ProjectComponent pPc) {
    	this(pPc, Project.MSG_INFO);
    }

    public LogOutputStream(Task pTask, int pLevel) {
    	this(((ProjectComponent) (pTask)), pLevel);
    }

    public LogOutputStream(ProjectComponent pPc, int pLevel) {
    	pc = pPc;
    	level = pLevel;
    }

    protected void processBuffer() {
    	System.err.println("processBuffer:");
    	try {
    		super.processBuffer();
    	} catch(IOException e) {
    		throw new RuntimeException("Unexpected IOException caught: " + e);
    	}
    }

    protected void processLine(String pLine) {
    	processLine(pLine, level);
    }

    protected void processLine(String pLine, int pLevel) {
    	System.err.println("processLine: " + pLevel + ", " + pLine);
    	pc.log(pLine, pLevel);
    }

    public int getMessageLevel() {
    	return level;
    }
}
