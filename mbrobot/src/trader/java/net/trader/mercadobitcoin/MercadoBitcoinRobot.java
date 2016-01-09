package net.trader.mercadobitcoin;

import java.util.Locale;
import java.util.Scanner;

import net.trader.robot.Robot;

public class MercadoBitcoinRobot extends Robot {

	private Double minimumBuyRate;
	private Double minimumSellRate;
	private Double minimumCoinAmount;
	private Double incDecPrice;
	
	public MercadoBitcoinRobot() {		
		super(10);
		
		minimumBuyRate = 0.01;
		minimumSellRate = 0.008;
		minimumCoinAmount = 0.01;
		incDecPrice = 0.00001;		
	}
	
	public MercadoBitcoinRobot(
		int delayTime, Double minimumBuyRate, Double minimumSellRate, 
		Double minimumCoinAmount, Double incDecPrice 
	) {
		super(delayTime);
		
		this.minimumBuyRate = minimumBuyRate;
		this.minimumSellRate = minimumSellRate;
		this.minimumCoinAmount = minimumCoinAmount;
		this.incDecPrice = incDecPrice;
	}

	public Double getMinimumBuyRate() {
		return minimumBuyRate;
	}

	public void setMinimumBuyRate(Double minimumBuyRate) {
		this.minimumBuyRate = minimumBuyRate;
	}

	public Double getMinimumSellRate() {
		return minimumSellRate;
	}

	public void setMinimumSellRate(Double minimumSellRate) {
		this.minimumSellRate = minimumSellRate;
	}

	public Double getMinimumCoinAmount() {
		return minimumCoinAmount;
	}

	public void setMinimumCoinAmount(Double minimumCoinAmount) {
		this.minimumCoinAmount = minimumCoinAmount;
	}

	public Double getIncDecPrice() {
		return incDecPrice;
	}

	public void setIncDecPrice(Double incDecPrice) {
		this.incDecPrice = incDecPrice;
	}
	
	public void readParams() {
		
		Scanner scanner = new Scanner(System.in );		
		scanner.useLocale(Locale.US);
		
		super.readParams(scanner);
		
		minimumBuyRate = getDoubleFromKeyboard(scanner, minimumBuyRate, "minimum buy rate");
		minimumSellRate = getDoubleFromKeyboard(scanner, minimumSellRate, "minimum sell rate");
		incDecPrice = getDoubleFromKeyboard(scanner, incDecPrice, "inc/dec price");
	    
	    scanner.close();
	}

}
