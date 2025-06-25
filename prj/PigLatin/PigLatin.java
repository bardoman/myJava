
import java.util.StringTokenizer;
import java.io.*; 

public class PigLatin
{
    public PigLatin()
    {
        String sentence, result, another;

        PigLatinTranslator translator = new PigLatinTranslator();

        do
        {
            System.out.println ();

            System.out.println ("Enter a sentence (no punctuation):");

            sentence = KeyboardReadString();

            System.out.println ();

            result = translator.translate (sentence);

            System.out.println ("That sentence in Pig Latin is:");

            System.out.println (result);

            System.out.println ();

            System.out.print ("Translate another sentence (y/n)? ");

            another = KeyboardReadString();
        }
        while(another.equalsIgnoreCase("y"));
    }

    public String KeyboardReadString() //read string function
    {
        String sText;

        try
        {
            InputStreamReader isr;

            BufferedReader br;

            isr = new InputStreamReader(System.in);

            br = new BufferedReader( isr);

            sText = br.readLine();

            return sText;
        }

        catch(IOException e) //error checking
        {

            System.out.println(e.toString());

            return null;
        }
    }

    public class PigLatinTranslator
    {
        public String translate (String sentence)
        {
            String result = "";

            sentence = sentence.toLowerCase();

            StringTokenizer tokenizer = new StringTokenizer (sentence);

            while(tokenizer.hasMoreTokens())
            {

                result += translateWord (tokenizer.nextToken());

                result += " ";
            }
            return result;
        }

        private String translateWord (String word)
        {
            String result = "";

            if(beginsWithVowel(word))

                result = word + "yay";

            else if(beginsWithPrefix(word))

                result = word.substring(2) + word.substring(0,2) + "ay";

            else  result = word.substring(1) + word.charAt(0) + "ay";

            return result;
        }


        private boolean beginsWithVowel (String word)

        {

            String vowels = "aeiouAEIOU";

            char letter = word.charAt(0);

            return(vowels.indexOf(letter) != -1);

        }

        private boolean beginsWithPrefix (String str)

        {
            return( str.startsWith ("bl") || str.startsWith ("pl") ||

                    str.startsWith ("br") || str.startsWith ("pr") ||

                    str.startsWith ("ch") || str.startsWith ("sh") ||

                    str.startsWith ("cl") || str.startsWith ("sl") ||

                    str.startsWith ("cr") || str.startsWith ("sp") ||

                    str.startsWith ("dr") || str.startsWith ("sr") ||

                    str.startsWith ("fl") || str.startsWith ("st") ||

                    str.startsWith ("fr") || str.startsWith ("th") ||

                    str.startsWith ("gl") || str.startsWith ("tr") ||

                    str.startsWith ("gr") || str.startsWith ("wh") ||

                    str.startsWith ("kl") || str.startsWith ("wr") ||

                    str.startsWith ("ph") );
        }

    } 

    public static void main (String[] args)
    {
        new PigLatin();
    }

}








