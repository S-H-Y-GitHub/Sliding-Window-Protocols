import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
public class Sender extends Thread
{
	private DatagramSocket socket;
	private Integer ackedSeq;
	private int windowSize = 16;
	private int timeoutLength = 2000;
	private int port;
	private HashMap<Integer, DatagramPacket> window;
	private int sendBase;
	
	public Sender(DatagramSocket socket, Integer ackedSeq, int targetPort)
	{
		this.socket = socket;
		this.ackedSeq = ackedSeq;
		port = targetPort;
		window = new HashMap<>();
	}
	
	@Override
	public void run()
	{
		try
		{
			int seq = sendBase = 0;
			for (int i = 0; i < 100; i++)
			{
				Data data = new Data(seq);
				byte[] bytes = data.getBytes();
				DatagramPacket packet =
						new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), port);
				socket.send(packet);
			}
			socket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void ack(int ackedSeq)
	{
		for (; sendBase < ackedSeq; sendBase++)
			window.remove(sendBase);
	}
	
	private Boolean addToWindow(int seq, DatagramPacket data) throws Exception
	{
		if (window.size() < windowSize)
		{
			window.put(seq,data);
			socket.send(data);
			return true;
		}
		return false;
	}
	private void resend(int seq) throws Exception
	{
		socket.send(window.get(seq));
	}
}
