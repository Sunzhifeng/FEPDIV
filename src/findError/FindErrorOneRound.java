/**
 * 功能描述：
 * 1.采用非对称的双线性映射
 * 2.采用带签名保护的索引哈希表
 * 3.服务器对数据标签进行累成，由服务器计算，最小化校验者的计算量
 * 4.服务器端对数据标签累成的改进版本
 * 5.批量校验
 * 6.包含对信息盲化处理
 */

package findError;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;

import file.FileOperation;
import it.unisa.dia.gas.jpbc.*;
import publicVerification.CloudServiceProvider;
import publicVerification.PublicInfor;
import publicVerification.TraceInfor;
import publicVerification.Verifier;
import publicVerification.Verifier.Chal;
import tool.GenerateRandom;



/**
 * 一轮坏块检测
 * @author MichaelSun
 * @version 2.0
 * @date  2015.6.5
 * 
 */
public class FindErrorOneRound {
	public static final String PATH="d:/test/FindErrorOneRound.txt";
	public static final String MATRIX="Matrix";	
	public static final String CUBE="Cube";	
	public static final String ONE="One";
	public static void testOneRound(String method,int []cBlocks,boolean isOne){

		Verifier verifier=new Verifier();
		CloudServiceProvider csp=new CloudServiceProvider();		
		//int [] blocks=GenerateRandom.random(1, n, c);//随机选取c个块进行挑战	
		Set<Integer>goodBlocks=new HashSet<>();
		Set<Integer>errorBlocks=new HashSet<>();
		Set<Integer>remainBlocks=new HashSet<>();
		for(int i:cBlocks)
			remainBlocks.add(i);
		while(remainBlocks.size()!=0)
		{	
			
			//产生挑战
			List<Chal>nextChal=verifier.challengeGen(remainBlocks);
			TraceInfor.challenges++;
			
			//计算证据
			TraceInfor.start();
			Map<String,Map<String,Element>> proofs=csp.genProof(method,nextChal);
			TraceInfor.csptime+=TraceInfor.end();
			TraceInfor.transCost+=proofs.size()*40;
			
			//验证证据
			TraceInfor.start();
			Map<String,Boolean>verResults=verifier.proofVerify(method, nextChal, proofs);
			TraceInfor.vertime+=TraceInfor.end();

			//判定算坏块
			TraceInfor.start();
			Map<String,Set<Integer>> blockStates=verifier.assertDamagedBlocks(method, nextChal, verResults);
			TraceInfor.asserttime+=TraceInfor.end();

			goodBlocks.addAll(blockStates.get("goodBlocks"));
			errorBlocks.addAll(blockStates.get("errorBlocks"));
			remainBlocks=blockStates.get("remainBlocks");
			if(nextChal.size()==remainBlocks.size()){
				System.out.println(nextChal.size());
				if(isOne)
					method=ONE;//下次挑战对剩余块采用One方法
				else 
					break;
			}
		}
		TraceInfor.errorBlocks=errorBlocks.size();
		TraceInfor.goodBlocks=goodBlocks.size();
		TraceInfor.remainBlocks=remainBlocks.size();
		
		TraceInfor.logging(PATH);
		
	}

	public static void main(String[] args) throws IOException {
		PublicInfor pubInfor=new PublicInfor();	
		int n=pubInfor.n;
		int[] c={500,1000,1500,2000,2500,3000,3500,4000,4500,5000};
		double []w={0.2,0.5,1};
		for(int i=0;i<1;i++)
		{
			for(int j=0;j<1;j++){
				int errorCounts=(int)(w[i]*c[j]/100);
				int []cBlocks=GenerateRandom.random(1, n, c[j]);//一轮中抽取的块数
				int [] damagedBlocks=GenerateRandom.random(cBlocks, errorCounts);//抽取的块数中损坏的块数		
				FileOperation.destoryBlocks(pubInfor.filePath,pubInfor.blockSizeK, damagedBlocks);
				TraceInfor.allErrorBlocks=errorCounts;
				TraceInfor.round=1;
				TraceInfor.logging(PATH,"-----------------------------------");
				TraceInfor.logging(PATH,CUBE+"\tc:"+c[j]+"\tw:"+w[i]);
				TraceInfor.logging(PATH,"-----------------------------------");
				testOneRound(CUBE,cBlocks,true);

				FileOperation.repairBlocks(pubInfor.filePath,pubInfor.blockSizeK, damagedBlocks);
			}
		}
	}
}
