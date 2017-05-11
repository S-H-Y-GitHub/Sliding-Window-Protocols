import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
public class Receiver extends Thread
{
	private DatagramSocket socket;
	private Integer ackedSeq;
	private Integer nextSeq;
	private int port;
	
	public Receiver(DatagramSocket socket, Integer ackedSeq,, int targetPort)
	{
		this.socket = socket;
		this.ackedSeq = ackedSeq;
		port = targetPort;
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
			Data data = new Data(arr,len);
			nextSeq = data.getSeq();
			while (true)
			{
				socket.receive(packet);
				arr = packet.getData();
				len = packet.getLength();
				data = new Data(arr,len);
				if(data.isAck())
					if (data.getSeq() > ackedSeq)
						ackedSeq = data.getSeq();
				else
				{
					if(data.getSeq()==nextSeq)
					{
						nextSeq++;
						n++;
						if (n>=10)
						{
							Data data1 = new Data(nextSeq-1,true);
							byte[] bytes = data1.getBytes();
							DatagramPacket packet1 =
									new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), port);
							socket.send(packet1);
							n = 0;
						}
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
