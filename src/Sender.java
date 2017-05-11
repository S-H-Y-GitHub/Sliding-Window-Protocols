import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
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
				ack(ackedSeq);
				Thread.sleep(100);
				Data data = new Data(seq,false);
				byte[] bytes = data.getBytes();
				DatagramPacket packet =
						new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), port);
				if (addToWindow(seq, packet))
					seq++;
				else
					i--;
				ack(ackedSeq);
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
			window.put(seq, data);
			socket.send(data);
			Timer t = new Timer();
			t.schedule(new TimeOutEvent(seq), timeoutLength);
			return true;
		}
		return false;
	}
	class TimeOutEvent extends TimerTask
	{
		private int seq;
		public TimeOutEvent(int seq)
		{
			this.seq = seq;
		}
		@Override
		public void run()
		{
			try
			{
				socket.send(window.get(seq));
				Timer t = new Timer();
				t.schedule(new TimeOutEvent(seq), timeoutLength);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
