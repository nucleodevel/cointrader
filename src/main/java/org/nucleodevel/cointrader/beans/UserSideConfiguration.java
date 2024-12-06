package org.nucleodevel.cointrader.beans;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class UserSideConfiguration {

	private RecordSide side;

	private RecordSideMode mode;
	private Double minimumRate;
	private Double breakdownRate;

	public UserSideConfiguration(RecordSide side) {
		super();
		this.side = side;
	}

	public RecordSide getSide() {
		return side;
	}

	public void setSide(RecordSide side) {
		this.side = side;
	}

	public RecordSideMode getMode() {
		return mode;
	}

	public void setMode(RecordSideMode mode) {
		this.mode = mode;
	}

	public Double getMinimumRate() {
		return minimumRate;
	}

	public void setMinimumRate(Double minimumRate) {
		this.minimumRate = minimumRate;
	}

	public Double getBreakdownRate() {
		return breakdownRate;
	}

	public void setBreakdownRate(Double breakdownRate) {
		this.breakdownRate = breakdownRate;
	}

	public Double getEffeciveRate() {
		Double rate = 0.0;
		switch (side) {
		case BUY:
			rate = 1 - minimumRate;
			break;
		case SELL:
			rate = 1 + minimumRate;
			break;
		}
		return rate;
	}

	public Double getEffeciveBreakdownRate() {
		Double rate = 0.0;
		switch (side) {
		case BUY:
			if (breakdownRate != null)
				rate = 1 - breakdownRate;
			break;
		case SELL:
			if (breakdownRate != null)
				rate = 1 + breakdownRate;
			break;
		}
		return rate;
	}

	@Override
	public String toString() {
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(5);
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);

		StringBuilder sb = new StringBuilder();

		sb.append(this.getClass().getSimpleName() + ": [");

		if (side != null)
			sb.append("\n      side: " + side);
		if (mode != null)
			sb.append("\n      mode: " + mode);
		if (minimumRate != null)
			sb.append("\n      minimumRate: " + decFmt.format(minimumRate));
		if (breakdownRate != null)
			sb.append("\n      breakdownRate: " + decFmt.format(breakdownRate));

		sb.append("\n    ]");

		return sb.toString();
	}

}