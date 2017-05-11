import java.net.DatagramSocket;
public class SocketThread extends Thread
{
	private int selfPort;
	private int targetPort;
	public SocketThread(int selfPort,int targetPort)
	{
		this.selfPort = selfPort;
		this.targetPort = targetPort;
	}
	@Override
	public void run()
	{
		try
		{
			DatagramSocket socket = new DatagramSocket(selfPort);
			Integer ackedSeq = 0;
			Receiver receiver = new Receiver(socket,ackedSeq);
			Sender sender = new Sender(socket,ackedSeq,targetPort);
			receiver.start();
			sender.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
