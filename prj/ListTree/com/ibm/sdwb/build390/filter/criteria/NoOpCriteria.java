package com.ibm.sdwb.build390.filter.criteria;

class NoOpCriteria implements FilterCriteria {

    private boolean fakeReturnType = true;

    NoOpCriteria(boolean fakeReturnType){
       this.fakeReturnType = fakeReturnType;
    }

    boolean getFakeReturnType() {
        return fakeReturnType;
    }



    public boolean passes(Object obj){
        return fakeReturnType;
    }



}












