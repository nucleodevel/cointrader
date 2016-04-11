package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.eti.claudiney.blinktrade.utils.Utils;

@SuppressWarnings({"serial", "unchecked"})
public class BalanceResponse implements Serializable {

	private int Status;
	private String Description;
	private List<Map<String, Object>> Responses;

	public int getStatus() {
		return Status;
	}

	public String getDescription() {
		return Description;
	}
	
	public List<Balance> getBalanceList() {
		
		List<Balance> l = new ArrayList<Balance>();
		
		for(Map<String, Object> m: Responses) {
			
			Map<String, ?> entry = (Map<String, ?>) m.get("4");
			Integer balanceRequestId = new Integer( ((Number)m.get("BalanceReqID")).intValue() );
			
			Balance b = new Balance(balanceRequestId);
			b.setBtcLocked(Utils.getBigInteger(entry.get("BTC_locked")));
			b.setBitcoinAmount(Utils.getBigInteger(entry.get("BTC")));
			
			BigDecimal currencyAmount = BigDecimal.ZERO;
			BigDecimal currencyLocked = BigDecimal.ZERO;
			
			for(BlinktradeCurrency c: BlinktradeCurrency.SUPPORTED_CURRENCIES) {
				
				if(entry.containsKey(c.getId())) {
					String amount = entry.get(c.getId()).toString();
					currencyAmount = new BigDecimal(amount)
						.divide(c.getRate(), c.getRateSize(), RoundingMode.DOWN);
				}
				
				String locked = c.getId() + "_locked";
				
				if(entry.containsKey(locked)) {
					String amount = entry.get(locked).toString();
					currencyLocked = new BigDecimal(amount)
						.divide(c.getRate(), c.getRateSize(), RoundingMode.DOWN);
				}
				
			}
			
			b.setCurrencyAmount(currencyAmount);
			b.setCurrencyLocked(currencyLocked);
			b.setClientID( Utils.getBigInteger(m.get("ClientID")).toString() );
			
			l.add(b);
			
		}
		
		return l;
		
	}
	
	public Balance getFirstResult() {
		
		List<Balance> balances = getBalanceList();
		if(balances.size() > 0) {
			return balances.get(0);
		}
		
		return null;
		
	}

}
