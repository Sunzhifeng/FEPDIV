package tool;

public class DataFilter {
	//四舍五入保留precision位小数
		public static Double roundDouble(double val, int precision) {  
			  Double ret = null;  
			        try {  
			           double factor = Math.pow(10, precision);  
			           ret = Math.floor(val * factor + 0.5) / factor;  
			        } catch (Exception e) {  
			           e.printStackTrace();  
			        }  
			       return ret;  
			    } 
		
}
