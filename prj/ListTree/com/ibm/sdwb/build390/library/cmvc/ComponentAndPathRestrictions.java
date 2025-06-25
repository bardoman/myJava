package com.ibm.sdwb.build390.library.cmvc;

import java.util.*;
import java.io.*;

public class ComponentAndPathRestrictions implements Serializable {
    static final long serialVersionUID = 1111111111111111L;
    private boolean includeListedComponents = false;
    private List componentList = null;
    private String compPath = null;//TST3456B    

    private boolean includeListedPaths = false;
    private List pathList = null;
    private String dirPath = null;//TST3456B

    public void setIncludeComponents(boolean shouldInclude) {
        includeListedComponents = shouldInclude;
    }

    public void setIncludePaths(boolean shouldInclude) {
        includeListedPaths = shouldInclude;
    }

    public boolean isComponentsIncluded () {
    	
    	return includeListedComponents;
        
    }

    public boolean isPathsIncluded () {
    	return includeListedPaths;
    }

    public void setComponentList(List tempComponents) {
    	componentList = tempComponents;           
    }  
    
    public void setPathList(List tempPaths) {
        pathList = tempPaths;
    }

    //TST3456B <Begin>    
    public void setComponentsPath(String tempCompPath){
    	compPath=tempCompPath;    	
    }
    
    public void setDirectoryPath(String tempDirPath){
    	dirPath = tempDirPath;
    }
    //TST3456B <End>
    
    public List getComponentList() {
        return componentList;
    }

    public List getPathList() {
        return pathList;
    }
    
    //TST3456B <Begin>
    public String getCompPath(){
    	return compPath;
    }
    
    public String getDirectoryPath(){
    	return dirPath;
    }
    //TST3456B <End>
}
