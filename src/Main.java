public class Main
{
	public static void main(String[] args)
	{
		SocketThread s1 = new SocketThread(2333, 6666, "线程1");
		SocketThread s2 = new SocketThread(6666, 2333, "线程2");
		s1.start();
		s2.start();
	}
}
