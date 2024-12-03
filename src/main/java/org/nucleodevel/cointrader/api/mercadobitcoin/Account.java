package org.nucleodevel.cointrader.api.mercadobitcoin;

public class Account {

	private String id;
	private String name;
	private String type;
	private String currency;
	private String currencySign;

	public Account(String id, String name, String type, String currency, String currencySign) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.currency = currency;
		this.currencySign = currencySign;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCurrencySign() {
		return currencySign;
	}

	public void setCurrencySign(String currencySign) {
		this.currencySign = currencySign;
	}

	@Override
	public String toString() {
		return "Account [" + "id=" + id + ", name=" + name + ", type=" + type + ", currency=" + currency
				+ ", currencySign=" + currencySign + "]";
	}

}
