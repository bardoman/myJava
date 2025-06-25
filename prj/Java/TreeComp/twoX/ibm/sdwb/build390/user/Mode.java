package com.ibm.sdwb.build390.user;

public abstract class  Mode {

    public static int NO_LIB = 0;
    public static int CONFIG = NO_LIB+1;
    public static int DEVELOPMENT = CONFIG +1; //default
    private static boolean isActive = false;

    public  int getId() {
        return DEVELOPMENT;
    }

    public void activate() {
        isActive =true;

    }

    public  boolean isActive() {
        return isActive;
    }

    public  boolean isFakeLibrary() {
        return(getId() == Mode.NO_LIB);
    }


    public String getCategory(){
        if(!isFakeLibrary()){
            return "Library ";
        }
        return "NOLIB ";
    }
}
