package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IndexedArgument  extends CommandLineArgument {

    private int startingIndex               = -1;   //a configurable option to change the startingIndex. If this changes the subsequent  properties change as well.
    private boolean allowIndexOnly          = true; //allows only entries like A1,A2,A3. A isn't valid 
    private boolean stopOnBrokenIndex       = true; //When the input is A1,A2,A3,A5, verifies upto A1,A2,A3
    private boolean allowOrderedIndexEntry  = true; //Allows ordered index entry. eg: A1,A2,A3 is valid. A1,A2,A3,A6 is invalid or  A3,A4 is invalid

    private Map<String,String> numericKeyValuesPairsMap = new HashMap<String,String>();
    private Set<String>        processedNumbers           = new HashSet<String>();
    private Set<String>        notSatisfiedNumbers        = new HashSet<String>();
    private Set<String>        processedOutOfOrderNumbers = new HashSet<String>();
    private boolean isBroken = false;
    private boolean isOrdered = false;
    private int maxIndex = 0;
    private int minIndex = 0;
    private int firstBrokenIndex = -1;
    private CommandLineArgument anArgument =null;


    public IndexedArgument(String tempCommandLineName, String tempExplaination) {
        this(1,tempCommandLineName,tempExplaination);
    }

    public IndexedArgument(int tempStartingIndex,String tempCommandLineName, String tempExplaination) {
        super(tempCommandLineName,tempExplaination);
        this.startingIndex = tempStartingIndex;
    }

    protected void setArgument(CommandLineArgument tempArgument) {
        this.anArgument  = tempArgument;
    }

    protected CommandLineArgument getArgument() {
        return anArgument;
    }

    protected Set<String> getProcessedOutOfOrderNumbersSet() {
        return processedOutOfOrderNumbers;
    }

    //darn... hmm.  i don't like this approach.
    protected CommandLineArgument createArgument() {
        return new TemporaryArgument(getCommandLineName(),getDescriptionOfBoolean());
    }

    public  boolean isSatisfied() {
        boolean satisfied = numericKeyValuesPairsMap.isEmpty();

        if (anArgument!=null) {
            satisfied = satisfied & anArgument.isSatisfied();
        }

        if (isAllowOrderedIndexEntry()) {
            satisfied = satisfied & !isOrdered;
        }
        return satisfied;
    }

    public String getReasonNotSatisfied() {
        StringBuilder reason = new StringBuilder();
        Formatter formatter = new Formatter(reason);
        for (String numberString:notSatisfiedNumbers) {
            formatter.format("%-20s%-22s",getCommandLineName() +numberString, "was   not  specified.");
        }
        if (anArgument!=null) {
            formatter.format("%-42s%n", anArgument.getReasonNotSatisfied());
        }
        return reason.toString();
    }

    protected int getMaxIndex() {
        return maxIndex;
    }

    protected int getMinIndex() {
        return minIndex;
    }


    public boolean inputAvailable() {
        return !numericKeyValuesPairsMap.isEmpty();
    }

    public boolean inputAvailableAt(int index) {
        boolean valueExists = numericKeyValuesPairsMap.containsKey(String.valueOf(index));
        return valueExists;
    }


    protected  void setAllowIndexOnly(boolean tempAllowIndexOnly) {
        this.allowIndexOnly = tempAllowIndexOnly;
    }

    protected  void setStopOnBrokenIndex(boolean tempStopOnBrokenIndex) {
        this.stopOnBrokenIndex = tempStopOnBrokenIndex;
    }

    protected  void setAllowOrderedIndexEntry(boolean tempAllowOrderedIndexEntry) {
        this.allowOrderedIndexEntry = tempAllowOrderedIndexEntry;
    }

    protected boolean isAllowOrderedIndexEntry() {
        return allowOrderedIndexEntry;
    }


    protected boolean isStopOnBrokenIndex() {
        return stopOnBrokenIndex;
    }

    protected boolean isAllowIndexOnly() {
        return allowIndexOnly;
    }

    public int getStartingIndex() {
        return startingIndex;
    }

    public Map<String,String> getIndexToValuesMap() {
        return numericKeyValuesPairsMap;
    }

    public void setValues(Map fullArgumentMap) {
        SortedMap<String,String> sortedMap = getCommandSpecificArgumentMap(fullArgumentMap);
        int count = startingIndex;
        if (!sortedMap.isEmpty() && !isAllowOrderedIndexEntry()) {
            count = Integer.parseInt(getNumberString(sortedMap.firstKey()));
        }
        minIndex = count; //the first keyindex
        for (Iterator keyIterator = sortedMap.keySet().iterator(); (keyIterator.hasNext() & !isBroken & !isOrdered);) {
            String testKey   = (String) keyIterator.next();
            String keyIndex = getNumberString(testKey);
            if (keyIndex != null && !processedNumbers.contains(keyIndex)) {
                if (keyIndex.matches("\\d+$")) {
                    int i = Integer.parseInt(keyIndex);
                    if (i > maxIndex) { //swap maxindex
                        maxIndex = i;
                    }
                    if (minIndex > i) { //swap min index
                        minIndex = i;
                    }
                    isBroken  = isStopOnBrokenIndex() & (count !=i);    //figure out if broken.
                    isOrdered = isAllowOrderedIndexEntry() & (count !=i);  //figure out if not ordered

                    if ((firstBrokenIndex < 0) && isBroken) { //record the broken index
                        firstBrokenIndex = i;
                    }

                    String testValue = (String) sortedMap.get(testKey);
                    if (isChangeValueToUpperCase()) {
                        testValue = testValue.toUpperCase();
                    }

                    if (!isBroken & !isOrdered) {
                        processedNumbers.add(String.valueOf(i));
                        numericKeyValuesPairsMap.put(keyIndex, testValue);

                        if (count !=i) {
                            for (int j=count; j<(i-count); count++) {
                                notSatisfiedNumbers.add(String.valueOf(j));
                            }
                        } else {
                            count++;
                        }
                    } else {
                        if (!isStopOnBrokenIndex()) { //only in cases where we allow  broken indices, we'll have to account for "out of order indices"
                            processedOutOfOrderNumbers.add(keyIndex);
                            numericKeyValuesPairsMap.put(keyIndex, testValue);
                        }
                    }
                } else if (!isAllowIndexOnly()) {
                    String testValue = (String) fullArgumentMap.get(testKey);
                    if (isChangeValueToUpperCase()) {
                        testValue = testValue.toUpperCase();
                    }
                    numericKeyValuesPairsMap.put(testKey, testValue);
                    setValue(testValue);
                }
            }
        }

    }

    private SortedMap<String,String> getCommandSpecificArgumentMap(Map fullArgumentMap) {
        SortedMap<String,String> sortedMap = new TreeMap<String,String> (new KeyComparator());
        for (Iterator keyIterator = fullArgumentMap.keySet().iterator(); (keyIterator.hasNext());) {
            String testKey = (String)keyIterator.next();
            if (testKey.matches(getCommandLineName()+"\\d+")) {
                sortedMap.put(testKey,(String)fullArgumentMap.get(testKey));
            }
        }
        return sortedMap;
    }


    private String getNumberString(String fullString) {
        StringBuffer returnString = null;
        boolean processingDigits = true;
        for (int characterIndex = fullString.length()-1; characterIndex >0 & processingDigits; characterIndex--) {
            if (Character.isDigit(fullString.charAt(characterIndex))) {
                if (returnString ==null) {
                    returnString = new StringBuffer();
                }
                returnString.append(fullString.charAt(characterIndex));
            } else {
                char ch = fullString.charAt(characterIndex);
                if (ch == '@') {
                    return null;
                }
                processingDigits = false;
            }
        }
        if (returnString!=null) {
            return returnString.reverse().toString(); /*PTM3465 for nos. > 10, it returns back 21 instead of 12*/
        } else {
            return fullString; //its going to return the fullString (with no key);
        }
    }

    public String toString() {
        StringBuilder strbd = new StringBuilder(getCommandLineName() + " " + getDescriptionOfBoolean()+"\n");
        for (Map.Entry<String,String> entry: numericKeyValuesPairsMap.entrySet()) {
            strbd.append(String.format("%3s%2s%s%n","",entry.getKey(),entry.getValue()));
        }
        strbd.append("\n");
        return strbd.toString();
    }

    private  class TemporaryArgument extends CommandLineArgument {
        private TemporaryArgument(String name,String description) {
            super(name,description);
        }
    }

    private class KeyComparator implements Comparator<String> {
        public int     compare(String  obj1, String obj2) {

            if (obj1.matches(".*\\d+$") && obj2.matches(".*\\d+$")) {
                Matcher matcher1 = Pattern.compile("\\d+$").matcher(obj1);
                Matcher matcher2 = Pattern.compile("\\d+$").matcher(obj2);
                if (matcher1.find() && matcher2.find()) {
                    MatchResult result1 = matcher1.toMatchResult();
                    MatchResult result2 = matcher2.toMatchResult();
                    if ((result1.groupCount() >= 0) && (result2.groupCount() >= 0)) {
                        try {
                            int objint1 = Integer.parseInt(result1.group());
                            int objint2=  Integer.parseInt(result2.group());
                            if (objint1 < objint2) {
                                return -1;
                            }
                            if (objint1 == objint2) {
                                return 0;
                            }

                            if (objint1 > objint2) {
                                return 1;
                            }
                        } catch (NumberFormatException nfe) {
                            //ignore it.
                        }
                    }

                }
                return 1;
            }
            return 1;
        }

        public boolean     equals(Object obj) {
            return obj.equals(this);
        }
    }
}
