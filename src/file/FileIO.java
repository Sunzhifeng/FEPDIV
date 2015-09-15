package file;

import it.unisa.dia.gas.jpbc.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FileIO {
	public static final int K=1024;
	public static final int M=1024*1024;
	/**
	 * 以字节为单位读取文件，可用于读二进制文件，如图片、声音、影像等文件。
	 * @param filePath 文件的名
	 */
	public static void readFileByBytes(String filePath)throws Exception{
		File file = new File(filePath);
		InputStream in = null;			
		in = new FileInputStream(file);
		while(in.read() != -1){	
			//对读到的数据进行处理
		}		
		in.close();

	}
	/**
	 * 以字节为单位读取文件，一次读多字节
	 * @param filePath 文件路径
	 */
	public static void readFileByMulBytes(String filePath)throws Exception{
		InputStream in = null;
		byte[] tempbytes = new byte[100];
		in = new FileInputStream(filePath);	
		while ((in.read(tempbytes)) != -1){
			//对读到的数据进行处理
		}		
		if (in != null)				
			in.close();			



	}
	/**
	 * 以字符为单位读取文件，常用于读文本，数字等类型的文件
	 * @param filePath 文件名
	 */
	public static void readFileByChars(String filePath)throws Exception{
		File file = new File(filePath);
		Reader reader = null;

		// 一次读一个字符
		reader = new InputStreamReader(new FileInputStream(file));
		int tempchar;
		while ((tempchar = reader.read()) != -1){
			//对于windows下，rn这两个字符在一起时，表示一个换行。
			//但如果这两个字符分开显示时，会换两次行。
			//因此，屏蔽掉r，或者屏蔽n。否则，将会多出很多空行。
			if (((char)tempchar) != 'r'){
				System.out.print((char)tempchar);
			}
		}
		reader.close();


	}

	public static void readFileByMulChars(String filePath)throws Exception{
		Reader reader = null;
		try {
			System.out.println("以字符为单位读取文件内容，一次读多个字节：");
			//一次读多个字符
			char[] tempchars = new char[30];
			int charread = 0;
			reader = new InputStreamReader(new FileInputStream(filePath));

			while ((charread = reader.read(tempchars))!=-1){
				//同样屏蔽掉r不显示
				if ((charread == tempchars.length)&&(tempchars[tempchars.length-1] != 'r')){
					System.out.print(tempchars);
				}else{
					for (int i=0; i<charread; i++){
						if(tempchars[i] == 'r'){
							continue;
						}else{
							System.out.print(tempchars[i]);
						}
					}
				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}finally {
			if (reader != null){
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}


	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 * @param filePath 文件名
	 */
	public static void readFileByLines(String filePath)throws Exception{
		File file = new File(filePath);
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file));
		String tempString = null;
		int line = 1;
		//一次读入一行，直到读入null为文件结束
		while ((tempString = reader.readLine()) != null){
			//handle tempString
			line=line+1;
		}
		reader.close();

	}

	//通过扫描文件所有行，提取想要的部分行
	//lineNum 有序
	public static List<String> readFileByLines(String filePath,int []lineNums)throws Exception{
		List<String> wantedLines=new ArrayList<String>(10);
		File file = new File(filePath);
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file));
		String tempString = null;
		int line = 1;
		int i=0;
		//一次读入一行，直到读入null为文件结束
		while ((tempString = reader.readLine()) != null&&i<lineNums.length){
			if(line==lineNums[i]){
				i++;
				wantedLines.add(tempString);
			}
			line++;
		}
		reader.close();
		return wantedLines;

	}



	/**
	 * 随机读取文件，一次读取n个指定长度（块数）	
	 * @param filePath
	 * @param beginIndex
	 * @param length
	 * @param n
	 */
	public static void readFileByRandomAccess(String filePath,int beginIndex,int length,int n)throws Exception{
		RandomAccessFile randomFile = null;				
		randomFile = new RandomAccessFile(filePath, "r");
		long fileLength = randomFile.length();	
		int randomPosit;
		for (int i = 0; i < n; i++) {
			//定位随机位置
			randomPosit=new Random().nextInt((int)(fileLength-length));			
			randomFile.seek(randomPosit);	
			int byteread = 0;
			while(byteread<length){
				randomFile.readByte();
				byteread++;
			}	
		}
		if (randomFile != null)				
			randomFile.close();


	}

	/**
	 * 随机读取文件，一次读取一个指定长度
	 * @param filePath
	 * @param beginIndex
	 * @param blockSizeK 块的大小
	 * @param length	50的整数倍
	 */
	public static void readFileByRandomAccess(String filePath,long beginIndex,long length)throws Exception{
		RandomAccessFile randomFile = null;
		try {
			randomFile = new RandomAccessFile(filePath, "r");
			long fileLength = randomFile.length();			
			randomFile.seek(beginIndex);			
			if(length>(fileLength-beginIndex))//到文件末尾的长度不够length
				length=(fileLength-beginIndex);
			int byteread = 0;
			while(byteread<length){//逐个字节读取length长度
				randomFile.readByte();
				byteread++;
			}	

		} catch (IOException e){
			e.printStackTrace();
		} finally {
			if (randomFile != null){
				try {

					randomFile.close();
				} catch (IOException e1) {
				}
			}
		}
	}


	/**
	 * 随机读取文件连续n行	
	 * @param filePath
	 * @param beginIndex
	 * @param length 每行长度
	 * @param n			
	 */
	public static List<String> readSequentMulLine(String filePath,int beginIndex,int length,int n)throws Exception{
		List<String> readData=new ArrayList<String>();
		RandomAccessFile randomFile = new RandomAccessFile(filePath, "r");
		randomFile.seek(beginIndex);
		for (int i = 0; i < n; i++) {
			//定位随机位置			
			String data=randomFile.readLine();
			readData.add(data);			

		}
		if (randomFile != null)				
			randomFile.close();
		return readData;

	}
	//文件记录不是定长，无法实现 
	public static List<String> readRandomMulLine(String filePath,int [] nBeginIndex,int length,int n)throws Exception{
		List<String> readData=new ArrayList<String>();
		RandomAccessFile randomFile = new RandomAccessFile(filePath, "r");
		for (int i = 0; i < n; i++) {
			randomFile.seek(nBeginIndex[i]*length);
			//定位随机位置			
			String data=randomFile.readLine();
			readData.add(data);			

		}
		if (randomFile != null)				
			randomFile.close();
		return readData;

	}

	/**
	 * 读取大文件并进行处理——MappedByteBuffer进行缓冲
	 * @param fileName
	 * @param fileLength
	 * @param buffSize
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void readLargeFile(String fileName,long fileLength,int buffSize) throws FileNotFoundException, IOException {  	       

		File file = new File(fileName);  
		//fileLength = file.length();  
		@SuppressWarnings("resource")
		FileChannel fc=new RandomAccessFile(file, "r").getChannel();
		MappedByteBuffer inputBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,	fileLength);// 读取大文件  

		byte[] dst = new byte[buffSize];// 每次读出3M的内容  

		for (int offset = 0; offset < fileLength; offset += buffSize) {  
			if (fileLength - offset >= buffSize) { 				
				inputBuffer.get(dst);  
			} else {  //最后一块不足buffsize大小，特殊处理
				for (int i = 0; i < fileLength - offset; i++)  
					dst[i] = inputBuffer.get(offset + i);  
			}  
			// 将得到的3M内容进行计算；  
		}
		fc.close();

	}  
	
	/**
	 * A方法追加文件：使用RandomAccessFile
	 *
	 * @param fileName		 文件名
	 *           
	 * @param content		追加的内容
	 * @throws IOException 
	 *            
	 */
	public static void appendString(String fileName, String content) throws IOException {

		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
		// 文件长度，字节数
		long fileLength = randomFile.length();
		// 将写文件指针移到文件尾。
		randomFile.seek(fileLength);
		randomFile.writeBytes(content);
		randomFile.close();

	}

	public static void appendByteArray(String fileName, byte[] content) throws IOException {

		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
		// 文件长度，字节数
		long fileLength = randomFile.length();
		// 将写文件指针移到文件尾。
		randomFile.seek(fileLength);
		randomFile.write(content);
		randomFile.close();
	}

	public static void appendElementArray(String fileName, Element[] content) throws IOException {
		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");

		// 将写文件指针移到文件尾。
		long fileLength = randomFile.length();		
		randomFile.seek(fileLength);

		for(int i=0;i<content.length;i++){
			randomFile.write(content[i].toBytes());
			randomFile.writeBytes("\n\r");
		}
		randomFile.close();
	}

	public static void appendListElement(String fileName, List<List<Element>> content) throws IOException {
		int size=content.size();

		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");

		// 将写文件指针移到文件尾。
		long fileLength = randomFile.length();		
		randomFile.seek(fileLength);

		for(int i=0;i<size;i++){
			List<Element> data=content.get(i);
			int length=data.size();
			for(int j=0;j<length;j++){
				randomFile.write(data.get(j).toBytes());
			}
			randomFile.writeBytes("\n\r");
		}
		randomFile.close();
	}

	public static void appendListElementArray(String fileName, List<Element[]> content) throws IOException {
		int size=content.size();

		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");

		// 将写文件指针移到文件尾。
		long fileLength = randomFile.length();		
		randomFile.seek(fileLength);
		for(int i=0;i<size;i++){
			Element[] data=content.get(i);
			int length=data.length;
			for(int j=0;j<length;j++){
				randomFile.write(data[i].toBytes());
			}
			randomFile.writeBytes("\n\r");
		}
		randomFile.close();
	}
	/**
	 * B方法追加文件：使用FileWriter
	 *
	 * @param fileName
	 * @param content
	 * @throws IOException 
	 */
	public static void appendMethodB(String fileName, String content) throws IOException {

		// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
		FileWriter writer = new FileWriter(fileName, true);
		writer.write(content);
		writer.close();

	}

	/**
	 * 随机生成指定大小文件
	 * @param fileSzieM 以M为单位
	 * @throws IOException
	 */
	public static void genRandomFile( String filePath,int fileSzieM) throws IOException{
		String str = "0123456789vasdjhklsadfqwiurewopt"; //自己补全字母和数字,这个字符数是作为随机取值的源
		PrintWriter pw = new PrintWriter(new FileWriter(filePath));
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

	public static void writeElementArray(String fileName, Element[] content) throws IOException {
		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
		for(int i=0;i<content.length;i++){
			randomFile.write(content[i].toBytes());
			randomFile.writeBytes("\n");
		}
		randomFile.close();
	}
	//
	public static void writeListListElement(String fileName, List<List<Element>> content) throws IOException {
		int size=content.size();
		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
		for(int i=0;i<size;i++){
			List<Element> data=content.get(i);
			int length=data.size();
			for(int j=0;j<length;j++){
				randomFile.write(data.get(j).toBytes());
			}
		}
		randomFile.close();
	}

	public static void writeListElementArray(String fileName, List<Element[]> content) throws IOException {
		int size=content.size();
		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
		for(int i=0;i<size;i++){
			Element[] data=content.get(i);
			int length=data.length;
			for(int j=0;j<length;j++){
				randomFile.write(data[j].toBytes());
			}
		}
		randomFile.close();
	}


	/**
	 * 写大文件——内存映射文件
	 * @param fileName	
	 * @param length	
	 * @throws Exception
	 */
	public static void writeLargeFile(String fileName,int length)throws Exception {
		@SuppressWarnings("resource")
		FileChannel fc = new RandomAccessFile(fileName, "rw").getChannel();  
		//注意，文件通道的可读可写要建立在文件流本身可读写的基础之上  
		MappedByteBuffer out = fc.map(FileChannel.MapMode.READ_WRITE, 0, length);  
		//写128M的内容  
		for (int i = 0; i < length; i++) {  
			out.put((byte) 'x');  
		}  
		System.out.println("Finished writing"); 

		fc.close();  
	}  



	//多线程写文件——可以看成是按块下载
	public static void FileWriteMulThread(String fileName,int nThread,int [] nSkip,String[] content) throws Exception{
		// 预分配文件所占的磁盘空间，磁盘中会创建一个指定大小的文件  
		RandomAccessFile raf = new RandomAccessFile(fileName, "rw");  
		raf.setLength(1*M); // 预分配 1M 的文件空间  
		raf.close();  		
		// 利用多线程同时写入一个文件  
		for(int i=0;i<nThread;i++){	
			new FileWriteThread(fileName,nSkip[i],content[i].getBytes()).start(); // 从文件的1024字节之后开始写入数据  

		}	 
	}  

	// 利用线程在文件的指定位置写入指定数据  
	static class FileWriteThread extends Thread{  
		private int skip;  
		private byte[] content;  
		private String fileName;
		public FileWriteThread(String fileName,int skip,byte[] content){  
			this.skip = skip;  
			this.content = content;  
		}  

		public void run(){  
			RandomAccessFile raf = null;  
			try {  
				raf = new RandomAccessFile(fileName, "rw");  
				raf.seek(skip);  
				raf.write(content);  
			} catch (FileNotFoundException e) {  
				e.printStackTrace();  
			} catch (IOException e) { 				
				e.printStackTrace();  
			} finally {  
				try {  
					raf.close();  
				} catch (Exception e) {  
				}  
			}  
		}  

	}

	static class FileReadThread extends Thread{
		private int skip;
		private int length;
		private String fileName;
		public FileReadThread(String fileName,int skip,int length) {
			this.skip=skip;
			this.fileName=fileName;
			this.length=length;
		}
		public void run(){  
			RandomAccessFile raf = null;  
			byte[] readTemp=new byte[length];
			try {  
				raf = new RandomAccessFile(fileName, "rw");  
				raf.seek(skip); 
				raf.read(readTemp);      
			} catch (FileNotFoundException e) {  
				e.printStackTrace();  
			} catch (IOException e) {  				
				e.printStackTrace();  
			} finally {  
				try {  
					raf.close();  
				} catch (Exception e) {  
				}  
			}  
		}  

	}


}

