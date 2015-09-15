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
 * һ����ս
 * @author MichaelSun
 * @version 2.0
 * @date  2015.6.5
 * 
 */
public class FindErrorOneChallenge {

	public static final String PATH="d:/test/FindErrorOneChallenge.txt";
	public static void main(String[] args) throws IOException {
		PublicInfor pubInfor=new PublicInfor();	
		Verifier verifier=new Verifier();
		CloudServiceProvider csp=new CloudServiceProvider();
		TraceInfor time=new TraceInfor();
		int n=pubInfor.n;//�ܿ���
		int c=100;//��ս����
		int errorCounts=10;
		String method="Cube";
		//�ƻ����ݿ�
		int [] damagedBlocks=GenerateRandom.random(1, n, errorCounts);		
		FileOperation.destoryBlocks(pubInfor.filePath,pubInfor.blockSizeK, damagedBlocks);

		List<Chal>challenges=verifier.challengeGen(c, n);

		//��������������֤��
		TraceInfor.start();
		Map<String,Map<String,Element>> proofs=csp.genProof(method,challenges);
		TraceInfor.csptime+=TraceInfor.end();

		//��֤֤��
		TraceInfor.start();
		Map<String,Boolean>verResults=verifier.proofVerify(method, challenges, proofs);
		TraceInfor.vertime+=TraceInfor.end();

		//�ж��𻵿�
		TraceInfor.start();
		Map<String,Set<Integer>> blockStates=verifier.assertDamagedBlocks(method, challenges, verResults);
		TraceInfor.vertime+=TraceInfor.end();

		TraceInfor.goodBlocks=blockStates.get("goodBlocks").size();
		TraceInfor.errorBlocks=blockStates.get("errorBlocks").size();
		TraceInfor.remainBlocks=blockStates.get("remainBlocks").size();
		TraceInfor.logging(PATH);
		//�ָ����ݿ�
		FileOperation.repairBlocks(pubInfor.filePath,pubInfor.blockSizeK, damagedBlocks);
	}



}
