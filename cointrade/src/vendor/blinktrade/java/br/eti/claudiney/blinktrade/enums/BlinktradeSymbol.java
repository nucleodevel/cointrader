package br.eti.claudiney.blinktrade.enums;

public enum BlinktradeSymbol {
	
	BTCBRL ("BTCBRL"),
	BTCPKR ("BTCPKR"),
	BTCVND ("BTCVND"),
	BTCVEF ("BTCVEF"),
	BTCCLP ("BTCCLP"),
	;
	
	private BlinktradeSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	private String symbol;
	
	public String getId() {
		return symbol;
	}
	
	public static BlinktradeSymbol getSymbolById(String id) {
		for(BlinktradeSymbol s : values()) {
			if(s.getId().equals(id)) {
				return s;
			}
		}
		return null;
	}

}
