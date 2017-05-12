import java.net.DatagramSocket;
public class SocketThread extends Thread
{
	private int selfPort;
	private int targetPort;
	private String tag;
	public SocketThread(int selfPort, int targetPort, String tag)
	{
		this.selfPort = selfPort;
		this.targetPort = targetPort;
		this.tag = tag;
	}
	@Override
	public void run()
	{
		try
		{
			DatagramSocket socket = new DatagramSocket(selfPort);
			AckSeq ackSeq = new AckSeq();
			ackSeq.ackSeq = -1;
			Receiver receiver = new Receiver(socket, ackSeq, targetPort, tag);
			Sender sender = new Sender(socket, ackSeq, targetPort, tag);
			receiver.start();
			sender.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
