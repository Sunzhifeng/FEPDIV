package test;

import tool.Accumulator;
import tool.DataFilter;
import tool.Stopwatch;

/**
 * FindError进行测试
 * 
 * @author MichaelSun
 * @version 1.0
 * @date 2014.12.22
 */
public class TestAccumulator {
	public static void main(String[] args) throws Exception {
		Accumulator averDoTime = new Accumulator();
		//Accumulator averIHTTime = new Accumulator();
		Accumulator averCSPTime = new Accumulator();
		Accumulator averVerTime = new Accumulator();
		Accumulator averTotal = new Accumulator();
		for (int avergaeTime = 0; avergaeTime < 1; avergaeTime++) {
			double DoTime = 0.00;
			// 元数据生成
			Stopwatch genTagTime = new Stopwatch(); // 计时器
			DoTime += genTagTime.elapsedTime();
			DoTime = DataFilter.roundDouble(DoTime / 1000, 3);
			double CSPTime = 0.00;
			double VerTime = 0.00;
			Stopwatch genProofTime = new Stopwatch(); // 计时器
			// 服务器生成证据
			CSPTime += genProofTime.elapsedTime();
			// 检查证据
			Stopwatch verproofTime = new Stopwatch(); // 计时器
			VerTime += verproofTime.elapsedTime();
			CSPTime = DataFilter.roundDouble(CSPTime / 1000, 3);
			VerTime = DataFilter.roundDouble(VerTime / 1000, 3);
			double total = DataFilter.roundDouble(DoTime + CSPTime + VerTime, 3);
			averDoTime.addDataValue(DoTime);
			averCSPTime.addDataValue(CSPTime);
			averVerTime.addDataValue(VerTime);
			averTotal.addDataValue(total);
		}

	}

}
