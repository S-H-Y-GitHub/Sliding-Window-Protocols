import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
public class Sender extends Thread
{
	private DatagramSocket socket;
	private AckedSeq ackedSeq;
	private int windowSize = 16;
	private int timeoutLength = 2000;
	private int delay = 100;
	private int port;
	private HashMap<Integer, DatagramPacket> window;
	private int sendBase;
	private Timer t;
	private String tag;
	public Sender(DatagramSocket socket, AckedSeq ackedSeq, int targetPort, String tag)
	{
		this.socket = socket;
		this.ackedSeq = ackedSeq;
		port = targetPort;
		window = new HashMap<>();
		t = new Timer();
		this.tag = tag;
	}
	
	@Override
	public void run()
	{
		try
		{
			int seq = sendBase = 0;
			t.schedule(new TimeOutEvent(seq), timeoutLength);
			for (int i = 0; i < 100; i++)
			{
				ack(ackedSeq.ackedSeq);
				Data data = new Data(seq,false);
				byte[] bytes = data.getBytes();
				DatagramPacket packet =
						new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), port);
				if (sendData(seq, packet))
					seq++;
				else
					i--;
				Thread.sleep(delay);
				ack(ackedSeq.ackedSeq);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	synchronized private void ack(int ackedSeq)
	{
		if (sendBase < ackedSeq)
		{
			t.cancel();
			t = new Timer();
			t.schedule(new TimeOutEvent(ackedSeq + 1), timeoutLength);
		}
		for (; sendBase <= ackedSeq; sendBase++)
			window.remove(sendBase);
	}
	synchronized private Boolean sendData(int seq, DatagramPacket data) throws Exception
	{
		if (window.size() < windowSize)
		{
			window.put(seq, data);
			System.out.println(tag + "\t[send]\t" + seq);
			socket.send(data);
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
				System.out.println(tag + "\t[time]\t" + seq);
				t.schedule(new TimeOutEvent(seq), timeoutLength);
				while (window.containsKey(seq))
				{
					System.out.println(tag + "\t[send]\t" + seq);
					socket.send(window.get(seq));
					seq++;
					Thread.sleep(delay);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
