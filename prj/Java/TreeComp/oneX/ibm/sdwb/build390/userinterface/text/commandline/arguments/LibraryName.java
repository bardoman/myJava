package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryName extends CommandLineArgument {

    private static final String keyword = "LIBRARYNAME";
    private static final String explaination ="The name of the library to use.";

    public LibraryName() {
        super(keyword,explaination);
    }
}
