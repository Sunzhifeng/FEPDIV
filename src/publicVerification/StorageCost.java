package publicVerification;

import file.FileOperation;
import tool.DataFilter;
import tool.StdOut;

/**
 * 服务器中存储消耗：签名、索引表、文件元信息
 * @author MichaelSun
 *
 */
public class StorageCost {
	public static final int K=1024;
	public static final int Byte=8;
	/**
	 * CSP中除数据的额外存储消耗
	 * @param n		总块数
	 * @param s		每块段数
	 * @param p		大素数（20B）
	 * @return 		多少KB
	 */
	public static double IHTPADDStorageCost(int n,int s,int p){
		double usLength=s*p;
		double ihtLength=IHTStorageCost(n,p);
		double tagLength=n*p;
		double total=usLength+ihtLength+tagLength;
		return DataFilter.roundDouble(total/K,3);
	}
	/**
	 * CSP中额外存储消耗占数据的比例
	 * @param n		总块数
	 * @param s		每块段数
	 * @param p		大素数（20B）
	 * @return 		百分比
	 */
	public static double IHTPADDStorageCostRatio(int n,int s,int p){
		double additionStorageCost=IHTPADDStorageCost(n,s,p);
		double total=n*s*p/K;
		return DataFilter.roundDouble(additionStorageCost/total, 3)*100;
	}


	/**
	 * IHT表的存储消耗
	 * (index,B,V,R,Hmac)
	 * @param n		文件块数
	 * @param p		签名长度
	 * @return		索引表的字节数(B)
	 */
	public static double IHTStorageCost(int n,int p){		
		return n*(2*p+2*Math.log(n)/Byte);
	}
	
	public static double tagStorageCost(int n,int p){
		return n*p/Byte;
	}
	//返回消耗的KB
	public static double MHTStorgeCost(int n,int s,int p){
		//double result=n*Math.log(n)+3*p+s*p;
		double result=n*p+3*p+s*p;
		return DataFilter.roundDouble(result/K,2);
	}

	public static void main(String[] args) {

		//文件大小为2M
		//int M=1024*1024;
		
		int []files={2};
		int []s={50,100,150,200,250,300,350,400,450,500};				
		int p=20;//160bit

		for(int f=0;f<files.length;f++){
			StdOut.println("文件大小"+files[f]+"M");
			StdOut.println("========IHT存储消耗=========");
			for(int i=0;i<s.length;i++){
				int blockNums=FileOperation.blockNumbers(files[f]*K,s[i],p);
				StdOut.println(s[i]+"\t"+blockNums+"\t"+
								DataFilter.roundDouble(IHTStorageCost(blockNums,p)/K,3)+"\t"+
								DataFilter.roundDouble(tagStorageCost(blockNums,p)/K,3)+"\t"+
								DataFilter.roundDouble(s[i]*p/1000,3));
			}
			/*StdOut.println("========MHT存储消耗=========");
			for(int i=0;i<s.length;i++){
				int blockNums=fileBlocks(files[f]*M,s[i],p);
				StdOut.println(MHTStorgeCost(blockNums,s[i],p));
			}*/
		}

	}

}
