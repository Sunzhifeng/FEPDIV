package publicVerification;

public class EnergyCost {
//校验者能耗计算参数
private static double PsCPU=2.5;
private static double PdCPU=5;
private static double PsRF=1.25;
private static double PdRF=1.25;

//服务器能耗计算参数
private static int k=1;
private static int a=3;
private static double f=3.2;


/**
 * 根据时间消耗计算校验者的能量消耗(计算消耗和传输消耗）
 * @param TsCPU		静态CPU运行时间
 * @param TdCPU		动态CPU运行时间
 * @param TsRF		静态RF传输时间
 * @param TdRF		动态RF传输时间
 * @return			消耗的总能量
 */
public static double verEnergy(double TsCPU,double TdCPU,double TsRF,double TdRF){
	return verComputeEnergy(TsCPU,TdCPU)+verTransEnergy(TsRF,TdRF);
}

private static double verComputeEnergy(double t1,double t2){
	return PsCPU*t1+PdCPU*t2;
}

private static double verTransEnergy(double t1,double t2){
	return PsRF*t1+PdRF*t2;
}


/**
 * 计算服务器的能量消耗——t*k*(f^a)
 * @param Tcsp
 * @return
 */
public static double CSPEnergy(double Tcsp){
	return Tcsp*k*Math.pow(f, a);
}

public static double energyCost(double t,double f){
	return t*k*Math.pow(f, a);
}
}
