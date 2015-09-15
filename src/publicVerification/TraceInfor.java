package publicVerification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import tool.DataFilter;
import tool.Stopwatch;

public class TraceInfor {   
	public static final int K=1024;
	//对时间的追踪
	public static double dotime=0;
	public static double csptime=0;
	public static double vertime=0;
	public static double asserttime=0;

	//对传输量的追踪
	public static double transCost=0;//字节

	//对能量消耗的追踪
	public static double energyCost=0;

	//对数据损坏情况的追踪
	public static int goodBlocks=0;
	public static int errorBlocks=0;
	public static int remainBlocks=0;
	public static int allErrorBlocks;
	//检测轮数
	public static int round=0;

	//挑战次数
	public static int challenges=0;
	//坏块检测率
	public static double p=0;

	public static Stopwatch stopwatch;	
	public static void start(){
		stopwatch=new Stopwatch();
	}
	public static double end(){
		return stopwatch.elapsedTime();
	}

	public static void logging(String Path){
			FileWriter fw;  
			BufferedWriter bf; 
			boolean append =false;  
			File file = new File(Path);  
			try{  
				if(file.exists()) append =true;  
				fw = new FileWriter(Path,append);//同时创建新文件  
				
				bf = new BufferedWriter(fw);  
				//时间
				bf.append("DO(s):"+DataFilter.roundDouble(dotime, 0)+"\t"+
						"VER(s):"+DataFilter.roundDouble(vertime, 0)+"\t"+
						"ASSERT(s):"+DataFilter.roundDouble(asserttime, 0)+"\t"+
						"CSP(s):"+DataFilter.roundDouble(csptime, 0));
				bf.append("\r\n");
				//块状态
				bf.append("GB:"+goodBlocks+"\t"+
						"EB:"+errorBlocks+"\t"+
						"RB:"+remainBlocks);
				bf.append("\r\n");
				//传输消耗
				bf.append("TransCost: "+DataFilter.roundDouble(transCost/K, 0));
				bf.append("\r\n");
				//轮数
				bf.append("rounds:"+round+"\t"+"Challenges:"+challenges);
				bf.append("\r\n");
				//坏块检测率
				bf.append("p:"+DataFilter.roundDouble((allErrorBlocks!=0?errorBlocks*100/allErrorBlocks:0.0), 2));
				bf.append("\r\n");
				bf.append("\r\n");
				bf.flush();  
				bf.close();    
			}catch (IOException e){  
				e.printStackTrace();  
			}  
		
			
	}
	public static void logging(String Path,String s){
		FileWriter fw;  
		BufferedWriter bf; 
		boolean append =false;  
		File file = new File(Path);  
		try{  
			if(file.exists()) append =true;  
			fw = new FileWriter(Path,append);//同时创建新文件  
			//创建字符输出流对象  
			bf = new BufferedWriter(fw);  
			//创建缓冲字符输出流对象  
			bf.append(s);  
			bf.append("\r\n");
			bf.flush();  
			bf.close();    
		}catch (IOException e){  
			e.printStackTrace();  
		}  

	}




}
