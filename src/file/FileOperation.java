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
 * �û����ض��ļ����д������
 * @author MichaelSun
 * @version 2.0
 * @date 2015.01.08
 */
public class FileOperation {

	public static final int K=1024;
	public static final int M=1024*1024;

	/**
	 * ��ȡ�ļ�����
	 * @param filePath
	 * @param blockSize ��KΪ��λ
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
	//ָ�����ݰ������ٿ�
	public static int blockNumbers(byte[]data,int blockSizeK) throws IOException{
		long dataLength=data.length;		
		long number=dataLength/(blockSizeK);
		long remain=dataLength%(blockSizeK);		
		return (int)(remain==0?number:number+1);
	}
	/**
	 * 
	 * @param fileSizeK	�ļ���СKB
	 * @param s			ÿ�����
	 * @param sectorSize�δ�С
	 * @return 			�ļ�����
	 */
	public static int  blockNumbers(int fileSizeK,int s,int sectorSize){
		int blockSizeK=(s*sectorSize/1000);
		int fileBlocks=(int)fileSizeK/blockSizeK;
		long remain=fileSizeK%blockSizeK;		
		fileBlocks=remain>0?fileBlocks+1:fileBlocks;
		return fileBlocks;
	}
	/**
	 * ���ļ����鴦���õ���Ӧ������Ԫ�ؼ���
	 * @param filePath  Ԥ�����ļ�·��
	 * @param blockSize �߼��ֿ��С
	 * @param r 		������
	 * @return          ���п�ӳ������Ԫ�ؼ���
	 * @throws IOException
	 */
	public static Element[] preProcessFile(String filePath,int blockSize,Field r) throws IOException{
		int blockSizeK=blockSize*K;
		int fileBlocks=blockNumbers(filePath, blockSize);
		RandomAccessFile in = new RandomAccessFile(filePath, "r");
		byte[] blockBuff=new byte[blockSizeK];//����Ĵ�С�պ�����С���			

		Element [] pdata=new Element[fileBlocks];			
		int remainBytes;//���һ�����⴦��
		//������Բ�ȡ���̲߳��д���������Ԫ�ء���mapreduce��˼�룩		
		for(int i=0;i<fileBlocks-1;i++){//����ǰfileBlocks-1��			
			in.read(blockBuff);			
			pdata[i]=r.newElementFromHash(blockBuff,0,blockSizeK);//�����ݽ���������.����ʹ��hash���ٶ���������Ҳ������newElementFromBytes
			//pdata[i]=r.newElementFromBytes(blockBuff);//�����ݽ���������.����ʹ��hash���ٶ���������Ҳ������newElementFromBytes
		}
		remainBytes=in.read(blockBuff);
		in.close();
		if(remainBytes==blockSizeK){
			pdata[fileBlocks-1]=r.newElementFromHash(blockBuff,0,blockSizeK);
		}else{//���һ����ܲ����������⴦��,����0���
			for(int k=remainBytes;k<blockBuff.length;k++){
				blockBuff[k]=0;			
			}
			pdata[fileBlocks-1]=r.newElementFromHash(blockBuff,0,blockSizeK);			
			System.out.print("�����С��"+remainBytes);
		}		
		return pdata;
	}

	/**
	 * ���ļ����зֿ���ֶ�Ԥ����
	 * @param filePath		�ļ���
	 * @param blockSize		���С
	 * @param s				ÿ��Ķ���
	 * @param sectorSize	�δ�С
	 * @param r				��������
	 * @return				n���ļ��鼰�ֶ���Ϣ����
	 * @throws IOException
	 */
	public static Element[][] preProcessFile(String filePath,int s,int sectorSize,Field r) throws IOException{
		int blockSize=s*sectorSize/1000;
		int blockSizeK=(blockSize)*K;
		int fileBlocks=blockNumbers(filePath, blockSize);
		byte[] blockBuff;//����Ĵ�С�պ�����С���		
		Element[][] nSectors=new Element[fileBlocks][s];//��Ԫ����Ϣ	

		RandomAccessFile in = new RandomAccessFile(filePath, "r");		

		//������Բ�ȡ���̲߳��д���������Ԫ�ء���mapreduce��˼�룩		
		for(int i=0;i<fileBlocks-1;i++){//����ǰfileBlocks-1��
			blockBuff=new byte[blockSizeK];
			in.read(blockBuff,0,blockBuff.length);			
			nSectors[i]=preProcessBlock(s, blockBuff, sectorSize, r);//��i��ķֶ���Ϣ�ӵ�sectors��

		}
		blockBuff=new byte[blockSizeK];
		int remainBytes=in.read(blockBuff);	
		if(remainBytes==blockSizeK){//���һ�����⴦��			
			nSectors[fileBlocks-1]=(preProcessBlock(s, blockBuff, sectorSize, r));

		}else{//���һ����ܲ����������⴦��,����0���
			for(int k=remainBytes;k<blockBuff.length;k++)
				blockBuff[k]=0;	
			nSectors[fileBlocks-1]=(preProcessBlock(s, blockBuff, sectorSize, r));
		}		
		in.close();	
		return nSectors;
	}


	/**
	 * �Կ���зֶδ���
	 * @param s 			����
	 * @param blockData 	������
	 * @param sectorSize 	�δ�С
	 * @param r 			������
	 * @return 				s����Ԫ��	
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
	//�����ȡ�����
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
				in.seek(blockSize*(blockNums[i]-1));//��λ��ָ����			
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

	//���ļ��������32M����������
	public static void preProcessLargeFile(String filePath,int blockSize,int s,int sectorSize,Field r) throws FileNotFoundException, IOException {  	       
		int blockSizeK=blockSize*K;
		File file = new File(filePath);
		long fileLength=file.length();//�ļ����ܳ���
		int length=0x4000000;//64M��ÿ�ζ����ڴ��������
		int count=(int)(fileLength/length);//�����ٴ�
		int remain=(int)fileLength%length;		
		count=(remain==0?count:count+1);

		FileChannel fc=new RandomAccessFile(file, "r").getChannel();
		int blockRemain=0;//64M���ݳ����ļ����С���µ��ֽ���
		for(int i=0;i<count;i++){			
			int start=i*length-blockRemain;//ÿ��ӳ�����ʼλ��
			//StdOut.println(start);
			MappedByteBuffer inputBuffer;
			//�ڴ�ӳ���ļ�������
			if (fileLength - start >= length){
				inputBuffer = fc.map(FileChannel.MapMode.READ_ONLY, start,length);// ��ȡ64M
			}else{
				inputBuffer = fc.map(FileChannel.MapMode.READ_ONLY, start,fileLength-start);// ��ȡ���ļ�  
			}

			int buffSize=16*blockSizeK;	//�������������16���鲻���ܴ���64M	
			byte[] blocksBuff = new byte[buffSize];// ÿ�δ���16�����С����  	
			for (int offset = 0; offset < length; offset += buffSize) {  
				if (length - offset >= buffSize) { 				
					inputBuffer.get(blocksBuff); 
					// ���õ���16�����ݽ��м��㣻  
					preProcessData(blocksBuff, blockSizeK, s, sectorSize, r);
				} else {  //���һ�鲻��buffsize��С�����⴦��		
					blockRemain=length-offset;//ʣ�ಿ�����¶�ȡ�´δ���

				}  
			}
		}
		fc.close();

	}

	//��һ�����ݽ��зֿ���ֶδ���,û�в�����䣬data��һ���ǿ��С��������
	private static Map<String,Object> preProcessData(byte[] data,int blockSizeK,int s,int sectorSize,Field r) throws IOException{
		int dataBlocks=blockNumbers(data, blockSizeK);		
		int remain=data.length-(dataBlocks-1)*blockSizeK;//���һ����ֽ���

		Element [] pdata=new Element[dataBlocks];//��Ԫ����Ϣ			
		List<Element[]> nSectors=new ArrayList<Element[]>(dataBlocks);//��Ԫ����Ϣ	
		byte[] blockBuff;

		//������Բ�ȡ���̲߳��д���������Ԫ�ء���mapreduce��˼�룩		
		for(int i=0;i<dataBlocks-1;i++){//����ǰfileBlocks-1��
			blockBuff=subByteArray(data, i*blockSizeK, blockSizeK);
			pdata[i]=r.newElementFromHash(blockBuff,0,blockSizeK);//�����ݽ���������.����ʹ��hash���ٶ���������Ҳ������newElementFromBytes
			nSectors.add(preProcessBlock(s, blockBuff, sectorSize, r));//��i��ķֶ���Ϣ�ӵ�sectors��
		}
		blockBuff=new byte[blockSizeK];

		if(remain==blockSizeK)//�������һ��			
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
	 * ����������ָ����Χ�ڵ�Ԫ��
	 * @param a			��������
	 * @param offset	Ҫ����Ԫ�ص���ʼ���
	 * @param len		Ҫ����Ԫ�ص���ֹ���
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
			System.out.println("��ȡ��ΧԽ�磡��");
			return;
		}
		for(int i=0;i<len;i++){
			to[i]=from[offset+i];
		}
	}
	//Ĭ�����0,�ֽ�����Ԫ�ص�Ĭ��ֵΪ0
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
	 * �������ָ����С�ļ�
	 * @param fileSzieM ��MΪ��λ
	 * @throws IOException
	 */
	public static void genRandomFile( String filePath,int fileSzieM) throws IOException{
		String str = "0123456789vasdjhklsadfqwiurewopt"; //�Լ���ȫ��ĸ������,����ַ�������Ϊ���ȡֵ��Դ
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

	//����ƻ��ļ���ָ����-����ĵ�һ���ֽڼ�ȥ1
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

	//����ƻ��ļ���ָ���顪������ĵ�һ���ֽڼ�ȥ1
	//damagedBlocks������С��������
	public static void destoryBlocks(String filePath,int blockSizeK,int[] damagedBlocks) throws IOException
	{
		try
		{  
			RandomAccessFile in=new RandomAccessFile(filePath, "rw");
			int c=damagedBlocks.length;
			//��ȡ���д��ƻ���ĵ�һ���ֽ�
			int []old=new int [c];
			for(int i=0;i<c;i++){
				long current=(damagedBlocks[i]-1)*blockSizeK*K;
				in.seek(current);
				old[i]=in.read();//���ÿ�ĵ�һ���ֽ�
				//System.out.println(damagedBlocks[i]+"\t"+old[i]);				
			}
			in.close();
			in=new RandomAccessFile(filePath, "rw");

			//���д��ƻ������ݿ�ĵ�һ���ֽ�ֵ+1
			for(int i=0;i<c;i++){
				long current=(damagedBlocks[i]-1)*blockSizeK*K;
				in.seek(current);
				in.write(old[i]-1);//���ÿ�ĵ�һ���ֽ�								
			}
			in.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	//�޸��𻵵����ݿ顪�����޸���ĵ�һ���ֽڼ�1
	//damagedBlocks����
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
				old[i]=in.read();//���ÿ�ĵ�һ���ֽ�
				//	System.out.println(damagedBlocks[i]+"\t"+old[i]);				
			}
			in.close();
			in=new RandomAccessFile(filePath, "rw");
			for(int i=0;i<c;i++){
				long current=(damagedBlocks[i]-1)*blockSizeK*K;
				in.seek(current);
				in.write(old[i]+1);//���ÿ�ĵ�һ���ֽ�								
			}
			in.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	//�ڴ�ӳ���ļ������ʺϴ��ļ�
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
	//�ڴ�ӳ���ļ�_�ʺϴ��ļ�
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

