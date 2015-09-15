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
	public static List<BigInteger> ai = new  ArrayList<BigInteger>();//系数ai ;
	/*private static final byte[] keybytes = new byte[] { (byte) 0xfc, (byte) 0x4f, (byte) 0xbe, (byte) 0x23,
		(byte) 0x59, (byte) 0xf2, (byte) 0x42, (byte) 0x37, (byte) 0x4c, (byte) 0x80, (byte) 0x44, (byte) 0x31,
		(byte) 0x20, (byte) 0xda, (byte) 0x20, (byte) 0x0c };*/

	private static final String HMAC_SHA1 = "HmacSHA1";	//160bit   

	/**
	 * 根据指定的密钥，产生指定个数的随机数160bit——hmacShA1
	 * @param num	随机数的个数
	 * @param key	输入的密钥
	 * @return		随机数的集合
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
			// 对于种子相同的Random对象，生成的随机数序列是一样的。			     	
			String data = String.valueOf(i+1);         
			byte[] hashValue = mac.doFinal(data.getBytes());
			//System.out.println("length:"+hashValue.length);
			list.add((new BigInteger(hashValue)).abs());
		}
		return list;
	}
	/**
	 * 生成指定范围内的多个无重复的随机数
	 * @param start	最小值
	 * @param end	最大值
	 * @param len	个数
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
	//包含种子的随机数生成器
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
	//从指定数组中产生不重复的不大于数组长度个随机数，
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
	 * @param remove  随机数不应在remove集合中
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
	 * 产生从某个随机位置开始的多个数值
	 * @param src 用来产生随机数的数组值
	 * @param count 连续的数值个数
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
	 * 产生多个均匀分布的随机数值
	 * @param src
	 * @param count //槽的个数
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
	 * 是否存在重复的随机数
	 * @param random   以生成的随机数
	 * @param ran	       新生成的随机数
	 * @return
	 */
	public static boolean  isDup(int []random,int ran){
		for (int i = 0; i < random.length; i++) {
			if(random[i]==ran)
				return true;//ran是否在random数组中
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

