import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
public class Sender extends Thread
{
	private DatagramSocket socket;
	private AckSeq ackSeq;
	private int windowSize = 16;
	private int timeoutLength = 2000;
	private int delay = 100;
	private int port;
	private HashMap<Integer, DatagramPacket> window;
	private HashMap<Integer, Boolean> ackWindow;
	private HashMap<Integer, Timer> timers;
	private int sendBase;
	private String tag;
	public Sender(DatagramSocket socket, AckSeq ackSeq, int targetPort, String tag)
	{
		this.socket = socket;
		this.ackSeq = ackSeq;
		port = targetPort;
		window = new HashMap<>();
		ackWindow = new HashMap<>();
		timers = new HashMap<>();
		this.tag = tag;
	}
	
	@Override
	public void run()
	{
		try
		{
			int seq = sendBase = 0;
			for (int i = 0; i < 100; i++)
			{
				ack(ackSeq.ackSeq);
				Data data = new Data(seq,false);
				byte[] bytes = data.getBytes();
				DatagramPacket packet =
						new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), port);
				if (sendData(seq, packet))
					seq++;
				else
					i--;
				Thread.sleep(delay);
				ack(ackSeq.ackSeq);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	synchronized private void ack(int ackedSeq)
	{
		if (ackWindow.containsKey(ackedSeq) && !ackWindow.get(ackedSeq))
		{
			//更新ACK表
			ackWindow.put(ackedSeq, true);
			//停止计时器
			timers.get(ackedSeq).cancel();
			timers.remove(ackedSeq);
			//滑动窗口
			if (ackedSeq == sendBase)
				for (; ackWindow.containsKey(sendBase) && ackWindow.get(sendBase); sendBase++)
					window.remove(sendBase);
		}
	}
	synchronized private Boolean sendData(int seq, DatagramPacket data) throws Exception
	{
		//判断窗口是否已满
		if (window.size() < windowSize)
		{
			//加入窗口
			window.put(seq, data);
			ackWindow.put(seq, false);
			System.out.println(tag + "\t[send]\t" + seq);
			//发送数据
			socket.send(data);
			//开始计时
			Timer t = new Timer();
			t.schedule(new TimeOutEvent(seq), timeoutLength, timeoutLength);
			timers.put(seq, t);
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
				System.out.println(tag + "\t[send]\t" + seq);
				//重发分组
				socket.send(window.get(seq));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
