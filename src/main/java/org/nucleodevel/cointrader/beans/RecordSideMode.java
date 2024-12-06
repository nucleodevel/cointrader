package org.nucleodevel.cointrader.beans;

import org.nucleodevel.cointrader.recordsidemode.implementer.AbstractRecordSideModeImplementer;
import org.nucleodevel.cointrader.recordsidemode.implementer.NoneRecordSideModeImplementer;
import org.nucleodevel.cointrader.recordsidemode.implementer.OperationsRecordSideModeImplementer;
import org.nucleodevel.cointrader.recordsidemode.implementer.OrdersRecordSideModeImplementer;
import org.nucleodevel.cointrader.recordsidemode.implementer.OtherOperationsRecordSideModeImplementer;
import org.nucleodevel.cointrader.recordsidemode.implementer.OtherOrdersRecordSideModeImplementer;

public enum RecordSideMode {

	NONE("NONE", NoneRecordSideModeImplementer.class), ORDERS("ORDERS", OrdersRecordSideModeImplementer.class),
	OPERATIONS("OPERATIONS", OperationsRecordSideModeImplementer.class),
	OTHER_ORDERS("OTHER_ORDERS", OtherOrdersRecordSideModeImplementer.class),
	OTHER_OPERATIONS("OTHER_OPERATIONS", OtherOperationsRecordSideModeImplementer.class);

	private final String value;
	private final Class<? extends AbstractRecordSideModeImplementer> implementer;

	private RecordSideMode(String value, Class<? extends AbstractRecordSideModeImplementer> implementer) {
		this.value = value;
		this.implementer = implementer;
	}

	public String getValue() {
		return this.value;
	}

	public Class<? extends AbstractRecordSideModeImplementer> getImplementer() {
		return implementer;
	}

}