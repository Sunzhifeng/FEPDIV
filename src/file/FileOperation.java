package file;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tool.GenerateRandom;
import tool.SortAlg;

/**
 * 用户本地对文件进行处理操作
 * @author MichaelSun
 * @version 2.0
 * @date 2015.01.08
 */
public class FileOperation {

	public static final int K=1024;
	public static final int M=1024*1024;

	/**
	 * 获取文件块数
	 * @param filePath
	 * @param blockSize 以K为单位
	 * @return
	 * @throws IOException 
	 */
	public static int blockNumbers(String filePath,int blockSize) throws IOException{
		File file = new File(filePath);		
		long fileLength = file.length();
		long number=fileLength/(K*blockSize);
		long remain=fileLength%(K*blockSize);		
		return (int)(remain==0?number:number+1);
	}
	//指定数据包含多少块
	public static int blockNumbers(byte[]data,int blockSizeK) throws IOException{
		long dataLength=data.length;		
		long number=dataLength/(blockSizeK);
		long remain=dataLength%(blockSizeK);		
		return (int)(remain==0?number:number+1);
	}
	/**
	 * 
	 * @param fileSizeK	文件大小KB
	 * @param s			每块段数
	 * @param sectorSize段大小
	 * @return 			文件块数
	 */
	public static int  blockNumbers(int fileSizeK,int s,int sectorSize){
		int blockSizeK=(s*sectorSize/1000);
		int fileBlocks=(int)fileSizeK/blockSizeK;
		long remain=fileSizeK%blockSizeK;		
		fileBlocks=remain>0?fileBlocks+1:fileBlocks;
		return fileBlocks;
	}
	/**
	 * 对文件按块处理，得到对应的域中元素集合
	 * @param filePath  预处理文件路径
	 * @param blockSize 逻辑分块大小
	 * @param r 		域运算
	 * @return          所有块映射后的域元素集合
	 * @throws IOException
	 */
	public static Element[] preProcessFile(String filePath,int blockSize,Field r) throws IOException{
		int blockSizeK=blockSize*K;
		int fileBlocks=blockNumbers(filePath, blockSize);
		RandomAccessFile in = new RandomAccessFile(filePath, "r");
		byte[] blockBuff=new byte[blockSizeK];//缓冲的大小刚好与块大小相等			

		Element [] pdata=new Element[fileBlocks];			
		int remainBytes;//最后一块特殊处理
		//这里可以采取多线程并行处理，生成域元素。（mapreduce的思想）		
		for(int i=0;i<fileBlocks-1;i++){//处理前fileBlocks-1块			
			in.read(blockBuff);			
			pdata[i]=r.newElementFromHash(blockBuff,0,blockSizeK);//对数据进行域运算.这里使用hash（速度稍慢），也可以用newElementFromBytes
			//pdata[i]=r.newElementFromBytes(blockBuff);//对数据进行域运算.这里使用hash（速度稍慢），也可以用newElementFromBytes
		}
		remainBytes=in.read(blockBuff);
		in.close();
		if(remainBytes==blockSizeK){
			pdata[fileBlocks-1]=r.newElementFromHash(blockBuff,0,blockSizeK);
		}else{//最后一块可能不够，需特殊处理,采用0填充
			for(int k=remainBytes;k<blockBuff.length;k++){
				blockBuff[k]=0;			
			}
			pdata[fileBlocks-1]=r.newElementFromHash(blockBuff,0,blockSizeK);			
			System.out.print("最后块大小："+remainBytes);
		}		
		return pdata;
	}

	/**
	 * 对文件进行分块与分段预处理
	 * @param filePath		文件名
	 * @param blockSize		块大小
	 * @param s				每块的段数
	 * @param sectorSize	段大小
	 * @param r				大素数阶
	 * @return				n个文件块及分段信息集合
	 * @throws IOException
	 */
	public static Element[][] preProcessFile(String filePath,int s,int sectorSize,Field r) throws IOException{
		int blockSize=s*sectorSize/1000;
		int blockSizeK=(blockSize)*K;
		int fileBlocks=blockNumbers(filePath, blockSize);
		byte[] blockBuff;//缓冲的大小刚好与块大小相等		
		Element[][] nSectors=new Element[fileBlocks][s];//段元素信息	

		RandomAccessFile in = new RandomAccessFile(filePath, "r");		

		//这里可以采取多线程并行处理，生成域元素。（mapreduce的思想）		
		for(int i=0;i<fileBlocks-1;i++){//处理前fileBlocks-1块
			blockBuff=new byte[blockSizeK];
			in.read(blockBuff,0,blockBuff.length);			
			nSectors[i]=preProcessBlock(s, blockBuff, sectorSize, r);//第i块的分段信息加到sectors中

		}
		blockBuff=new byte[blockSizeK];
		int remainBytes=in.read(blockBuff);	
		if(remainBytes==blockSizeK){//最后一块特殊处理			
			nSectors[fileBlocks-1]=(preProcessBlock(s, blockBuff, sectorSize, r));

		}else{//最后一块可能不够，需特殊处理,采用0填充
			for(int k=remainBytes;k<blockBuff.length;k++)
				blockBuff[k]=0;	
			nSectors[fileBlocks-1]=(preProcessBlock(s, blockBuff, sectorSize, r));
		}		
		in.close();	
		return nSectors;
	}


	/**
	 * 对块进行分段处理
	 * @param s 			段数
	 * @param blockData 	块数据
	 * @param sectorSize 	段大小
	 * @param r 			素数域
	 * @return 				s个域元素	
	 * @throws FileNotFoundException 
	 */
	public static Element[] preProcessBlock(int s,byte[] blockData,int sectorSize,Field r) {
		Element[] sectorNums=new Element[s];		
		for(int i=0;i<s;i++){
			byte[] buff=subByteArray(blockData,i*sectorSize,sectorSize);			
			sectorNums[i]=(r.newElementFromBytes(buff));			
		}
		return sectorNums;
	}	
	//随机读取多个块
	public static List<byte[]> readBlocks(String filePath,int blockSize,int[]blockNums) {
		int c=blockNums.length;
		byte[] data ;
		List<byte[]>cdata=new ArrayList<>();
		RandomAccessFile in;
		try {
			in = new RandomAccessFile(filePath, "r");
			in.seek(0);
			for(int i=0;i<c;i++){
				data=new byte[blockSize];
				in.seek(blockSize*(blockNums[i]-1));//定位到指定块			
				in.read(data);
				cdata.add(data);
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return cdata;
	}

	//大文件处理大于32M，二级缓存
	public static void preProcessLargeFile(String filePath,int blockSize,int s,int sectorSize,Field r) throws FileNotFoundException, IOException {  	       
		int blockSizeK=blockSize*K;
		File file = new File(filePath);
		long fileLength=file.length();//文件的总长度
		int length=0x4000000;//64M，每次读入内存的数据量
		int count=(int)(fileLength/length);//读多少次
		int remain=(int)fileLength%length;		
		count=(remain==0?count:count+1);

		FileChannel fc=new RandomAccessFile(file, "r").getChannel();
		int blockRemain=0;//64M数据除以文件块大小余下的字节数
		for(int i=0;i<count;i++){			
			int start=i*length-blockRemain;//每次映射的起始位置
			//StdOut.println(start);
			MappedByteBuffer inputBuffer;
			//内存映射文件输入流
			if (fileLength - start >= length){
				inputBuffer = fc.map(FileChannel.MapMode.READ_ONLY, start,length);// 读取64M
			}else{
				inputBuffer = fc.map(FileChannel.MapMode.READ_ONLY, start,fileLength-start);// 读取大文件  
			}

			int buffSize=16*blockSizeK;	//！！！！这里的16个块不可能大于64M	
			byte[] blocksBuff = new byte[buffSize];// 每次处理16个块大小数据  	
			for (int offset = 0; offset < length; offset += buffSize) {  
				if (length - offset >= buffSize) { 				
					inputBuffer.get(blocksBuff); 
					// 将得到的16块内容进行计算；  
					preProcessData(blocksBuff, blockSizeK, s, sectorSize, r);
				} else {  //最后一块不足buffsize大小，特殊处理		
					blockRemain=length-offset;//剩余部分重新读取下次处理

				}  
			}
		}
		fc.close();

	}

	//对一批数据进行分块与分段处理,没有采用填充，data不一定是块大小的正数倍
	private static Map<String,Object> preProcessData(byte[] data,int blockSizeK,int s,int sectorSize,Field r) throws IOException{
		int dataBlocks=blockNumbers(data, blockSizeK);		
		int remain=data.length-(dataBlocks-1)*blockSizeK;//最后一块的字节数

		Element [] pdata=new Element[dataBlocks];//块元素信息			
		List<Element[]> nSectors=new ArrayList<Element[]>(dataBlocks);//段元素信息	
		byte[] blockBuff;

		//这里可以采取多线程并行处理，生成域元素。（mapreduce的思想）		
		for(int i=0;i<dataBlocks-1;i++){//处理前fileBlocks-1块
			blockBuff=subByteArray(data, i*blockSizeK, blockSizeK);
			pdata[i]=r.newElementFromHash(blockBuff,0,blockSizeK);//对数据进行域运算.这里使用hash（速度稍慢），也可以用newElementFromBytes
			nSectors.add(preProcessBlock(s, blockBuff, sectorSize, r));//第i块的分段信息加到sectors中
		}
		blockBuff=new byte[blockSizeK];

		if(remain==blockSizeK)//处理最后一块			
			subByteArray(data, (dataBlocks-1)*blockSizeK, blockSizeK,blockBuff);
		else
			subByteArray(data, (dataBlocks-1)*blockSizeK, remain,blockBuff);

		pdata[dataBlocks-1]=r.newElementFromHash(blockBuff,0,blockSizeK);
		nSectors.add(preProcessBlock(s, blockBuff, sectorSize, r));

		Map<String,Object> blockSector=new HashMap<String,Object>(2);
		blockSector.put("pdata", pdata);
		blockSector.put("nSectors", nSectors);
		return blockSector;
	}


	/**
	 * 复制数组中指定范围内的元素
	 * @param a			给定数组
	 * @param offset	要复制元素的起始编号
	 * @param len		要复制元素的终止编号
	 * @return
	 */
	public static byte [] subByteArray(byte []a,int offset,int len){
		int aLength=a.length;
		if(aLength-offset<len)
			return null;	
		byte [] result=new byte[len];
		for(int i=0;i<len;i++){
			result[i]=a[offset+i];
		}

		return result;
	}
	public static void subByteArray(byte[] from,int offset,int len,byte[]to){
		int fromLength=from.length;
		if(fromLength-offset<len){
			System.out.println("截取范围越界！！");
			return;
		}
		for(int i=0;i<len;i++){
			to[i]=from[offset+i];
		}
	}
	//默认填充0,字节数组元素的默认值为0
	private static void fillOutZero(byte[]data,int offset,int len){		
		for(int i=0;i<len;i++){
			data[offset+i]=0;
		}
	}


	public static void clearUpFile(String configPath) throws IOException{
		File f = new File(configPath);
		FileWriter fw =  new FileWriter(f);
		fw.write("");		
		fw.close();
	}
	/**
	 * 随机生成指定大小文件
	 * @param fileSzieM 以M为单位
	 * @throws IOException
	 */
	public static void genRandomFile( String filePath,int fileSzieM) throws IOException{
		String str = "0123456789vasdjhklsadfqwiurewopt"; //自己补全字母和数字,这个字符数是作为随机取值的源
		File file=new File(filePath);
		PrintWriter pw = new PrintWriter(new FileWriter(file));
		int len = str.length();		
		for (int i = 0; i < K; i++)
		{
			StringBuilder s = new StringBuilder();
			for (int j = 0; j < (fileSzieM*K); j++)
			{
				s.append(str.charAt((int)(Math.random()*len)));
			}
			pw.print(s.toString());
		}
		pw.close();
	}

	//随机破坏文件的指定块-将块的第一个字节减去1
	public static void destoryBlocks(String filePath,int blockSize,int c)
	{
		try {
			int n=blockNumbers(filePath, blockSize);
			int [] damagedBlocks=GenerateRandom.random(1, n, c);
			SortAlg.sort(damagedBlocks, 0, damagedBlocks.length-1);
			destoryBlocks(filePath,blockSize,damagedBlocks);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//随机破坏文件的指定块——将块的第一个字节减去1
	//damagedBlocks——从小到大有序
	public static void destoryBlocks(String filePath,int blockSizeK,int[] damagedBlocks) throws IOException
	{
		try
		{  
			RandomAccessFile in=new RandomAccessFile(filePath, "rw");
			int c=damagedBlocks.length;
			//读取所有待破坏块的第一个字节
			int []old=new int [c];
			for(int i=0;i<c;i++){
				long current=(damagedBlocks[i]-1)*blockSizeK*K;
				in.seek(current);
				old[i]=in.read();//读该块的第一个字节
				//System.out.println(damagedBlocks[i]+"\t"+old[i]);				
			}
			in.close();
			in=new RandomAccessFile(filePath, "rw");

			//所有待破坏的数据块的第一个字节值+1
			for(int i=0;i<c;i++){
				long current=(damagedBlocks[i]-1)*blockSizeK*K;
				in.seek(current);
				in.write(old[i]-1);//读该块的第一个字节								
			}
			in.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	//修复损坏的数据块——待修复块的第一个字节加1
	//damagedBlocks有序
	public static void repairBlocks(String filePath,int blockSizeK,int[] damagedBlocks) throws IOException
	{
		try
		{  
			RandomAccessFile in=new RandomAccessFile(filePath, "rw");
			int c=damagedBlocks.length;
			int []old=new int [c];
			for(int i=0;i<c;i++){
				long current=(damagedBlocks[i]-1)*blockSizeK*K;
				in.seek(current);
				old[i]=in.read();//读该块的第一个字节
				//	System.out.println(damagedBlocks[i]+"\t"+old[i]);				
			}
			in.close();
			in=new RandomAccessFile(filePath, "rw");
			for(int i=0;i<c;i++){
				long current=(damagedBlocks[i]-1)*blockSizeK*K;
				in.seek(current);
				in.write(old[i]+1);//读该块的第一个字节								
			}
			in.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	//内存映射文件——适合大文件
	public static void destoryBlocksLargeFile(String filePath,int blockSize,int[] damagedBlocks) throws IOException
	{
		int length=0x4000000;
		int c=damagedBlocks.length;
		FileChannel fc=new RandomAccessFile(filePath, "rw").getChannel();
		MappedByteBuffer byteBuffer=fc.map(FileChannel.MapMode.READ_WRITE, 0, length);
		for(int i=0;i<c;i++)
		{
			int current=(damagedBlocks[i]-1)*blockSize*K;
			byteBuffer.position(current);
			byteBuffer.mark();			
			byte oldValue=byteBuffer.get();
			//System.out.println(oldValue);
			byteBuffer.reset();
			oldValue-=1;
			byteBuffer.put(oldValue);

		}
		fc.close();

	}
	//内存映射文件_适合大文件
	public static void repairBlocksLargeFile(String filePath,int blockSize,int[] damagedBlocks) throws IOException
	{

		int length=0x4000000;
		int c=damagedBlocks.length;
		FileChannel fc=new RandomAccessFile(filePath, "rw").getChannel();
		MappedByteBuffer byteBuffer=fc.map(FileChannel.MapMode.READ_WRITE, 0, length);

		for(int i=0;i<c;i++)
		{
			int current=(damagedBlocks[i]-1)*blockSize*K;
			byteBuffer.position(current);
			byteBuffer.mark();
			byte oldValue=byteBuffer.get();
			//System.out.println(oldValue);
			byteBuffer.reset();
			oldValue+=1;
			byteBuffer.put(oldValue);			
		}
		fc.close();

	}


}

