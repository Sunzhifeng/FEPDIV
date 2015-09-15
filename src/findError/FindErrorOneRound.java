/**
 * ����������
 * 1.���÷ǶԳƵ�˫����ӳ��
 * 2.���ô�ǩ��������������ϣ��
 * 3.�����������ݱ�ǩ�����۳ɣ��ɷ��������㣬��С��У���ߵļ�����
 * 4.�������˶����ݱ�ǩ�۳ɵĸĽ��汾
 * 5.����У��
 * 6.��������Ϣä������
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
 * һ�ֻ�����
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
		//int [] blocks=GenerateRandom.random(1, n, c);//���ѡȡc���������ս	
		Set<Integer>goodBlocks=new HashSet<>();
		Set<Integer>errorBlocks=new HashSet<>();
		Set<Integer>remainBlocks=new HashSet<>();
		for(int i:cBlocks)
			remainBlocks.add(i);
		while(remainBlocks.size()!=0)
		{	
			
			//������ս
			List<Chal>nextChal=verifier.challengeGen(remainBlocks);
			TraceInfor.challenges++;
			
			//����֤��
			TraceInfor.start();
			Map<String,Map<String,Element>> proofs=csp.genProof(method,nextChal);
			TraceInfor.csptime+=TraceInfor.end();
			TraceInfor.transCost+=proofs.size()*40;
			
			//��֤֤��
			TraceInfor.start();
			Map<String,Boolean>verResults=verifier.proofVerify(method, nextChal, proofs);
			TraceInfor.vertime+=TraceInfor.end();

			//�ж��㻵��
			TraceInfor.start();
			Map<String,Set<Integer>> blockStates=verifier.assertDamagedBlocks(method, nextChal, verResults);
			TraceInfor.asserttime+=TraceInfor.end();

			goodBlocks.addAll(blockStates.get("goodBlocks"));
			errorBlocks.addAll(blockStates.get("errorBlocks"));
			remainBlocks=blockStates.get("remainBlocks");
			if(nextChal.size()==remainBlocks.size()){
				System.out.println(nextChal.size());
				if(isOne)
					method=ONE;//�´���ս��ʣ������One����
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
				int []cBlocks=GenerateRandom.random(1, n, c[j]);//һ���г�ȡ�Ŀ���
				int [] damagedBlocks=GenerateRandom.random(cBlocks, errorCounts);//��ȡ�Ŀ������𻵵Ŀ���		
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
