package test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import findError.FindErrorBlock;
import tool.Accumulator;
import tool.DataFilter;
import tool.GenerateRandom;
import tool.SortAlg;
import tool.StdOut;

public class TestFindErrorMoreRounds {	
	public static final int RANDOM=2;
	public static final int SEQUENTRANDOM=0;
	public static final int MEANRANDOM=1;
	public static final int MATRIX=1;
	public static final int CUBE=2;
	public static final int AVERAGE=1;	
	public static final int SEEK=17;

	/**
	 * 多轮校验，每轮校验数据量无重复选取
	 * @param method
	 * @param all
	 * @param chalLen
	 * @param errors
	 * @param isOne
	 */
	public static void testMoreRound(int method,int[] all,int chalLen,int[]errors,boolean isOne){
		Accumulator goodAcc=new Accumulator();
		Accumulator errorAcc=new Accumulator();
		Accumulator remainAcc=new Accumulator();			
		for(int j=0;j<AVERAGE;j++){	
			int rounds=all.length/chalLen;
			rounds+=(all.length%chalLen)==0?0:1;

			Set<Integer> errorBlocks=new HashSet<Integer>();		
			Set<Integer> remainBlocks=new HashSet<Integer>();
			Set<Integer> goodBlocks=new HashSet<Integer>();

			for(int k=0;k<rounds;k++){
				//一轮的检测-无重复
				int from=k*chalLen;
				int to=(k+1)*chalLen>all.length?all.length:(k+1)*chalLen;
				int chal[]=Arrays.copyOfRange(all, from, to);
				Set<Integer> errorSet=new HashSet<Integer>();		
				Set<Integer> remainSet=new HashSet<Integer>();
				Set<Integer> goodSet=new HashSet<Integer>();
				for(int i:chal)
					remainSet.add(i);
				while(true)			
				{
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
				goodBlocks.addAll(goodSet);
				remainBlocks.addAll(remainSet);
				errorBlocks.addAll(errorSet);

			}
			goodAcc.addDataValue(goodBlocks.size());
			remainAcc.addDataValue(remainBlocks.size());
			errorAcc.addDataValue(errorBlocks.size());
		}
		//StdOut.print(DataFilter.roundDouble(chalCounts.mean(),0));
		StdOut.print("\t"+DataFilter.roundDouble(goodAcc.mean(),0));
		StdOut.print("\t"+DataFilter.roundDouble(remainAcc.mean(),0));
		StdOut.print("\t"+DataFilter.roundDouble(errorAcc.mean(),0));
		StdOut.println();		

	}
	//检测出损坏块p，需要轮数
	public static void testMoreRound(int method,int[] all,int chalLen,int[]errors,boolean isOne,double p){
		Accumulator roundsAcc=new Accumulator();		
		for(int j=0;j<AVERAGE;j++){	
			Set<Integer> errorBlocks=new HashSet<Integer>();		
			Set<Integer> remainBlocks=new HashSet<Integer>();
			Set<Integer> goodBlocks=new HashSet<Integer>();

			int findErrors=0;
			int rounds=0;
			while((int)errors.length*p>findErrors){			
				rounds++;
				//在总块中排出已确定的损坏块后，再随机抽取chalLen个块
				int chal[]=GenerateRandom.random(all,chalLen,errorBlocks);

				//一轮校验结果
				Set<Integer> errorSet=new HashSet<Integer>();		
				Set<Integer> remainSet=new HashSet<Integer>();
				Set<Integer> goodSet=new HashSet<Integer>();
				for(int i:chal)
					remainSet.add(i);
				while(true)			
				{
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
					if(nextchal.size()==remainSet.size()){//本轮挑战中没有找到任何的坏块信息	
						if (isOne) {	
							result=feb.findErrorByOneBlock(nextchal, errors);
							remainSet.clear();
							errorSet.addAll(result.get("errorBlocks"));
							goodSet.addAll(result.get("goodBlocks"));						

						}	
						break;
					}
				}
				goodBlocks.addAll(goodSet);
				remainBlocks.addAll(remainSet);
				errorBlocks.addAll(errorSet);
				findErrors+=errorSet.size();

			}
			roundsAcc.addDataValue(rounds);
		}
		System.out.println(DataFilter.roundDouble(roundsAcc.mean(),0));
	}

	//指定轮数下的损坏数据块的发现率
	public static void testMoreRound(int method,int[] all,int chalLen,int[]errors,boolean isOne,int rounds){
			Set<Integer> errorBlocks=new HashSet<Integer>();		
			Set<Integer> remainBlocks=new HashSet<Integer>();
			Set<Integer> goodBlocks=new HashSet<Integer>();						
			while(rounds>0){			
				//在总块中排出已确定的损坏块后，再随机抽取chalLen个块
				int chal[]=GenerateRandom.random(all,chalLen,errorBlocks);

				//一轮校验结果
				Set<Integer> errorSet=new HashSet<Integer>();		
				Set<Integer> remainSet=new HashSet<Integer>();
				Set<Integer> goodSet=new HashSet<Integer>();
				for(int i:chal)
					remainSet.add(i);
				while(true)			
				{
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
					if(nextchal.size()==remainSet.size()){//本轮挑战中没有找到任何的坏块信息	
						if (isOne) {	
							result=feb.findErrorByOneBlock(nextchal, errors);
							remainSet.clear();
							errorSet.addAll(result.get("errorBlocks"));
							goodSet.addAll(result.get("goodBlocks"));						
						}	
						break;
					}
				}
				goodBlocks.addAll(goodSet);
				remainBlocks.addAll(remainSet);
				errorBlocks.addAll(errorSet);					
				rounds--;
				
			}
			
		
		System.out.println(DataFilter.roundDouble(errorBlocks.size()*100/errors.length,2));
	}

	public static void main(String[] args){	
		int allBlocks=10000;
		double [] errorPor={0.1,0.5,1};			
		long seek=System.currentTimeMillis();			
		int [] all=GenerateRandom.random(1, allBlocks*10,allBlocks,seek);
		int []c={100,200,300,400,500,600,700,800,900,1000};
		int []rounds={10,20,30,40,50,60,70,80,90,100};
		for(int i=0;i<errorPor.length ;i++){
			int errorCount=(int)Math.ceil(allBlocks*errorPor[i]/100);
			int []errors=GenerateRandom.random(all,errorCount,seek);
			SortAlg.sort(errors, 0, errors.length-1);
			for(int j=0;j<c.length;j++){								
				testMoreRound(CUBE, all, c[j], errors,true,0.8);
			}
			
			//多轮的坏块探测率
			/*for(int j=0;j<rounds.length;j++){								
				testMoreRound(CUBE, all, 1000, errors,true,rounds[j]);
			}*/
			System.out.println();
		}
	}



}
