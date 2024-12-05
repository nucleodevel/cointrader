package org.nucleodevel.cointrader.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nucleodevel.cointrader.beans.Coin;
import org.nucleodevel.cointrader.beans.Currency;
import org.nucleodevel.cointrader.beans.Provider;
import org.nucleodevel.cointrader.beans.RecordSideMode;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ParamLabelErrorException;
import org.nucleodevel.cointrader.exception.ParamSyntaxErrorException;
import org.nucleodevel.cointrader.exception.ParamValueErrorException;

public class ParamReader {

	private File file;

	private UserConfiguration userConfiguration;

	public ParamReader() {

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

	public void readParams(String[] args)
			throws ParamLabelErrorException, ParamSyntaxErrorException, ParamValueErrorException {

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

	public void readParamsFromFile()
			throws ParamLabelErrorException, ParamSyntaxErrorException, ParamValueErrorException, IOException {

		userConfiguration = new UserConfiguration();

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
						try {
							userConfiguration.setBuyMode(RecordSideMode.valueOf(paramValue));
						} catch (Exception ex) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-sm":
						try {
							userConfiguration.setSellMode(RecordSideMode.valueOf(paramValue));
						} catch (Exception ex) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-coin":
						List<String> coinStrList = List.of(paramValue.split(";"));
						List<Coin> coinList = new ArrayList<>();

						coinStrList.stream().forEach((str) -> coinList.add(Coin.valueOf(str)));

						userConfiguration.setCoinList(coinList);
						break;
					case "-curr":
						userConfiguration.setCurrency(Currency.valueOf(paramValue));
						break;
					case "-mbr":
						try {
							userConfiguration.setMinimumBuyRate(Double.parseDouble(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-msr":
						try {
							userConfiguration.setMinimumSellRate(Double.parseDouble(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-bbr":
						try {
							userConfiguration.setBreakdownBuyRate(Double.parseDouble(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-bsr":
						try {
							userConfiguration.setBreakdownSellRate(Double.parseDouble(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-mca":
						try {
							userConfiguration.setMinimumCoinAmount(Double.parseDouble(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-idp":
						try {
							userConfiguration.setIncDecPrice(Double.parseDouble(paramValue));
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
					default:
						throw new ParamLabelErrorException(paramLabel);
					}
				}
			} finally {
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
		} finally {
			br.close();
		}
		return fileContent;
	}

}