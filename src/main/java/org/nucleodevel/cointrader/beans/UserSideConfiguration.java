package org.nucleodevel.cointrader.beans;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class UserSideConfiguration {

	private RecordSide side;

	private RecordSideMode mode;
	private Double regularRate;
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

	public Double getRegularRate() {
		return regularRate;
	}

	public void setRegularRate(Double regularRate) {
		this.regularRate = regularRate;
	}

	public Double getBreakdownRate() {
		return breakdownRate;
	}

	public void setBreakdownRate(Double breakdownRate) {
		this.breakdownRate = breakdownRate;
	}

	public Double getEffeciveRegularRate() {
		return 1 + side.getMultiplierFactorForRates().doubleValue() * regularRate;
	}

	public Double getEffeciveBreakdownRate() {
		return 1 + side.getMultiplierFactorForRates().doubleValue() * breakdownRate;
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
		if (regularRate != null)
			sb.append("\n      regularRate: " + decFmt.format(regularRate));
		if (breakdownRate != null)
			sb.append("\n      breakdownRate: " + decFmt.format(breakdownRate));

		sb.append("\n    ]");

		return sb.toString();
	}

}