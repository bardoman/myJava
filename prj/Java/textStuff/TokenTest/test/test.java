package test;

import java.util.*;

public class test {
String str="als;als;f ssdkfj sklkfdj ;alskflskdf";

  public test() {

  StringTokenizer strTok=new StringTokenizer(str);

  System.out.println(strTok.countTokens());
  strTok.nextElement();
   System.out.println(strTok.countTokens());
    strTok.nextElement();
   System.out.println(strTok.countTokens());
  }
  public static void main(String[] args) {
    test test1 = new test();
  }
}