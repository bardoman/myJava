
import com.thoughtworks.xstream.*;
import java.util.*;

public class XStreamTest
{
    XStreamTest()
    {
        XStream xstream = new XStream();

        Hashtable newHash = new Hashtable();

        newHash.put("a","a");

        newHash.put("b","b");

        newHash.put("c","c");

        String hashXml = xstream.toXML(newHash);

        Hashtable newHashXml = (Hashtable) xstream.fromXML(hashXml);

        if(newHashXml.equals(newHash))
        {
            System.out.println("These suckers are the same dude");
        }

        //

        Person joe = new Person("a","b","c");

        String xml = xstream.toXML(joe);

        Person newJoe = (Person)xstream.fromXML(xml);

        if(newJoe.equals(joe))
        {
            System.out.println("These suckers are the same dude");
        }


        String tst = xstream.toXML(this);

        XStreamTest newTst = (XStreamTest)xstream.fromXML(tst);

        if(this.equals(newTst))
        {
            System.out.println("These suckers are the same dude");
        }

    }

    public static void main(String args[])
    {

        new XStreamTest(); 

    }



    class Person
    {
        private String a;
        private String b;
        private String c;

        public Person(String a,String b,String c)
        {
            this.a=a;
            this.b=b;
            this.c=c;
        }
    }


}

