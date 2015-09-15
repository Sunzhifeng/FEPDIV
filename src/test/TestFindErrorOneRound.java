package test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import findError.FindErrorBlock;
import tool.Accumulator;
import tool.DataFilter;
import tool.GenerateRandom;
import tool.SortAlg;
import tool.StdOut;

public class TestFindErrorOneRound {
	public static final int RANDOM=2;
	public static final int SEQUENTRANDOM=0;
	public static final int MEANRANDOM=1;
	public static final int MATRIX=1;
	public static final int CUBE=2;
	public static final int AVERAGE=100;	
	public static final int SEEK=17;


	//一轮校验
	//isOne 最后是否使用逐块探测所有坏块
	public static void testOneRound(int method,int[]chal,int errorCount,int DI,boolean isOne){
		Accumulator goodBlocks=new Accumulator();
		Accumulator errorBlocks=new Accumulator();
		Accumulator remainBlocks=new Accumulator();	
		Accumulator chalCounts=new Accumulator();	
		for(int j=0;j<AVERAGE;j++){
			int []errors;
			long seek=17;
			if(DI==0)
				errors=GenerateRandom.sequentRandom(chal, errorCount,seek);

			else if(DI==1)
				errors=GenerateRandom.meanRandom(chal,errorCount,seek);
			else
				errors=GenerateRandom.random(chal,errorCount,seek);
			SortAlg.sort(errors, 0, errors.length-1);
			int chalCount=0;
			Set<Integer> errorSet=new HashSet<Integer>();		
			Set<Integer> remainSet=new HashSet<Integer>();
			Set<Integer> goodSet=new HashSet<Integer>();
			for(int i:chal)
				remainSet.add(i);
			while(true)			
			{
				chalCount++;
				Set<Integer> nextchal=remainSet;
				FindErrorBlock feb=new FindErrorBlock();
				Map<String,Set<Integer>>result=null;
				if(method==MATRIX){
					result=feb.findErrorByMatrix(nextchal, errors);
				}else {
					result=feb.findErrorByCube(nextchal, errors);

				}
				remainSet=result.get("remainBlocks");
				errorSet.addAll(result.get("errorBlocks"));
				goodSet.addAll(result.get("goodBlocks"));

				if(nextchal.size()==remainSet.size()){	
					 if (isOne) {	
						result=feb.findErrorByOneBlock(nextchal, errors);
						remainSet.clear();
						errorSet.addAll(result.get("errorBlocks"));
	                    goodSet.addAll(result.get("goodBlocks"));						
					
					 }	
					 break;
				}
			}
			goodBlocks.addDataValue(goodSet.size());
			remainBlocks.addDataValue(remainSet.size());
			errorBlocks.addDataValue(errorSet.size());
			chalCounts.addDataValue(chalCount);
		}
		StdOut.print(DataFilter.roundDouble(chalCounts.mean(),0));
		StdOut.print("\t"+DataFilter.roundDouble(goodBlocks.mean(),0));		
		StdOut.print("\t"+DataFilter.roundDouble(errorBlocks.mean(),0));
		StdOut.print("\t"+DataFilter.roundDouble(remainBlocks.mean(),0));
		StdOut.println();		
	}



	public static void main(String[] args){	
		int allBlocks=100000;		
		int []c={1000,2000,3000,4000,5000,6000,7000,8000,9000,10000};
		double [] errorPor={0.1,0.5,1};	
		for(int j=0;j<errorPor.length;j++){
			for(int k=0;k<c.length;k++){						
				long seek=System.currentTimeMillis();	
				//long seek=17;
				int [] chal=GenerateRandom.random(1, allBlocks,c[k],seek);				
				int errorCount=(int)(c[k]*errorPor[j]/100);				
				testOneRound(MATRIX,chal,errorCount,MEANRANDOM,false);
			}
			StdOut.println();


		}
	}
}
