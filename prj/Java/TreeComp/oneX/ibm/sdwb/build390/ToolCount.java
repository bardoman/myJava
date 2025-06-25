/* File Tcapi.java created by Jon Tollefson on Tue Aug  5 1997.
   Tool: toolcount
   Classification: IBM Confidential
   (C) Copyright IBM Corp (This work is unpublished). All rights reserved.
*/
package com.ibm.sdwb.build390;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * ToolCount -- API for counting tools.  This is a port of the file tcapi.c and tcproto.h.
 **/
public class ToolCount {
    private final static String COPYRIGHT__ = "Tool:toolcount, IBM Confidential, (C) Copyright IBM Corp (This work is unpublished.) All rights reserved.";
    public static void main(String argv[]) {
	/* At the command prompt: java ToolCount <toolset> <toolname> */
	if (argv.length > 1) {
	    ToolCount.setVerbose(true);
	    ToolCount.toolcount(argv[0], argv[1]);
	}
    }
    //os types
    /**
     * Unknown OS type
     **/
    public final static int TC_OS_UNKNOWN = 0;
    /**
     * AIX OS type
     **/
    public final static int TC_OS_AIX     = 1;
    /**
     * OS/2 OS type
     **/
    public final static int TC_OS_OS2     = 2;
    /**
     * Win95 OS type
     **/
    public final static int TC_OS_WIN95   = 3;
    /**
     * WinNT OS type
     **/
    public final static int TC_OS_WINNT   = 4;
    /**
     * Win OS type
     **/
    public final static int TC_OS_WIN     = 5;
    /**
     * Linux OS type
     **/
    public final static int TC_OS_LINUX   = 6;
    /**
     * OS400 OS type
     **/
    public final static int TC_OS_OS400   = 7;
    /**
     * VM OS type
     **/
    public final static int TC_OS_VM      = 8;
    /**
     * MVS OS type
     **/
    public final static int TC_OS_MVS     = 9;
    /**
     * HP UNIX type
     **/
    public final static int TC_OS_HPUX    = 10;
    /**
     * SOLARIS type
     **/
    public final static int TC_OS_SOLARIS = 11;

    /**
     * Maximum length of a toolset name.
     **/
    public final static int TC_MAX_TOOLSET_LEN = 32;
    /**
     * Maximum length of a tool name.
     **/
    public final static int TC_MAX_TOOL_LEN = 32;
    /**
     * Maximum length of a username
     **/
    public final static int TC_MAX_USER_LEN = 64;


    /**
     * Fallback address if toolcount server cannot be found.
     */
    public final static String FALLBACK_SERVER_LOCATION = "andrew.rchland.ibm.com";


    /**
     * Increments the count for the named tool by one.
     * @param toolset Name of the toolset that the tool belongs to.
     * @param toolname Name of the tool in which to increment the count.
     **/
    public static void toolcount(String toolset, String toolname) {
	toolcount(toolset, toolname, null, null, TC_OS_UNKNOWN, 1);
    }


    /**
     * Allow the user to set a version name for the current toolset.
     * For now this assumes you are using a single toolset.
     * @param ver Version string (keep short < 10 chars).
     */
    public static void setDefaultToolsetVersion(String ver) {
	defaultToolsetVersion = ver;
    }

    /*
     * This function is intended to allow someone to override the toolcount
     * server address externally.  Passing NULL for the location resets the override.
     * @param location Contains a "hostname:port" value.
     */
    public static void setlocation(String location) {
	if(location == null)
	    override_saddr = null;
	else
	    override_saddr = locationAddress(location, null);
    }

    /**
     * Turn verbose mode on (or off).
     * Output will go to System.out.  This is for debugging.
     */
    public static void setVerbose(boolean mode) {
	verbose = mode;
    }

    /**
     * Pack the info as a list of null terminated strings.
     * To locate the toolcount server, lookup the
     * server(tc_toolset) address.  Then send the info via
     * UDP to the toolcount daemon.  Toolcount() is the preferred method
     * since it defaults everything else.
     *
     * NOTE:  We should have a protocol header file.
     * @param toolset Name of the toolset that the tool belongs to.
     * @param toolname Name of the tool in which to increment the count.
     * @param username Name of the user of the toolset.
     * @param userIdentity ID(IBM serial number) of the user of the toolset.
     * @param ostype An integer representing the type of operating system.
     * @param count An integer used to increment the count of the toolset.
     **/
    static void toolcount(String toolset, String toolname, String username, String userIdentity, int ostype, int count) {
	int len;
	int plen = 0;
	byte packetData[] = new byte[256];
	byte tempData[] = new byte[128];
	String hostname = null;
	String domainname = null;

	packetData[0] = 2;	/* Protocol version #2 */
	plen = 1;

	/* Get domain name, and check for xxx.ibm.com */
	try{
	    InetAddress hostAddr=InetAddress.getLocalHost();
	    hostname = hostAddr.getHostName();
	    if (hostname == null) {
		if (verbose)
		    System.out.println("Cannot get my own hostname");
		return;
	    }
	    if(hostname.indexOf(".")  == -1) {
		if (verbose)
		    System.out.println("My hostname " + hostname + " is not qualified.  Look up via DNS");
		try {
		    InetAddress hAddr=InetAddress.getByName(hostAddr.getHostAddress());
		    hostname = hAddr.getHostName();
		    if (hostname == null) {
			if (verbose)
			    System.out.println("DNS failed to locate my hostname");
			return;
		    }
		    if (hostname.indexOf(".") == -1) {
			String myaddr = hAddr.getHostAddress();
			if (verbose)
			    System.out.println("DNS returned a short name.  Try reverse DNS on my addr " + myaddr);
			hAddr = InetAddress.getByName(myaddr);
			hostname = hAddr.getHostName();
			if (hostname == null) {
			    if (verbose)
				System.out.println("Reverse DNS failed to locate my hostname");
			    return;
			}
			if (hostname.indexOf(".") == -1) {
			    if (verbose)
				System.out.println("Reverse DNS found " + hostname + ".  Try resolv.conf");
			    hostname = getdom();
			    if (hostname == null) {
				if (verbose)
				    System.out.println("Cannot read resolv.conf either...punt");
				return;
			    } else {
				if (verbose)
				    System.out.println("Got " + hostname + " from resolv.conf");
				// Simulate a hostname.  We are only interested in the domain.
				hostname = "x." + hostname;
			    }
			}
		    }
		} catch (UnknownHostException e) {
		    if (verbose)
			System.out.println("DNS and/or reverse DNS failed");
		    return;
		}
	    }
	    if (hostname != null) {
		domainname = hostname.substring((hostname.indexOf(".")+1));
		if((!domainname.endsWith(".ibm.com")) && (!domainname.endsWith(".IBM.COM"))) {
		    if (verbose)
			System.out.println("Host is not IBM");
		    return;
		}
	    }
	} catch(UnknownHostException e) {
	    domainname = null;
	}
	/* Add toolset name */
	if(toolset == null)
	    return;		/* too bad */
	copyBytes(toolset, packetData, plen);
	plen += toolset.length();	// ToDo: should test length.
	packetData[plen++] = (byte) '\0';

	/* Add tool name */
	if(toolname == null)
	    return;		/* too bad */
	copyBytes(toolname, packetData, plen);
	plen += toolname.length();	// ToDo: should test length.
	if (defaultToolsetVersion != null) {
	    // Add the toolset version string to the tool name.
	    packetData[plen++] = (byte) '/';
	    copyBytes(defaultToolsetVersion, packetData, plen);
	    plen += defaultToolsetVersion.length();
	}
	packetData[plen++] = (byte) '\0';

	/* Add user name */
	if(username == null)
	    username = System.getProperty("user.name");
	if(domainname != null) {
	    username += "@" + domainname;
	}
	userIdentity = System.getProperty("USERIDENTITY");
	if(userIdentity != null)
	    username += "/" + userIdentity;
	copyBytes(username, packetData, plen);
	plen += username.length();	// ToDo: should test length.
	packetData[plen++] = (byte) '\0';

	/* Add OS type as an ASCII string. */
	if(ostype == TC_OS_UNKNOWN) {
	    String osname = System.getProperty("os.name");
	    copyBytes(osname, packetData, plen);
	    plen += osname.length();
	} else {
	    /* Code specified.  We assume it is one digit for now. */
	    packetData[plen++] = (byte)(ostype + '0');
	}
	packetData[plen++] = (byte) '\0';

	/* Add count as an ASCII string. */
	if(count == 0)
	    count = 1;
	String countS = new Integer(count).toString();
	copyBytes(countS, packetData, plen);
	plen += countS.length();	// ToDo: should test length.
	packetData[plen++] = (byte) '\0';

	// Dump the packet.
	// for (int i = 0; i < plen; i++) System.out.println("[" + i + "] (" + packetData[i] + ") " + (char)packetData[i]);

	/* Transmit the packet. */
	DatagramPacket packet;
	if(override_saddr!=null)
	    packet = override_saddr;
	else if((packet = getTCaddr(toolset)) == null)
	    return;
	packet.setData(packetData);
	packet.setLength(plen);

	try {
	    DatagramSocket socket = new DatagramSocket();
	    socket.send(packet);
	    socket.close();
	} catch(SocketException e) {
	    System.out.println("SocketException: "+e);
	}
	catch(IOException ie) {
	    System.out.println("IOException: "+ie);
	}
    }


    //private
    /**
     * Override toolcount server address.  Used if non-null
     **/
    private static DatagramPacket override_saddr;

    private static DatagramPacket saddr_ptr;
    private static String saddr_toolset = "";
    private static boolean inited = false;
    private static String defaultToolsetVersion = null;

    /**
     * Return a pointer to a static socket address
     * of the toolcount server.
     * @param toolset Name of the toolset that the tool belongs to.
     * @return Return the socket address of the toolcount server.
     **/
    private static DatagramPacket getTCaddr(String toolset) {
	/* Lookup the address of the toolcount server for this toolset.
	 * We save the address from call to call, but still need to verify
	 * the saved address is for the correct toolset.
	 */
	//ensure toolset name doesn't have more then max chars
	String ts = toolset.substring(0, toolset.length() < TC_MAX_TOOLSET_LEN ? toolset.length() : TC_MAX_TOOLSET_LEN);

	if(!inited || !ts.equals(saddr_toolset)) {
	    inited = true;

	    /* Look for a servers.loc entry of tc_<toolset> */
	    String serviceName = "tc_" + toolset;
	    String addrstring = System.getProperty(serviceName.toUpperCase());
	    if (addrstring != null)
		saddr_ptr = locationAddress(addrstring, toolset);
	    else {
		/* Try default of tc-toolset.rchland.ibm.com */
		addrstring = "tc-" + toolset + ".rchland.ibm.com";
		saddr_ptr = locationAddress(addrstring, toolset);
		if (saddr_ptr == null) {
		    /* Ok, try fallback default. */
		    saddr_ptr = locationAddress(FALLBACK_SERVER_LOCATION, toolset);
		}
	    }
	    saddr_toolset = ts;
	}
	return saddr_ptr;
    }

    /**
     * Given a location string (e.g. hostname:port), create a
     * DatagramPacket containing the address and port of the named location.
     * @param location A string containing the hostname and port of the location.
     * @return Return a DatagramPacket containing the address and port of the location.
     **/
    private static DatagramPacket locationAddress(String location, String toolset) {
	DatagramSocket saddr;
	short port;
	String hostName, portName;

	StringTokenizer st = new StringTokenizer(location, ":", false);
	if (!st.hasMoreTokens())
	    return null;
	hostName = st.nextToken();
	if (st.hasMoreTokens()) {
	    portName = st.nextToken();
	    if (portName == null)
		return null;
	    else
		port = new Short(portName).shortValue();
	} else if (toolset != null) {
	    // compute port
	    port = (short)(16534 + (byte)toolset.toUpperCase().charAt(0));
	} else
	    return null;
	InetAddress hp = null;
	if(hostName!=null) {
	    try {
		hp = InetAddress.getByName(hostName);
	    } catch(UnknownHostException e) {
		return null;
	    }
	}
	return (new DatagramPacket(new byte[1], 0, hp, port));
    }

    /** Copy a string to a byte array.
     * @param str String to copy.
     * @param dst Byte array destination.
     * @param startpos Index of start position in destination (typically 0).
     */
    private static void copyBytes(String str, byte dst[], int startpos) {
	int strlength = str.length();
	for ( int i=0; i<strlength; i++) {
		dst[startpos+i] = (byte)str.charAt(i);
	}
    }

    /**
     * Try to get my domain name from /etc/resolv.conf.
     * This will fail on many platforms.  It is a last chance effort.
     */
    private static String getdom() {
	try {
	    java.io.InputStream f = new java.io.FileInputStream("/etc/resolv.conf");
	    byte buf[] = new byte[500];
	    if (f.read(buf) > 0) {
		int i = 0;
		while (i < buf.length) {
		    // Look for a line starting with 'domain'
		    if (buf[i] == 'd' && i+7 < buf.length && buf[i+1] == 'o') {
			// assume it is 'domain'
			i += 7;
			while (i < buf.length && (buf[i] == ' ' || buf[i] == '\t'))
			    i++;
			// ok, i indexes the start of the domain.  Find the end.
			int j = i;
			while (j < buf.length && buf[j] != '\n')
			    j++;
			return new String(buf, i, j-i);
		    } else {
			// skip this line.
			while (i < buf.length && buf[i] != '\n')
			    i++;
			i++;
			continue;
		    }
		}
	    }
	    f.close();
	} catch (java.io.FileNotFoundException e) {
	    if (verbose)
		System.out.println("Cannot open resolv.conf");
	} catch (java.io.IOException e) {
	    if (verbose)
		System.out.println("Error reading resolv.conf");
	}
	return null;
    }

    /**
     * TRUE if we are debugging (main sets this).
     */
    private static boolean verbose = false;

}
