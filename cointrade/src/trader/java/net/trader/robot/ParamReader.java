package net.trader.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.trader.beans.Broker;
import net.trader.beans.Coin;
import net.trader.beans.Currency;
import net.trader.beans.Provider;
import net.trader.beans.UserConfiguration;
import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;

public class ParamReader {

	private File file;
	
	private UserConfiguration userConfiguration;

	public ParamReader() {
		userConfiguration = new UserConfiguration();
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
								userConfiguration.setDelayTime(Integer.parseInt(paramValue));
							} catch (NumberFormatException e) {
								throw new ParamValueErrorException(paramLabel);
							}
							break;
						case "-bm":
							if (
								!paramValue.equals("none") && !paramValue.equals("order") &&
								!paramValue.equals("operation")
							)
								throw new ParamValueErrorException(paramLabel);
							userConfiguration.setBuyMode(paramValue);					
							break;
						case "-sm":
							if (
								!paramValue.equals("none") && !paramValue.equals("order") &&
								!paramValue.equals("operation")
							)
								throw new ParamValueErrorException(paramLabel);
							userConfiguration.setSellMode(paramValue);					
							break;
						case "-coin": 
							userConfiguration.setCoin(Coin.valueOf(paramValue));
							break;
						case "-curr": 
							userConfiguration.setCurrency(Currency.valueOf(paramValue));
							break;
						case "-mbr": 
							try {
								userConfiguration.setMinimumBuyRate(
									Double.parseDouble(paramValue)
								);
							} catch (NumberFormatException e) {
								throw new ParamValueErrorException(paramLabel);
							}
							break;
						case "-msr": 
							try {
								userConfiguration.setMinimumSellRate(
									Double.parseDouble(paramValue)
								);
							} catch (NumberFormatException e) {
								throw new ParamValueErrorException(paramLabel);
							}
							break;
						case "-mbi": 
							try {
								userConfiguration.setMaxBuyInterval(Double.parseDouble(paramValue));
							} catch (NumberFormatException e) {
								throw new ParamValueErrorException(paramLabel);
							}
							break;
						case "-msi": 
							try {
								userConfiguration.setMaxSellInterval(Double.parseDouble(paramValue));
							} catch (NumberFormatException e) {
								throw new ParamValueErrorException(paramLabel);
							}
							break;
						case "-mca": 
							try {
								userConfiguration.setMinimumCoinAmount(
									Double.parseDouble(paramValue)
								);
							} catch (NumberFormatException e) {
								throw new ParamValueErrorException(paramLabel);
							}
							break;
						case "-idp": 
							try {
								userConfiguration.setIncDecPrice(
									Double.parseDouble(paramValue)
								);
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
							userConfiguration.setProvider(Provider.valueOf(paramValue));
							break;
						case "-ub": 
							userConfiguration.setBroker(Broker.valueOf(paramValue));
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
	
	public String getFileContent() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(getFileName()));
		String fileContent = "";
		try { 
			String line = null;
			while ((line = br.readLine()) != null)
				fileContent += line + "\n";
		}
		finally {
			br.close();
		}
		return fileContent;
	}

}
