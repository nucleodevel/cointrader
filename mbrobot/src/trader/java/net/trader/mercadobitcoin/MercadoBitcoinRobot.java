package net.trader.mercadobitcoin;

import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;
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
	
	public void readParams(String[] args) throws ParamLabelErrorException, ParamSyntaxErrorException, ParamValueErrorException {
		
		for (int i = 0; i < args.length; i++) {
			
			String paramLabel = args[i];
			
			if (i + 1 == args.length)
				throw new ParamSyntaxErrorException(paramLabel);
			
			String paramValue = args[++i];
			
			switch (paramLabel) {
				case "-dt": 
					try {
						setDelayTime(Integer.parseInt(paramValue));
					} catch (NumberFormatException e) {
						throw new ParamValueErrorException(paramLabel);
					}
					break;
				case "-mbr": 
					try {
						minimumBuyRate = Double.parseDouble(paramValue);
					} catch (NumberFormatException e) {
						throw new ParamValueErrorException(paramLabel);
					}
					break;
				case "-msr": 
					try {
						minimumSellRate = Double.parseDouble(paramValue);
					} catch (NumberFormatException e) {
						throw new ParamValueErrorException(paramLabel);
					}
					break;
				case "-idp": 
					try {
						incDecPrice = Double.parseDouble(paramValue);
					} catch (NumberFormatException e) {
						throw new ParamValueErrorException(paramLabel);
					}
					break;
				default:
					throw new ParamLabelErrorException(paramLabel);
			}
			
		}
		
	}

}