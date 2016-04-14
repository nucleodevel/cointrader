package br.eti.claudiney.blinktrade.enums;

public enum BlinktradeBroker {

	SURBITCOIN		("1"),
	VBTC			("3"),
	FOXBIT			("4"),
	TESTNET			("5"),
	URDUBIT			("6"),
	CHILEBIT		("9"),
	;
	
	private BlinktradeBroker(String brokerID) {
		this.brokerID = brokerID;
	}
	
	private String brokerID;
	
	public String getBrokerID() {
		return brokerID;
	}

}
