import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
public class Receiver extends Thread
{
	private DatagramSocket socket;
	private AckSeq ackSeq;
	private int port;
	private String tag;
	
	public Receiver(DatagramSocket socket, AckSeq ackSeq, int targetPort, String tag)
	{
		this.socket = socket;
		this.ackSeq = ackSeq;
		port = targetPort;
		this.tag = tag;
	}
	@Override
	public void run()
	{
		try
		{
			DatagramPacket packet = new DatagramPacket(new byte[1472], 1472);
			int n = 0;
			while (true)
			{
				socket.receive(packet);
				byte[] arr = packet.getData();
				int len = packet.getLength();
				Data data = new Data(arr, len);
				if (data.isAck())
				{
					ackSeq.ackSeq = data.getSeq();
					System.out.println(tag + "\t[ack]\t" + ackSeq.ackSeq);
				}
				else
				{
					n++;
					if (n != 10)
					{
						Data data1 = new Data(data.getSeq(), true);
						byte[] bytes = data1.getBytes();
						DatagramPacket packet1 =
								new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), port);
						socket.send(packet1);
					}
					else
						n = 0;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
