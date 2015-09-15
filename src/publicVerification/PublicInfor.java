package publicVerification;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import tool.PropertiesUtil;
import db.DBOperation;
import file.FileOperation;

public class PublicInfor {
	public static final String FILECONFIG="fileconfig.properties";
	public static final String PUBLIC="public";
	public static final int K=1024;
	public static final boolean USEPBC=false;
	public static final String CURVEPATH="pairing/d/d_159.properties";
	public Pairing pairing;
	public  Element g1;
	public  Element g2;	
	public  int sectors;
	public  int sectorSize;
	public  int blockSize;//块字节数
	public  int blockSizeK;
	public  int n;
	public  Element []us;
	public  Element v;
	public  String filePath;//用户文件存放路径，也是存放在服务器中的文件路径
	
	public PublicInfor(){
		init();
	}
	public void init(){
		try {
			Properties pro=PropertiesUtil.loadProperties();
			filePath=pro.getProperty("filePath");
			sectors=Integer.valueOf(pro.getProperty("sectors"));
			sectorSize=Integer.valueOf(pro.getProperty("sectorSize"));			
			blockSize=(sectors*sectorSize/1000)*K;
			blockSizeK=blockSize/K;
			n=FileOperation.blockNumbers(filePath, blockSizeK);
			//校验算法全局公开信息
			PairingFactory.getInstance().setUsePBCWhenPossible(USEPBC);
			pairing=PairingFactory.getPairing(CURVEPATH);
			Map<String, byte[]> pubInfor=getPublicInfor(PUBLIC);
			g1=pairing.getG1().newElementFromBytes(pubInfor.get("g1"));
			g2=pairing.getG2().newElementFromBytes(pubInfor.get("g2"));
			v=pairing.getG2().newElementFromBytes(pubInfor.get("v"));			
			us=new Element[sectors];
			for (int i=0;i<sectors;i++){
				us[i]=pairing.getG1().newElementFromBytes(pubInfor.get("us"+String.valueOf(i+1)));
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}
	
		//获取公开校验参数信息
		public static Map<String ,byte[]> getPublicInfor(String table){
			DBOperation dbo=new DBOperation();
			return dbo.selectBatch(table, "");
			
		}
		
}
