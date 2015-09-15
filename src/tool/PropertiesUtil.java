package tool;

import it.unisa.dia.gas.jpbc.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * ���������ļ��Ĺ������ࡪ������д
 * @author MichaelSun
 * @version 1.0
 * @date 2015.1.10
 * @email china-firstszf1989@163.com
 *
 */
public class PropertiesUtil {

	/**
	 * ����Ĭ��·���µ������ļ�
	 * @return		Properties ����
	 * @throws IOException
	 */
	public static  Properties loadProperties() throws IOException{
		InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream("fileConfig.properties");
		Properties props = new Properties();
		props.load(in);		
		in.close();//�ر���Դ
		return props;
	}

	/**
	 * ����ָ��·���µ������ļ�
	 * @param configPath
	 * @return		Properties ����
	 * @throws IOException
	 */
	public static  Properties loadProperties(String configPath) throws IOException{
		InputStream in = new FileInputStream(configPath);
		Properties props = new Properties();
		props.load(in);
		in.close();//�ر���Դ
		return props;
	}

	/**
	 * ��ȡ�����ļ�ĳ��key��ֵ
	 * @param configPath
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public static String readValue(String configPath,String key) throws IOException {
		Properties props=loadProperties(configPath);	
		return  props.getProperty(key);
	}
	public static Map<String,String> readValues(String configPath,String[] keys) throws IOException {
		Properties props;
		InputStream in;
		Map<String,String> result=new HashMap<>();
		try {
			props = loadProperties(configPath);
			in= new FileInputStream(configPath);
			for(int i=0;i<keys.length;i++){
				String s=props.getProperty(keys[i]);
				result.put(keys[i], s);			
			}

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	public static Map<String,List<String>> readValues(String configPath,String[][] keys)  {
		Properties props;
		InputStream in;
		Map<String,List<String>> result=new HashMap<>();
		try {
			props = loadProperties(configPath);
			in= new FileInputStream(configPath);
			int row=keys.length;
			int col=keys[0].length;
			for(int i=0;i<row;i++){
				List<String>sectors=new ArrayList<>();
				for(int j=0;j<col;j++){
					String s=props.getProperty(keys[i][j]);
					sectors.add(s);			
				}
				String blockNum=keys[i][0].split("_")[0];
				result.put(blockNum, sectors);
			}

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * ��ȡproperties��ȫ����Ϣ��map����
	 * @param configPath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Map<String,String> readAllProperties(String configPath) throws FileNotFoundException,IOException  {
		//�������еļ�ֵ
		Map<String,String> map=new HashMap<String,String>();
		Properties props=loadProperties(configPath);		
		Enumeration en = props.propertyNames();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			String Property = props.getProperty(key);
			map.put(key, Property);
		}
		return map;
	}

	/**
	 * ����ĳ��key��ֵ,���������ļ���	
	 * @param configPath		
	 * @param key		
	 * @param value
	 * @throws IOException
	 */
	public static void setValue(String configPath,String key,String value)  {

		Properties props;	
		OutputStream fos ;
		try {
			props=loadProperties(configPath);
			fos= new FileOutputStream(configPath);
			props.setProperty(key, value);		
			// ���� Properties ���е������б�����Ԫ�ضԣ�д�������

			props.store(fos,"last update");
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

	}
	

	public static void saveElement(String configPath,Element[]es,String describe) {
		
		
		Properties props;
		OutputStream fos;
		try {			
			props = loadProperties(configPath);
			fos = new FileOutputStream(configPath);
			for(int i=0;i<es.length;i++){
				props.setProperty(describe+String.valueOf(i+1), es[i].toString());	
			}
			// ���� Properties ���е������б�����Ԫ�ضԣ�д�������
			props.store(fos,"last update"); 

			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	}
	public static void saveElement(String configPath,Element[][]es,String describe) {

		Properties props;
		OutputStream fos;
		try {			
			props = loadProperties(configPath);
			fos = new FileOutputStream(configPath);
			for(int i=0;i<es.length;i++){
				for(int j=0;j<es[0].length;j++)
					props.setProperty(describe+String.valueOf(i+1)+"_"+String.valueOf(j+1), es[i][j].toString());	
			}
			// ���� Properties ���е������б�����Ԫ�ضԣ�д�������
			props.store(fos,"last update"); 

			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	}

	/**
	 * ��������ļ�����
	 * @param configPath
	 * @throws IOException
	 */
	public static void clearUpProperties(String configPath) throws IOException{
		File f = new File(configPath);
		FileWriter fw =  new FileWriter(f);
		fw.write("");		
		fw.close();
	}
	public static void main(String[] args) throws IOException {
		String filePath1="d:/test/test.properties";//�ļ�����Ϊ��testkey testValue
		Map<String,String> testValue=new HashMap<String,String>();
		testValue.put("key1", "value1");
		testValue.put("key2", "value");

		//����ָ��·���µ������ļ�
		Properties pro=loadProperties(filePath1);
		System.out.println(pro);		

		//��ȡ������Ϣ�����϶�����
		Map<String,String> map=readAllProperties(filePath1);		
		System.out.println(map);

		//����ĳ��key-value��ֵ��
		setValue(filePath1,"testKey3", "testMap2");		
		System.out.println(loadProperties(filePath1));				


		//����ļ�
		clearUpProperties(filePath1);
	}
}