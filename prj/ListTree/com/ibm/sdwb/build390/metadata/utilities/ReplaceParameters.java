package com.ibm.sdwb.build390.metadata.utilities;

import java.io.Serializable;


public class ReplaceParameters  implements Serializable {

    static final long serialVersionUID = 1111111111111111L;


    private String[] tokens;
    private boolean useRegex = false;
    private boolean replaceWithOldMetadata = false;

    public ReplaceParameters(String[] tokens, boolean useRegex, boolean replaceWithOldMetadata){
        this.tokens = tokens;
        this.useRegex = useRegex;
        this.replaceWithOldMetadata = replaceWithOldMetadata;
    }

    public String[]  getReplaceEntry(){
        return tokens;
    }

    public  boolean useRegularExpression(){
        return useRegex;
    }

    public boolean replaceWithOldMetadata(){
        return replaceWithOldMetadata;
    }
} 
