
import java.net.*;
import java.io.*;

public class HttpGet
{

	public static void main(String[] args)
	throws Exception
	{
		String path="http://nl.ijs.si/GNUsl/tex/tunix/tips/";
                String name="node";
                String tail=".html";
                String source=null;
                String fullname=null;

                for(int i=1;i!=387;i++)
                {
                  fullname=name+i+tail;
                  source=path+fullname;

		URL url=new URL(source);

		HttpURLConnection hCon=(HttpURLConnection)url.openConnection();

		hCon.setRequestMethod("GET");

		InputStream is = hCon.getInputStream();

		FileOutputStream fos = new FileOutputStream(new File(fullname));

		byte[] buffer = new byte[100 * 1024];

		int length;

		while((length = is.read(buffer)) >= 0)
		{
			fos.write(buffer, 0, length);
		}

		fos.close();

		is.close();
                }
	}
}