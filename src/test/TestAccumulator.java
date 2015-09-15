package test;

import tool.Accumulator;
import tool.DataFilter;
import tool.Stopwatch;

/**
 * FindError���в���
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
			// Ԫ��������
			Stopwatch genTagTime = new Stopwatch(); // ��ʱ��
			DoTime += genTagTime.elapsedTime();
			DoTime = DataFilter.roundDouble(DoTime / 1000, 3);
			double CSPTime = 0.00;
			double VerTime = 0.00;
			Stopwatch genProofTime = new Stopwatch(); // ��ʱ��
			// ����������֤��
			CSPTime += genProofTime.elapsedTime();
			// ���֤��
			Stopwatch verproofTime = new Stopwatch(); // ��ʱ��
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
