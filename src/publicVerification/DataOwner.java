package publicVerification;

import java.io.IOException;
import java.util.Properties;

import db.DBOperation;
import file.FileOperation;
import tool.PropertiesUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class DataOwner {
	public static void main(String[] args) throws IOException {
		PairingFactory.getInstance().setUsePBCWhenPossible(false);
		Pairing p = PairingFactory.getPairing(PublicInfor.CURVEPATH);
		DataOwner dataOwner = new DataOwner(p);

		// 配置文件处理参数
		Properties pro = PropertiesUtil.loadProperties();
		String filePath = pro.getProperty("filePath");
		int sectors = Integer.valueOf(pro.getProperty("sectors"));
		int sectorSize = Integer.valueOf(pro.getProperty("sectorSize"));
		int blockSize = sectors * sectorSize / 1000; // 块单位为K

		// 对文件进行预处理
		Element[][] nSectors = FileOperation.preProcessFile(filePath, sectors, sectorSize, p.getZr());
		int n = FileOperation.blockNumbers(filePath, blockSize);

		// 数据所有者操作
		dataOwner.setup(sectors);// 初始化校验参数
		dataOwner.keyGen(); // 生成公钥和私钥
		Element[] tags = dataOwner.metaGen(nSectors, n);// 计算块标签
		dataOwner.publicInfor();// 发布公开信息
		dataOwner.storeTag(tags);// 存储块标签

	}

	public static final String FILETAG = "filetag";// 存放文件块标签数据表filetag
	public static final String PUBLIC = "public"; // 存放公开信息的数据表public

	protected Pairing pairing;
	public Element g1;
	public Element g2;
	private Element[] ps;// 秘密值
	public Element[] us;
	private Element x;// 私钥
	public Element v;// 公钥

	public DataOwner(Pairing p) {
		this.pairing = p;
	}

	/**
	 * 初始化设置
	 * 
	 * @param
	 * @throws IOException
	 */
	public void setup(int s) {
		g1 = pairing.getG1().newRandomElement().getImmutable();
		g2 = pairing.getG2().newRandomElement().getImmutable();
		ps = psGen(s);
		us = usGen(ps);
	}

	/**
	 * 生成初始化密钥
	 * 
	 * @return
	 */
	public void keyGen() {

		// 生成密钥x
		x = pairing.getZr().newRandomElement().getImmutable();
		// 生成公钥pk
		v = g2.duplicate().powZn(x);

	}

	/**
	 * 生成指定s个G中元素
	 * 
	 * @param ps
	 */
	public Element[] usGen(Element[] ps) {// 生成校验标签时减少计算开销
		int s = ps.length;
		Element[] us = new Element[s];
		for (int i = 0; i < s; i++) {
			// ui=g1^ai
			us[i] = g1.duplicate().powZn(ps[i]);
		}

		return us;
	}

	/**
	 * 随机生成s个Zp中元素
	 * 
	 * @param s
	 * @return
	 */
	public Element[] psGen(int s) {
		Element[] ps = new Element[s];
		for (int i = 0; i < s; i++) {
			// a1,...as
			ps[i] = pairing.getZr().newRandomElement();
		}
		return ps;
	}

	/**
	 * 生成某块标签
	 * 
	 * @param blockNum
	 *            块编号
	 * @param mij
	 *            块中段元素数组
	 * @return 块标签
	 */
	private Element metaGen(int blockNum, Element[] mij) {
		int s = mij.length;
		// 生成文件块标签：t=(H(blockNum)*(g1^(aj*mij))^x
		Element aggSum = pairing.getZr().newZeroElement();
		for (int j = 0; j < s; j++) {
			aggSum = aggSum.add(ps[j].duplicate().mulZn(mij[j]));
		}
		byte[] data = String.valueOf(blockNum).getBytes();
		Element Hid = pairing.getG1().newElementFromHash(data, 0, data.length);
		Element t = (Hid.duplicate().mul(g1.duplicate().powZn(aggSum))).powZn(x);

		return t;
	}

	/**
	 * 计算多个数据块标签
	 * 
	 * @param mij
	 *            多块的数据集
	 * @param count
	 *            数据块数量
	 * @return 多块的标签
	 */
	public Element[] metaGen(Element[][] mij, int count) {
		Element[] blockTags = new Element[count];
		for (int i = 0; i < count; i++) {
			blockTags[i] = metaGen(i + 1, mij[i]);
		}
		return blockTags;
	}

	/**
	 * 数据所有者公开信息 ——用数据库保存
	 */
	public void publicInfor() {
		DBOperation.clear(PUBLIC);
		// 持久化信息
		DBOperation.storeData(PUBLIC, g1, "g1");
		DBOperation.storeData(PUBLIC, g2, "g2");
		DBOperation.storeData(PUBLIC, v, "v");
		DBOperation.storeData(PUBLIC, us, "us");
	}

	public void storeTag(Element[] tags) {
		DBOperation.clear(FILETAG);
		DBOperation.storeData(FILETAG, tags, "");
	}

}
