package test;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import findError.FindErrorBlock;
import tool.GenerateRandom;
import tool.SortAlg;
import tool.StdOut;

public class TestCube {
	public static final int RANDOM=2;
	public static final int SEQUENTRANDOM=0;
	public static final int MEANRANDOM=1;
	public static final int MATRIX=1;
	public static final int CUBE=2;
	public static final int AVERAGE=1;	
	public static final int SEEK=17;

	public static void testOneRound(int method,int[]chal,int errorCount,int DI){

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
			
			Set<Integer> errorSet=new HashSet<Integer>();		
			Set<Integer> remainSet=new HashSet<Integer>();
			Set<Integer> goodSet=new HashSet<Integer>();
			for(int i:chal)
				remainSet.add(i);
			
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
			System.out.println(errorCount+"\t"+errorSet.size()
					+"\t"+goodSet.size()
					+"\t"+remainSet.size());

		}
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
				testOneRound(CUBE,chal,errorCount,RANDOM);
			}
			StdOut.println();


		}
	}
}
