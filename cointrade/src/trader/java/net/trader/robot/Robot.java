package net.trader.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;

public class Robot {

	private File file;
	private Integer delayTime;
	private String operationMode;
	private Double minimumBuyRate;
	private Double minimumSellRate;
	private Double minimumCoinAmount;
	private Double incDecPrice;
	private Double sellRateAfterBreakdown;
	private UserConfiguration userConfiguration;

	public Robot() {
		userConfiguration = new UserConfiguration();
	}

	public Robot(String fileName, Integer delayTime) {
		this.file = new File(fileName);
		this.delayTime = delayTime;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileName() {
		if (file == null)
			return "";
		return file.getAbsolutePath();
	}

	public void setFileName(String fileName) {
		this.file = new File(fileName);
	}

	public Integer getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
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

	public UserConfiguration getUserConfiguration() {
		return userConfiguration;
	}

	public void setUserConfiguration(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
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
						case "-mca": 
							try {
								minimumCoinAmount = Double.parseDouble(paramValue);
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
						case "-uk": 
							userConfiguration.setKey(paramValue);
							break;
						case "-us": 
							userConfiguration.setSecret(paramValue);
							break;
						case "-up": 
							userConfiguration.setProvider(paramValue);
							break;
						case "-ub": 
							userConfiguration.setBroker(paramValue);
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
