import java.util.Arrays;
public class Data
{
	private Integer seq;
	private Boolean ack;
	private byte[] data;
	
	public Data(Integer seq,Boolean ack)
	{
		this.seq = seq;
		this.ack = ack;
		if(!ack)
		{
			data = new byte[1467];
			Arrays.fill(data, Byte.MAX_VALUE);
		}
		else
		{
			data = new byte[0];
		}
	}
	
	public Data(byte[] inputStream, int length)
	{
		if (inputStream.length < 4 || inputStream.length > 1472)
			throw new IllegalArgumentException("数据包格式非法");
		seq = 0;
		seq += inputStream[0] << 24;
		seq += inputStream[1] << 16;
		seq += inputStream[2] << 8;
		seq += inputStream[3];
		ack = inputStream[4] == 1;
		data = new byte[1467];
		System.arraycopy(inputStream, 5, data, 0, inputStream.length - 5);
	}
	public byte[] getBytes()
	{
		byte[] result = new byte[1472];
		//记住，这里是从左往右写的！！
		result[0] = (byte) ((seq >> 24) & 0xFF);
		result[1] = (byte) ((seq >> 16) & 0xFF);
		result[2] = (byte) ((seq >> 8) & 0xFF);
		result[3] = (byte) (seq & 0xFF);
		result[4] = (byte) (ack ? 1 : 0);
		System.arraycopy(data, 0, result, 5, data.length);
		return result;
	}
	public int getSeq()
	{
		return seq;
	}
	public void setSeq(int seq)
	{
		this.seq = seq;
	}
	public byte[] getData()
	{
		return data;
	}
	public void setData(byte[] data)
	{
		this.data = data;
	}
	public Boolean isAck()
	{
		return ack;
	}
	public void setAck(Boolean ack)
	{
		this.ack = ack;
	}
}
