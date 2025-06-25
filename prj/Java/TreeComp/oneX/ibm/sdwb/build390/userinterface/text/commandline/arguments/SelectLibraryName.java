package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class SelectLibraryName extends CommandLineArgument {

    private static final String keyword = "SELECTLIBRARYNAME";
    private static final String explaination ="set the active library server from the stored setup using shortname." + 
                                              "\nThe shortname format is as follows.\n"+ 
                                              "<user>@<libraryname>@<first portion of the library server name upto the .(dot)>";

    public SelectLibraryName() {
        super(keyword,explaination);
    }
}
