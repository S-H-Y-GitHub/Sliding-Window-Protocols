import java.net.DatagramPacket;
import java.net.DatagramSocket;
public class Receiver extends Thread
{
	private DatagramSocket socket;
	private Integer ackedSeq;
	public Receiver(DatagramSocket socket, Integer ackedSeq)
	{
		this.socket = socket;
		this.ackedSeq = ackedSeq;
	}
	@Override
	public void run()
	{
		try
		{
			DatagramPacket packet = new DatagramPacket(new byte[1472], 1472);
			while (true)
			{
				socket.receive(packet);
				
				byte[] arr = packet.getData();                            //获取数据
				int len = packet.getLength();                            //获取有效的字节个数
				Data data = new Data(arr);
				if(data.getAck())
					ackedSeq = data.getSeq();
				else
				{
					//发送ACK数据包
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
