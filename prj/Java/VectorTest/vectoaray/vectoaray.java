
import java.util.*;


public class vectoaray
{
	Vector vect=new Vector();
	Spud spud[];

	public class Spud
	{
		int num=0;

		public Spud(int n)
		{
			num=n;
		}

		public void showSpud()
		{
			System.out.println("spud="+num);
		}
	};

	public vectoaray()
	{
		int i;

		spud=new Spud[10];

		for(i=0;i!=spud.length;i++)
		{
			vect.add(new Spud(i));
		}

		spud= (Spud[]) vect.toArray(spud);

		for(i=0;i!=spud.length;i++)
		{
			spud[i].showSpud();
		}

	}















	public static void main(String args[])
	{

		new vectoaray();
	}
}
