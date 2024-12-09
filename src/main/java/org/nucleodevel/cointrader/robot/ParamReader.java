package org.nucleodevel.cointrader.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.nucleodevel.cointrader.beans.Coin;
import org.nucleodevel.cointrader.beans.Currency;
import org.nucleodevel.cointrader.beans.Provider;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.RecordSideMode;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.beans.UserSideConfiguration;
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
		userConfiguration.setSideConfiguration(RecordSide.BUY, new UserSideConfiguration(RecordSide.BUY));
		userConfiguration.setSideConfiguration(RecordSide.SELL, new UserSideConfiguration(RecordSide.SELL));

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
					case "-delayTime":
						try {
							userConfiguration.setDelayTime(Integer.parseInt(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-buyMode":
						try {
							userConfiguration.getSideConfiguration(RecordSide.BUY)
									.setMode(RecordSideMode.valueOf(paramValue));
						} catch (Exception ex) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-sellMode":
						try {
							userConfiguration.getSideConfiguration(RecordSide.SELL)
									.setMode(RecordSideMode.valueOf(paramValue));
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
					case "-currency":
						userConfiguration.setCurrency(Currency.valueOf(paramValue));
						break;
					case "-buyRegularRate":
						try {
							userConfiguration.getSideConfiguration(RecordSide.BUY)
									.setRegularRate(new BigDecimal(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-sellRegularRate":
						try {
							userConfiguration.getSideConfiguration(RecordSide.SELL)
									.setRegularRate(new BigDecimal(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-buyBreakdownRate":
						try {
							userConfiguration.getSideConfiguration(RecordSide.BUY)
									.setBreakdownRate(new BigDecimal(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-sellBreakdownRate":
						try {
							userConfiguration.getSideConfiguration(RecordSide.SELL)
									.setBreakdownRate(new BigDecimal(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-minimumCoinAmount":
						try {
							userConfiguration.setMinimumCoinAmount(new BigDecimal(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-incDecPrice":
						try {
							userConfiguration.setIncDecPrice(new BigDecimal(paramValue));
						} catch (NumberFormatException e) {
							throw new ParamValueErrorException(paramLabel);
						}
						break;
					case "-key":
						userConfiguration.setKey(paramValue);
						break;
					case "-secret":
						userConfiguration.setSecret(paramValue);
						break;
					case "-provider":
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