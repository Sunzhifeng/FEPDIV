package test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import findError.FindErrorBlock;
import tool.Accumulator;
import tool.DataFilter;
import tool.GenerateRandom;
import tool.SortAlg;
import tool.StdOut;

public class TestFindErrorBlocks {
	public static final int n=2; //2B,10000块
	public static final int p=20;//160bit;
	public static final int K=1000;
	public static double errorFindTransCost(double challengeCount,double challengeBlocks){
		double challenge=(challengeBlocks*(n+p)+challengeCount*2*p);
		double proof=3*challengeCount*p;
		return DataFilter.roundDouble((challenge+proof)/K,2);
	}
	public static double errorFindTransCostOne(double c){
		double challenge=c*(n+3*p);
		double proof=3*c*p;
		return DataFilter.roundDouble((challenge+proof)/K, 3);
	}
	public static void main(String[] args){	

		int allBlocks=10000;		
		double [] errorPor={0.1,0.5,1,2};
		//int []errorCount=new int[40];
		//int []errorCount={1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49};
		//int []cCount={20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100,105,110,115};
	
		int []cCount={1000};	
		
		for(int k=0;k<cCount.length;k++){			
			//随机产生1000个挑战块
			int [] chal=GenerateRandom.random(1, allBlocks,cCount[k],1);
		
			for(int i=0;i<20;i++){
				//损坏块的数量
				int errorCount=(int)(1000*errorPor[i]/100);	
				
				//平均多次计算挑战数和挑战块数
				int average=20;
				Accumulator accChalCount=new Accumulator();
				Accumulator accChalBlocks=new Accumulator();
				Accumulator goodBlocks=new Accumulator();
				Accumulator remainBlocks=new Accumulator();
				for(int j=0;j<average;j++){
					//错误块平均分配DI=1
					//int [] error=GenerateRandom.meanRandom(chal,errorCount);
					
					//错误块随机分配——实际的情况DI=1/2
					//int [] error=GenerateRandom.random(chal,errorCount);
					
					//错误块连续分配DI=0
					int [] error=GenerateRandom.sequentRandom(chal, errorCount,1);	
					SortAlg.sort(error, 0, error.length-1);
					FindErrorBlock feb=new FindErrorBlock();
					
					//Map<String,Object>binary=feb.findErrorByBinary(chal, error);
					Map<String,Set<Integer>>matrix=feb.findErrorByMatrix(chal, error);
					//Map<String,Object>cube=feb.findErrorByCube(chal, error);
					
					//accChalCount.addDataValue((int)matrix.get("chalCount"));					
					//accChalBlocks.addDataValue((int)matrix.get("chalBlocks"));
					//goodBlocks.addDataValue(((Set<Integer>)matrix.get("goodBlocks")).size());
					//remainBlocks.addDataValue(((Set<Integer>)matrix.get("remainBlocks")).size());
					 
					/*Map<String,Integer>binaryOnce=feb.findErrorByBinaryOnce(chal, error);
					goodBlocks.addDataValue((int)binaryOnce.get("goodBlocks"));					
					remainBlocks.addDataValue((int)binaryOnce.get("remainBlocks"));*/
				}				
				StdOut.println(DataFilter.roundDouble(accChalCount.mean(),0)
								+"\t"+DataFilter.roundDouble(accChalBlocks.mean(),0)
								+"\t"+errorFindTransCost(accChalCount.mean(),accChalBlocks.mean()));
				
				//StdOut.println(errorPor[i]+"\t"+DataFilter.roundDouble(goodBlocks.mean(),0)
				//		+"\t"+DataFilter.roundDouble(remainBlocks.mean(),0)
				//		+"\t"+errorCount);
								
				//StdOut.println(DataFilter.roundDouble(accChalCount.mean(),0));
				//StdOut.println(DataFilter.roundDouble(accChalBlocks.mean(),0));
				//StdOut.println(errorFindTransCostOne(cCount[k]));
				//StdOut.println(errorFindTransCost(accChalCount.mean(),accChalBlocks.mean()));
				
			}
			StdOut.println();
			

		}
	}
}

