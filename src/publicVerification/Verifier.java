package publicVerification;

import it.unisa.dia.gas.jpbc.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import findError.FindErrorBlock;
import tool.GenerateRandom;
import tool.SortAlg;

public class Verifier {

	public PublicInfor pubInfor;
	public Verifier(){
		pubInfor=new PublicInfor();
	}
	/**
	 * 挑战信息的内部类
	 */
	public  static class Chal implements Comparable<Chal>{
		public int num; //块的逻辑编号
		public Element random;//相应的随机数
		public Chal(int num,Element random){
			this.num=num;
			this.random=random;
		}
		public int compareTo(Chal o) {
			// TODO Auto-generated method stub
			return num-o.num;
		}
		

	}
	/**
	 * 校验者生成挑战信息
	 * @param c   			校验块数
	 * @param allBlocks   	全部块数
	 * @return				挑战信息chal
	 */
	public List<Chal> challengeGen(int c,int allBlocks){
		int []ran=new int[c];
		ran=GenerateRandom.random(1,allBlocks,c); //1-allBlocks中的c个不同的数
		SortAlg.sort(ran, 0, c-1);
		List<Chal>challenges=new ArrayList<>(c);		
		//生成每块对应的随机数vi
		for(int i=0;i<c;i++){			
			challenges.add(new Chal(ran[i],pubInfor.pairing.getZr().newRandomElement()));
		}
		return challenges;
	}

	public Chal[]challengeGen(int[]blocks){
		int len=blocks.length;
		GenerateRandom.shuffle(blocks);
		Chal[] challenges=new Chal[len];
		for(int j=0;j<len;j++)	
			challenges[j++]=new Chal(blocks[j],pubInfor.pairing.getZr().newRandomElement());
		return challenges;
	}
	
	public List<Chal> challengeGen(Set<Integer>blocks){
		int len=blocks.size();		
		List<Chal> challenges=new ArrayList<>(len);
		for(int b:blocks)	
			challenges.add(new Chal(b,pubInfor.pairing.getZr().newRandomElement()));
		
		return challenges;
	}

	/**
	 * 根据CSP发来的Proof,校验者验证Proof是否正确	
	 * @param v 公钥	
	 * @return  true或false
	 */
	public boolean proofVerify(List<Chal>challenges,Map<String,Element> proof){
		Element aggreTMul=proof.get("aggreTMul");
		Element aggreDMul=proof.get("aggreDMul");		
		int c=challenges.size();		
		Element aggreBlock=pubInfor.pairing.getG1().newOneElement();
		for(int i=0;i<c;i++){
			byte[] data=String.valueOf(challenges.get(i).num).getBytes();
			Element Hid=pubInfor.pairing.getG1().newElementFromHash(data,0,data.length);
			Element tmp=Hid.duplicate().powZn(challenges.get(i).random);
			aggreBlock=aggreBlock.mul(tmp);
		}	
		//e(Hchal,v)	
		Element temp1 =pubInfor.pairing.pairing(aggreBlock,pubInfor.v);	
		//e(Tp,g2)
		Element temp2 = pubInfor.pairing.pairing(aggreTMul, pubInfor.g2);
		return (aggreDMul.mul(temp1)).equals(temp2)? true :false;
	}
	//损坏块判定验证结果
	public Map<String,Boolean> proofVerify(String method ,List<Chal>challenges,Map<String,Map<String,Element>>proofs){
		
		Map<String,Boolean> results=new HashMap<>();
		if("One".equals(method))
		{//逐块验证			
			for(Chal c:challenges)			
			{
				List<Chal>one=new ArrayList<Chal>(1);
				one.add(c);
				String num=String.valueOf(c.num);
				boolean result=proofVerify(one, proofs.get(num));
				results.put(num, result);
			}
		}
		else if("Matrix".equals(method))
		{
			int len=challenges.size();
			Map<String,Integer>indexAB=FindErrorBlock.getMatrixIndex(len);
			int row=indexAB.get("row");
			int col=indexAB.get("col");	

			//行验证结果
			for(int r=0;r<row;r++){
				List<Chal> rchal=new ArrayList<>();
				for(int c=0;c<col;c++){
					int index=r*col+c;
					if(index<len)
						rchal.add(challenges.get(index));
				}
				boolean result=proofVerify(rchal,proofs.get("r"+(r+1)));
				//System.out.println(result);
				results.put("r"+(r+1), (result));
			}

			//列验证结果
			for(int c=0;c<col;c++){
				List<Chal> cchal=new ArrayList<>();
				for(int r=0;r<row;r++){
					int index=r*col+c;
					if(index<len)
						cchal.add(challenges.get(index));
				}
				boolean result=proofVerify(cchal,proofs.get("c"+(c+1)));
				//System.out.println(result);
				results.put("c"+(c+1), (result));
			}
		}
		else if("Cube".equals(method))
		{
			int len=challenges.size();	
			Map<String,Integer> abc=FindErrorBlock.getCubeIndex(len);
			int a=abc.get("a");
			int b=abc.get("b");
			int c=abc.get("c");

			//int [][][]V=new int [c][b][a];

			//检查X维度证据						
			for(int i=0;i<c;i++){		
				for(int j=0;j<b;j++){
					//int [][]X=new int[c][b];
					List<Chal> xchal=new ArrayList<>();
					for(int k=0;k<a;k++){
						int index=i*a*b+j*a+k;
						if(index<len){
							xchal.add(challenges.get(index));
						}
					}
					boolean result=proofVerify(xchal,proofs.get("x"+(i+1)+(j+1)));
					results.put("x"+(i+1)+(j+1),(result));
				}

			}
			//检查Y维度证据
			//Map<String,Map<String,Element>> yproof=new HashMap<>();
			for(int i=0;i<c;i++){
				for(int k=0;k<a;k++){	
					//int [][]Y=new int[c][a];
					List<Chal> ychal=new ArrayList<>();
					for(int j=0;j<b;j++){
						int index=i*a*b+j*a+k;
						if(index<len){
							ychal.add(challenges.get(index));
						}
					}
					boolean result=proofVerify(ychal,proofs.get("y"+(i+1)+(k+1)));
					results.put("y"+(i+1)+(k+1),(result));
				}

			}

			//检查Z维度证据			
			for(int j=0;j<b;j++){			
				for(int k=0;k<a;k++){	
					//int [][]Z=new int[b][a];
					List<Chal> zchal=new ArrayList<>();
					for(int i=0;i<c;i++){
						int index=i*a*b+j*a+k;
						if(index<len){
							zchal.add(challenges.get(index));
						}
					}
					boolean result=proofVerify(zchal,proofs.get("z"+(j+1)+(k+1)));
					results.put("z"+(j+1)+(k+1),(result));
				}
			}
		}
		else
		{

		}

		return results;
	}
	public Map<String,Set<Integer>> assertDamagedBlocks(String method,List<Chal>challenges,Map<String,Boolean>verResults)
	{
		int length=challenges.size();
		int [] blockNums=new int [length];
		for(int i=0;i<length;i++){
			blockNums[i]=challenges.get(i).num;
		}
		return assertDamagedBlocks(method, blockNums, verResults);
	}
	//判定损坏块
	public Map<String,Set<Integer>> assertDamagedBlocks(String method,int[]blockNums,Map<String,Boolean>verResults){
		Map<String,Set<Integer>> results=new HashMap<>();
		if("One".equals(method))
		{//逐块验证
			Set<Integer> errorBlocks=new HashSet<>();
			Set<Integer> goodBlocks=new HashSet<>();
			Set<Integer> remainBlocks=new HashSet<>();
			for(int i:blockNums)
			{
				//System.out.println(i);
				boolean verR=verResults.get(String.valueOf(i));	
				if(!verR)
					errorBlocks.add(i);
				else
					goodBlocks.add(i);
			}
			results.put("errorBlocks", errorBlocks);
			results.put("goodBlocks", goodBlocks);
			results.put("remainBlocks", remainBlocks);
		}
		//矩阵验证
		else if("Matrix".equals(method))
		{
			results=matrixAssert(blockNums,verResults);
		}
		//立方验证
		else if("Cube".equals(method))
		{
			results=cubeAssert(blockNums,verResults);
		}
		//其他方法
		else
		{
			System.out.println("other unimplements method！");
		}
		return results;
	}

	public  Map<String,Set<Integer>> matrixAssert(int[]blockNums,Map<String,Boolean>verResults){
		int len=blockNums.length;
		//行列索引
		Map<String,Integer>indexAB=FindErrorBlock.getMatrixIndex(len);
		int a=indexAB.get("row");
		int b=indexAB.get("col");

		//行列验证结果
		List<Boolean>row=new ArrayList<>();
		List<Boolean>col=new ArrayList<>();			
		for(int i=0;i<a;i++){
			row.add(verResults.get("r"+((i+1))));
		}
		for(int j=0;j<b;j++){
			col.add(verResults.get("c"+(j+1)));
		}

		Set<Integer> errorBlocks=new HashSet<Integer>();
		Set<Integer> goodBlocks=new HashSet<Integer>();
		Set<Integer>remainBlocks=new HashSet<Integer>();

		//查找好块
		for(int i=0;i<len;i++)
		{
			boolean rResult=row.get(i/b);
			boolean cResult=col.get(i%b);
			if(rResult||cResult)//某行或某列验证通过
			{
				goodBlocks.add(blockNums[i]);
				blockNums[i]=0;
			}			
		}		
		//查找坏块及未确定的数据块
		for(int i=0;i<len;i++)
		{
			boolean rResult=row.get(i/b);
			boolean cResult=col.get(i%b);
			int num=blockNums[i];
			if(num!=0)
			{
				boolean isUnique=false;
				int rowUnique=0,colUnique=0,index;
				//检查第i块所在的行:(i/b)*b+0->(i/b)*b+(b-1)
				for(int j=0;j<b;j++)
				{
					index=(i/b)*b+j;
					if(index<len)
						rowUnique^=blockNums[index];
				}
				//检查第i块所在列:0*b+i%b->(a-1)*b+i%b
				for(int k=0;k<a;k++)
				{
					index=k*b+i%b;
					if(index<len)
						colUnique^=blockNums[index];
				}
				if(rowUnique==blockNums[i]||colUnique==blockNums[i])
					isUnique=true;

				if((!rResult&&!cResult)&&isUnique)//行与列验证都false且唯一未确定块
					errorBlocks.add(num);		
				else
					remainBlocks.add(num);
			}

		}
		Map<String,Set<Integer>> result=new HashMap<>(3);
		result.put("errorBlocks", errorBlocks);
		result.put("goodBlocks", goodBlocks);		
		result.put("remainBlocks", remainBlocks);
		return result;		
	}

	public  Map<String,Set<Integer>> cubeAssert(int[]blockNums,Map<String,Boolean>verResults)
	{
		Map<String,Set<Integer>> results=new HashMap<>();
		//立方体索引
		int len=blockNums.length;	
		Map<String,Integer> abc=FindErrorBlock.getCubeIndex(len);
		int a=abc.get("a");
		int b=abc.get("b");
		int c=abc.get("c");

		//构建挑战立方体
		int [][][]V=new int [c][b][a];
		int index=0;
		for(int i=0;i<c;i++)
		{		
			for(int j=0;j<b;j++)
			{
				for(int k=0;k<a;k++)
				{
					if (index<blockNums.length)					
						V[i][j][k]=blockNums[index++];					
					else
						V[i][j][k]=0;//采用‘0’填充
				}
			}
		}

		Set<Integer> errorBlocks=new HashSet<Integer>();
		Set<Integer> goodBlocks=new HashSet<Integer>();
		Set<Integer>remainBlocks=new HashSet<Integer>();

		//判断好块
		for(int i=0;i<c;i++)
		{		
			for(int j=0;j<b;j++)
			{
				for(int k=0;k<a;k++)
				{
					int blockNum=V[i][j][k];
					boolean x=verResults.get("x"+(i+1)+(j+1));
					boolean y=verResults.get("y"+(i+1)+(k+1));
					boolean z=verResults.get("z"+(j+1)+(k+1));
					if(blockNum!=0&&(x || y || z)){					
						goodBlocks.add(blockNum);//此块为好块
						V[i][j][k]=0;//标记好块

					}
				}
			}
		}

		//判断坏块、未确定的块
		for(int i=0;i<c;i++)
		{
			for(int j=0;j<b;j++)
			{
				for(int k=0;k<a;k++)
				{
					int blockNum=V[i][j][k];
					if (blockNum!=0){
						boolean x=verResults.get("x"+(i+1)+(j+1));
						boolean y=verResults.get("y"+(i+1)+(k+1));
						boolean z=verResults.get("z"+(j+1)+(k+1));

						//是否为某列唯一未标记的块
						boolean isUnique=false;
						int xunique=0,yunique=0,zunique=0;
						for(int l=0;l<a;l++)
							xunique^=V[i][j][l];
						for(int m=0;m<b;m++)
							yunique^=V[i][m][k];
						for(int n=0;n<c;n++)
							zunique^=V[n][j][k];
						if (xunique==blockNum || yunique==blockNum || zunique==blockNum)
							isUnique=true;
						if((!x || !y || !z)&&isUnique)
						{		
							errorBlocks.add(blockNum);

						}
						else if(!isUnique)
							remainBlocks.add(blockNum);	
						else
							System.out.println("Cube Error!");
					}
				}
			}
		}
		results.put("goodBlocks", goodBlocks);
		results.put("errorBlocks", errorBlocks);
		results.put("remainBlocks", remainBlocks);		
		return results;

	}
}

