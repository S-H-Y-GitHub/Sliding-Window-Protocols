import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
public class Receiver extends Thread
{
	private DatagramSocket socket;
	private AckedSeq ackedSeq;
	private Integer nextSeq;
	private int port;
	private String tag;
	
	public Receiver(DatagramSocket socket, AckedSeq ackedSeq, int targetPort, String tag)
	{
		this.socket = socket;
		this.ackedSeq = ackedSeq;
		port = targetPort;
		this.tag = tag;
	}
	@Override
	public void run()
	{
		try
		{
			DatagramPacket packet = new DatagramPacket(new byte[1472], 1472);
			socket.receive(packet);
			int n = 1;
			byte[] arr = packet.getData();                            //获取数据
			int len = packet.getLength();                            //获取有效的字节个数
			Data data = new Data(arr, len);
			nextSeq = data.getSeq() + 1;
			while (true)
			{
				socket.receive(packet);
				arr = packet.getData();
				len = packet.getLength();
				data = new Data(arr, len);
				if (data.isAck())
				{
					if (data.getSeq() > ackedSeq.ackedSeq)
					{
						ackedSeq.ackedSeq = data.getSeq();
						System.out.println(tag + "\t[ack]\t" + ackedSeq.ackedSeq);
					}
				}
				else
				{
					n++;
					if (data.getSeq() == nextSeq)
						nextSeq++;
					if (n >= 20)
					{
						Data data1 = new Data(nextSeq - 1, true);
						byte[] bytes = data1.getBytes();
						DatagramPacket packet1 =
								new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), port);
						socket.send(packet1);
						n = 0;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
