package org.nucleodevel.cointrader.beans;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class UserSideConfiguration {

	private RecordSide side;

	private RecordSideMode mode;
	private BigDecimal regularRate;
	private BigDecimal breakdownRate;

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

	public BigDecimal getRegularRate() {
		return regularRate;
	}

	public void setRegularRate(BigDecimal regularRate) {
		this.regularRate = regularRate;
	}

	public BigDecimal getBreakdownRate() {
		return breakdownRate;
	}

	public void setBreakdownRate(BigDecimal breakdownRate) {
		this.breakdownRate = breakdownRate;
	}

	public BigDecimal getEffeciveRegularRate() {
		return side.getMultiplierFactor().multiply(regularRate).add(BigDecimal.valueOf(1));
	}

	public BigDecimal getEffeciveBreakdownRate() {
		return side.getMultiplierFactor().multiply(breakdownRate).add(BigDecimal.valueOf(1));
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