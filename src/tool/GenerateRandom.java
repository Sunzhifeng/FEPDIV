package tool;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class GenerateRandom {	
	public static List<BigInteger> ai = new  ArrayList<BigInteger>();//ϵ��ai ;
	/*private static final byte[] keybytes = new byte[] { (byte) 0xfc, (byte) 0x4f, (byte) 0xbe, (byte) 0x23,
		(byte) 0x59, (byte) 0xf2, (byte) 0x42, (byte) 0x37, (byte) 0x4c, (byte) 0x80, (byte) 0x44, (byte) 0x31,
		(byte) 0x20, (byte) 0xda, (byte) 0x20, (byte) 0x0c };*/

	private static final String HMAC_SHA1 = "HmacSHA1";	//160bit   

	/**
	 * ����ָ������Կ������ָ�������������160bit����hmacShA1
	 * @param num	������ĸ���
	 * @param key	�������Կ
	 * @return		������ļ���
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 */
	public static List<BigInteger> GenerateRandom160(int num,byte[]key) throws NoSuchAlgorithmException, InvalidKeyException
	{
		List<BigInteger> list = new  ArrayList<BigInteger>();
		//Random ran = new Random(r);            
		SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1);     
		Mac mac = Mac.getInstance(HMAC_SHA1);     
		mac.init(signingKey);     
		for (int i = 0; i < num; i++)
		{
			// ����������ͬ��Random�������ɵ������������һ���ġ�			     	
			String data = String.valueOf(i+1);         
			byte[] hashValue = mac.doFinal(data.getBytes());
			//System.out.println("length:"+hashValue.length);
			list.add((new BigInteger(hashValue)).abs());
		}
		return list;
	}
	/**
	 * ����ָ����Χ�ڵĶ�����ظ��������
	 * @param start	��Сֵ
	 * @param end	���ֵ
	 * @param len	����
	 * @return
	 */
	public static  int[] random(int start,int end,int len){
		int [] rst=new int[len];
		Arrays.fill(rst,start-1);
		Random r=new Random();
		for (int i = 0; i < rst.length; ) {
			int ran=r.nextInt(end-start+1)+start;
			if(!isDup(rst, ran)){
				rst[i++]=ran;
			}

		}
		return rst;
	}
	//�������ӵ������������
	public static  int[] random(int start,int end,int len,long seek){
		int [] rst=new int[len];
		Arrays.fill(rst,start-1);
		Random r=new Random(seek);
		for (int i = 0; i < rst.length; ) {
			int ran=r.nextInt(end-start+1)+start;
			if(!isDup(rst, ran)){
				rst[i++]=ran;
				//	System.out.println("num:"+ran);
			}

		}

		return rst;
	}
	//��ָ�������в������ظ��Ĳ��������鳤�ȸ��������
	public static int [] random(int [] src,int len){
		int [] rst=new int[len];
		Random r=new Random();
		for (int i = 0; i < rst.length; ) {
			int index=r.nextInt(src.length);
			int ran=src[index];
			if(!isDup(rst, ran)){
				rst[i++]=ran;
			}

		}
		return rst;
	}

	/**
	 * 
	 * @param src
	 * @param len
	 * @param remove  �������Ӧ��remove������
	 * @return
	 */
	public static int [] random(int  [] src,int len,Set<Integer>remove){
		int [] rst=new int[len];
		Random r=new Random();
		for (int i = 0; i < rst.length; ) {
			int index=r.nextInt(src.length);
			int ran=src[index];
			if(!isDup(rst, ran)&&!remove.contains(ran)){
				rst[i++]=ran;
			}

		}
		return rst;
	}

	/**
	 * ������ĳ�����λ�ÿ�ʼ�Ķ����ֵ
	 * @param src �������������������ֵ
	 * @param count ��������ֵ����
	 */
	public static int [] sequentRandom(int[] src ,int count,long seek){
		int [] rst=new int[count];
		Random r =new Random(seek);
		int start=r.nextInt(src.length);
		int remain=(src.length-start)>=count?0:(count-(src.length-start));
		for(int i=0;i<count;i++){
			if(remain==0)
				rst[i]=src[start+i];
			else
				rst[i]=src[start-i];
		}
		return rst;

	}
	/**
	 * ����������ȷֲ��������ֵ
	 * @param src
	 * @param count //�۵ĸ���
	 * @return 
	 */
	public static int [] meanRandom(int[] src ,int count,long seek){
		int [] rst=new int[count];	
		Random ran=new Random(seek);
		int scale=src.length/count;			
		for(int i=0;i<count;i++){
			int index=0;			
			index=scale*i+ran.nextInt(scale);					
			rst[i]=src[index];
		}

		return rst;
	}


	public static int [] random(int [] src,int len,long seek){
		int [] rst=new int[len];
		Random r=new Random(seek);
		for (int i = 0; i < rst.length; ) {
			int index=r.nextInt(src.length);
			int ran=src[index];
			if(!isDup(rst, ran)){
				rst[i++]=ran;
			}

		}
		return rst;
	}
	/**
	 * �Ƿ�����ظ��������
	 * @param random   �����ɵ������
	 * @param ran	       �����ɵ������
	 * @return
	 */
	public static boolean  isDup(int []random,int ran){
		for (int i = 0; i < random.length; i++) {
			if(random[i]==ran)
				return true;//ran�Ƿ���random������
		}
		return false;
	}
    
	public static void shuffle(int []array){
		Random r=new Random();		
		for(int i=array.length-1;i>0;i--)
		{
			int j=r.nextInt(i+1);
			int temp=array[j];
			array[j]=array[i];
			array[i]=temp;
		}
		
	}
	public static void main(String [] args) throws InvalidKeyException, NoSuchAlgorithmException{
		int [] a={1,2,3,4,5,6,7,8,9,10,11};
		int [] b=random(a,5,System.currentTimeMillis());
		for(int value:b)
			System.out.print(value+" ");
		System.out.println();
		shuffle(b);
		for(int value:b)
			System.out.print(value+" ");
		
	}

}

