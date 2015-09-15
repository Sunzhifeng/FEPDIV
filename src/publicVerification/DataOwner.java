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

		// �����ļ��������
		Properties pro = PropertiesUtil.loadProperties();
		String filePath = pro.getProperty("filePath");
		int sectors = Integer.valueOf(pro.getProperty("sectors"));
		int sectorSize = Integer.valueOf(pro.getProperty("sectorSize"));
		int blockSize = sectors * sectorSize / 1000; // �鵥λΪK

		// ���ļ�����Ԥ����
		Element[][] nSectors = FileOperation.preProcessFile(filePath, sectors, sectorSize, p.getZr());
		int n = FileOperation.blockNumbers(filePath, blockSize);

		// ���������߲���
		dataOwner.setup(sectors);// ��ʼ��У�����
		dataOwner.keyGen(); // ���ɹ�Կ��˽Կ
		Element[] tags = dataOwner.metaGen(nSectors, n);// ������ǩ
		dataOwner.publicInfor();// ����������Ϣ
		dataOwner.storeTag(tags);// �洢���ǩ

	}

	public static final String FILETAG = "filetag";// ����ļ����ǩ���ݱ�filetag
	public static final String PUBLIC = "public"; // ��Ź�����Ϣ�����ݱ�public

	protected Pairing pairing;
	public Element g1;
	public Element g2;
	private Element[] ps;// ����ֵ
	public Element[] us;
	private Element x;// ˽Կ
	public Element v;// ��Կ

	public DataOwner(Pairing p) {
		this.pairing = p;
	}

	/**
	 * ��ʼ������
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
	 * ���ɳ�ʼ����Կ
	 * 
	 * @return
	 */
	public void keyGen() {

		// ������Կx
		x = pairing.getZr().newRandomElement().getImmutable();
		// ���ɹ�Կpk
		v = g2.duplicate().powZn(x);

	}

	/**
	 * ����ָ��s��G��Ԫ��
	 * 
	 * @param ps
	 */
	public Element[] usGen(Element[] ps) {// ����У���ǩʱ���ټ��㿪��
		int s = ps.length;
		Element[] us = new Element[s];
		for (int i = 0; i < s; i++) {
			// ui=g1^ai
			us[i] = g1.duplicate().powZn(ps[i]);
		}

		return us;
	}

	/**
	 * �������s��Zp��Ԫ��
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
	 * ����ĳ���ǩ
	 * 
	 * @param blockNum
	 *            ����
	 * @param mij
	 *            ���ж�Ԫ������
	 * @return ���ǩ
	 */
	private Element metaGen(int blockNum, Element[] mij) {
		int s = mij.length;
		// �����ļ����ǩ��t=(H(blockNum)*(g1^(aj*mij))^x
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
	 * ���������ݿ��ǩ
	 * 
	 * @param mij
	 *            �������ݼ�
	 * @param count
	 *            ���ݿ�����
	 * @return ���ı�ǩ
	 */
	public Element[] metaGen(Element[][] mij, int count) {
		Element[] blockTags = new Element[count];
		for (int i = 0; i < count; i++) {
			blockTags[i] = metaGen(i + 1, mij[i]);
		}
		return blockTags;
	}

	/**
	 * ���������߹�����Ϣ ���������ݿⱣ��
	 */
	public void publicInfor() {
		DBOperation.clear(PUBLIC);
		// �־û���Ϣ
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
