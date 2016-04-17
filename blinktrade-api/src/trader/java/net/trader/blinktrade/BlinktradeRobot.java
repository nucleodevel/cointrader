package net.trader.blinktrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;
import net.trader.robot.Robot;

public class BlinktradeRobot extends Robot {
	
	private String operationMode;
	private Double minimumBuyRate;
	private Double minimumSellRate;
	private Double minimumCoinAmount;
	private Double incDecPrice;
	private Double sellRateAfterBreakdown;
	
	public BlinktradeRobot() {		
		super();
		
		operationMode = "bs";
		minimumBuyRate = 0.009;
		minimumSellRate = 0.007;
		minimumCoinAmount = 0.0001;
		incDecPrice = 0.01;		
		sellRateAfterBreakdown = -0.05;
	}

	public String getOperationMode() {
		return operationMode;
	}

	public void setOperationMode(String operationMode) {
		this.operationMode = operationMode;
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

	public Double getSellRateAfterBreakdown() {
		return sellRateAfterBreakdown;
	}

	public void setSellRateAfterBreakdown(Double sellRateAfterBreakdown) {
		this.sellRateAfterBreakdown = sellRateAfterBreakdown;
	}
	
	public void readParams(String[] args) throws ParamLabelErrorException, ParamSyntaxErrorException, ParamValueErrorException {
		
		for (int i = 0; i < args.length; i++) {
			
			String paramLabel = args[i];
			
			if (i + 1 == args.length)
				throw new ParamSyntaxErrorException(paramLabel);
			
			String paramValue = args[++i];
			
			switch (paramLabel) {
				case "-f": 
					setFileName(paramValue);
					break;
				case "-dt": 
					try {
						setDelayTime(Integer.parseInt(paramValue));
					} catch (NumberFormatException e) {
						throw new ParamValueErrorException(paramLabel);
					}
					break;
				case "-om":
					if (
						!paramValue.equals("b") || !paramValue.equals("s") ||
						!paramValue.equals("bs")
					)
						throw new ParamValueErrorException(paramLabel);
					operationMode = paramValue;					
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
				case "-srab": 
					try {
						sellRateAfterBreakdown = Double.parseDouble(paramValue);
					} catch (NumberFormatException e) {
						throw new ParamValueErrorException(paramLabel);
					}
					break;
				default:
					throw new ParamLabelErrorException(paramLabel);
			}
			
		}
		
	}
	

	
	public void readParamsFromFile() throws ParamLabelErrorException, ParamSyntaxErrorException, ParamValueErrorException, IOException {
		
		if (getFileName().equals(""))
			return;
		File file = getFile();
		
		if (!file.exists())
			throw new IOException();
		else {		
			// Construct BufferedReader from FileReader
			BufferedReader br = new BufferedReader(new FileReader(getFileName()));
			
			try { 
				String line = null;
				while ((line = br.readLine()) != null) {
					String[] args = line.split("\\s+");
					if (args.length < 1)
						return;
					String paramLabel = args[0];
					if (args.length < 2)
						throw new ParamSyntaxErrorException(paramLabel);
					String paramValue = args[1];
					
					switch (paramLabel) {
						case "-dt": 
							try {
								setDelayTime(Integer.parseInt(paramValue));
							} catch (NumberFormatException e) {
								throw new ParamValueErrorException(paramLabel);
							}
							break;
						case "-om":
							if (
								!paramValue.equals("b") && !paramValue.equals("s") &&
								!paramValue.equals("bs")
							)
								throw new ParamValueErrorException(paramLabel);
							operationMode = paramValue;					
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
						case "-srab": 
							try {
								sellRateAfterBreakdown = Double.parseDouble(paramValue);
							} catch (NumberFormatException e) {
								throw new ParamValueErrorException(paramLabel);
							}
							break;
						default:
							throw new ParamLabelErrorException(paramLabel);
					}
				}
			}
			finally {
				br.close();
			}
	    }
		
	}

}
