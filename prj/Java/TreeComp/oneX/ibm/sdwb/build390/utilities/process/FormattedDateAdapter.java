package com.ibm.sdwb.build390.utilities.process;

import java.util.*;
import java.text.*;

public class FormattedDateAdapter implements Comparable {
    private Date date;
    private DateFormat dateFormatter;
    public FormattedDateAdapter(Date date) {
        this.date = date;
    }

    public void setDateFormatter(DateFormat dateFormat) {
        this.dateFormatter = dateFormat;
    }

    public Date getDate() {
        return date;
    }

    public int compareTo(Object o) {
        return date.compareTo(((FormattedDateAdapter)o).getDate());
    }
    protected Object clone() throws CloneNotSupportedException {
        return date.clone();
    }

    public boolean equals(Object o) {
        return date.equals(o);
    }
    public String toString() {
        return dateFormatter.format(date);
    }

}

