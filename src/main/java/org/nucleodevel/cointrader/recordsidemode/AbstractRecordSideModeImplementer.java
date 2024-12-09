package org.nucleodevel.cointrader.recordsidemode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.OrderType;
import org.nucleodevel.cointrader.beans.Record;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.exception.NotAvailableMoneyException;
import org.nucleodevel.cointrader.robot.ProviderReport;
import org.nucleodevel.cointrader.utils.Utils;

public abstract class AbstractRecordSideModeImplementer {

	private static long NUM_OF_CONSIDERED_RECORDS_FOR_LAST_RELEVANT_PRICE_BY_RECORDS_AND_THEIR_POSITIONS = 5;

	protected ProviderReport providerReport;
	protected RecordSide side;

	protected UserConfiguration userConfiguration;
	protected CoinCurrencyPair coinCurrencyPair;

	public AbstractRecordSideModeImplementer(ProviderReport providerReport, RecordSide side)
			throws ApiProviderException {
		super();
		this.providerReport = providerReport;
		this.side = side;
		this.userConfiguration = providerReport.getUserConfiguration();
		this.coinCurrencyPair = providerReport.getCoinCurrencyPair();
	}

	public abstract void tryToMakeOrders() throws ApiProviderException;

	protected void makeOrdersByLastRelevantPrice(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			BigDecimal lastRelevantPrice, Boolean hasToWinCurrent) throws ApiProviderException {

		try {
			Order newOrder = winTheOrder(coinCurrencyPair, side, 0, lastRelevantPrice, hasToWinCurrent);

			List<Order> userActiveOrders = providerReport.getUserActiveOrders(coinCurrencyPair, side);
			Order myOrder = userActiveOrders.size() > 0 ? userActiveOrders.get(0) : null;

			if (newOrder == myOrder || (newOrder == null && myOrder != null))
				System.out.println("  Maintaining previous - " + myOrder);
			else {
				if (myOrder != null) {
					providerReport.cancelOrder(myOrder);
					System.out.println("  " + side + " cancelled: " + " - " + myOrder);
				}
				providerReport.createOrder(newOrder, side);
				System.out.println("  " + side + " created: " + " - " + newOrder);
			}
		} catch (NotAvailableMoneyException e) {
			System.out.println("  There are no money available for " + side);
		}
	}

	protected void cancelAllOrdersOfOtherCoinCurrencyPairsButSameSide(CoinCurrencyPair coinCurrencyPair,
			RecordSide side) throws ApiProviderException {

		for (CoinCurrencyPair ccp : providerReport.getCoinCurrencyPairList()) {

			if (!ccp.toString().equals(coinCurrencyPair.toString())) {
				List<Order> userActiveOrders = providerReport.getUserActiveOrders(ccp, side);

				Order myOrder = userActiveOrders.size() > 0 ? userActiveOrders.get(0) : null;
				if (myOrder != null) {
					System.out.println(
							"  " + side + " cancelled by order of " + coinCurrencyPair + " : " + " - " + myOrder);
					providerReport.cancelOrder(myOrder);
				}
			}

		}
	}

	protected BigDecimal getLastRelevantPriceByOperationsAndTheirAmounts(CoinCurrencyPair coinCurrencyPair,
			RecordSide side, Boolean showMessages) throws ApiProviderException {

		List<Record> selectedList = new ArrayList<>(providerReport.getUserOperations(coinCurrencyPair));
		return getLastRelevantPriceByRecordsAndTheirAmounts(coinCurrencyPair, side, selectedList, showMessages);
	}

	protected BigDecimal getLastRelevantPriceByOperationsAndTheirPositions(CoinCurrencyPair coinCurrencyPair,
			RecordSide side, Boolean showMessages) throws ApiProviderException {

		List<Record> selectedList = new ArrayList<>(providerReport.getUserOperations(coinCurrencyPair));
		return getLastRelevantPriceByRecordsAndTheirPositions(coinCurrencyPair, side, selectedList, showMessages);
	}

	protected BigDecimal getLastRelevantPriceByOrdersAndTheirAmounts(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			Boolean showMessages) throws ApiProviderException {

		List<Record> selectedList = new ArrayList<>(providerReport.getOrderBookBySide(coinCurrencyPair, side));
		return getLastRelevantPriceByRecordsAndTheirAmounts(coinCurrencyPair, side, selectedList, showMessages);
	}

	protected BigDecimal getLastRelevantPriceByOrdersAndTheirPositions(CoinCurrencyPair coinCurrencyPair,
			RecordSide side, Boolean showMessages) throws ApiProviderException {

		List<Record> recordList = new ArrayList<>(providerReport.getOrderBookBySide(coinCurrencyPair, side));
		return getLastRelevantPriceByRecordsAndTheirPositions(coinCurrencyPair, side, recordList, showMessages);
	}

	private BigDecimal getLastRelevantPriceByRecordsAndTheirPositions(CoinCurrencyPair coinCurrencyPair,
			RecordSide side, List<Record> recordList, Boolean showMessages) throws ApiProviderException {

		DecimalFormat decFmt = Utils.getDefaultDecimalFormat();

		BigDecimal lastRelevantPriceByRecords = new BigDecimal(0);

		BigDecimal sumOfCoin = BigDecimal.valueOf(0);
		BigDecimal sumOfNumerators = BigDecimal.valueOf(0);

		List<Record> selectedList = new ArrayList<>();

		for (int i = 0; i < NUM_OF_CONSIDERED_RECORDS_FOR_LAST_RELEVANT_PRICE_BY_RECORDS_AND_THEIR_POSITIONS; i++) {
			Record record = recordList.get(i);
			sumOfCoin = sumOfCoin.add(record.getCoinAmount());
			sumOfNumerators = sumOfNumerators.add(record.getCoinAmount().multiply(record.getCurrencyPrice()));
			selectedList.add(record);
		}

		if (sumOfCoin.compareTo(BigDecimal.ZERO) != 0) {
			lastRelevantPriceByRecords = sumOfNumerators.divide(sumOfCoin, 8, RoundingMode.HALF_EVEN);
		}

		if (showMessages) {
			System.out.println(
					"  Last relevant " + side + " price by records: " + decFmt.format(lastRelevantPriceByRecords));
			System.out.println("  Considered records: ");
			for (Record record : selectedList)
				System.out.println("    " + record);
			System.out.println("");
		}

		return lastRelevantPriceByRecords;
	}

	private BigDecimal getLastRelevantPriceByRecordsAndTheirAmounts(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			List<Record> recordList, Boolean showMessages) throws ApiProviderException {

		DecimalFormat decFmt = Utils.getDefaultDecimalFormat();

		BigDecimal lastRelevantPriceByRecords = new BigDecimal(0);
		BigDecimal oldCoinAmount = new BigDecimal(0);
		BigDecimal sumOfAmount = BigDecimal.valueOf(0);

		BigDecimal otherSideAmountWithOpenOrders = providerReport.getBalance(coinCurrencyPair)
				.getSideAmount(side.getOther());

		List<Record> selectedList = new ArrayList<>();
		for (Record record : recordList) {
			BigDecimal otherSideAmount = record.getSideAmount(side.getOther());
			if (record.getSide() == side) {
				if (sumOfAmount.add(otherSideAmount).compareTo(otherSideAmountWithOpenOrders) <= 0) {
					sumOfAmount = sumOfAmount.add(otherSideAmount);
					selectedList.add(record);
				} else {
					oldCoinAmount = record.getCoinAmount();
					BigDecimal otherSideRestOfAmount = otherSideAmountWithOpenOrders.subtract(sumOfAmount);
					BigDecimal recordPrice = record.getCurrencyPrice();
					BigDecimal otherSideEstimatedCoinAmount = side.getOther()
							.getEstimatedCoinAmountByAmountAndPrice(otherSideRestOfAmount, recordPrice);

					record.setCoinAmount(otherSideEstimatedCoinAmount);
					selectedList.add(record);
					sumOfAmount = sumOfAmount.add(otherSideAmountWithOpenOrders).subtract(sumOfAmount);
					break;
				}
			}
		}
		if (sumOfAmount.compareTo(BigDecimal.ZERO) != 0) {
			for (Record record : selectedList) {
				BigDecimal otherSideAmount = record.getSideAmount(side.getOther());
				lastRelevantPriceByRecords = lastRelevantPriceByRecords.add(otherSideAmount
						.multiply(record.getCurrencyPrice()).divide(sumOfAmount, 8, RoundingMode.HALF_EVEN));
			}
		}
		if (showMessages) {
			System.out.println("  Last relevant " + side + " price: " + decFmt.format(lastRelevantPriceByRecords));
			System.out.println("  Considered records: ");
			for (Record record : selectedList)
				System.out.println("    " + record.toString());
			System.out.println("");
		}
		if (selectedList.size() > 0)
			selectedList.get(selectedList.size() - 1).setCoinAmount(oldCoinAmount);

		return lastRelevantPriceByRecords;
	}

	private Order winTheOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side, Integer orderIndex,
			BigDecimal lastRelevantPrice, Boolean hasToWinCurrent)
			throws ApiProviderException, NotAvailableMoneyException {

		List<Order> activeOrders = providerReport.getOrderBookBySide(coinCurrencyPair, side);
		if (orderIndex >= activeOrders.size() - 1) {
			Order myOrder = providerReport.getUserActiveOrders(coinCurrencyPair, side).size() > 0
					? providerReport.getUserActiveOrders(coinCurrencyPair, side).get(0)
					: null;
			if (myOrder != null)
				providerReport.cancelOrder(myOrder);
			BigDecimal coinAmount = providerReport.getBalance(coinCurrencyPair).getEstimatedCoinAmount(side,
					lastRelevantPrice);

			if (coinAmount.compareTo(userConfiguration.getMinimumCoinAmount()) < 0) {
				throw new NotAvailableMoneyException();
			}

			Order newOrder = new Order(coinCurrencyPair.getCoin(), coinCurrencyPair.getCurrency(), side, coinAmount,
					lastRelevantPrice);
			newOrder.setType(OrderType.LIMITED);
			return newOrder;
		}

		Order order = activeOrders.get(orderIndex);

		BigDecimal orderCurrencyPriceToCompare = lastRelevantPrice == null ? null : order.getCurrencyPrice();

		boolean isAGoodOrder = lastRelevantPrice == null || lastRelevantPrice.compareTo(BigDecimal.ZERO) <= 0;
		if (!isAGoodOrder && orderCurrencyPriceToCompare != null)
			isAGoodOrder = side.isAGoodRecordByRecordCurrencyPriceAndLastRelevantPrice(orderCurrencyPriceToCompare,
					lastRelevantPrice);

		if (isAGoodOrder) {
			Integer effectiveOrderIndex = hasToWinCurrent ? orderIndex : (orderIndex > 0 ? orderIndex - 1 : 0);
			Order newOrder = tryToWinAnOrder(coinCurrencyPair, side, effectiveOrderIndex);
			if (newOrder != null)
				return newOrder;
		}

		return winTheOrder(coinCurrencyPair, side, orderIndex + 1, lastRelevantPrice, hasToWinCurrent);
	}

	private Order tryToWinAnOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side, Integer orderIndex)
			throws NotAvailableMoneyException, ApiProviderException {

		DecimalFormat decFmt = Utils.getDefaultDecimalFormat();

		List<Order> activeOrders = providerReport.getOrderBookBySide(coinCurrencyPair, side);
		List<Order> userActiveOrders = providerReport.getUserActiveOrders(coinCurrencyPair, side);

		Order order = activeOrders.get(orderIndex);
		Order nextOrder = activeOrders.size() - 1 == orderIndex ? null : activeOrders.get(orderIndex + 1);
		Order bestOtherSideOrder = providerReport.getCurrentTopOrder(coinCurrencyPair, side.getOther());

		BigDecimal minimumCoinAmount = userConfiguration.getMinimumCoinAmount();
		BigDecimal effectiveIncDecPrice = userConfiguration.getEffectiveIncDecPrice(side);

		BigDecimal currencyPricePlusIncDec = order.getCurrencyPrice().add(effectiveIncDecPrice);
		BigDecimal balanceCoinAmount = providerReport.getBalance(coinCurrencyPair).getEstimatedCoinAmount(side,
				currencyPricePlusIncDec);

		if (balanceCoinAmount.compareTo(minimumCoinAmount) < 0) {
			throw new NotAvailableMoneyException();
		}

		// get the unique order or null
		Order myOrder = userActiveOrders.size() > 0 ? userActiveOrders.get(0) : null;
		boolean isOrderCurrencyPriceEqualsToMyOrderCurrencyPrice = myOrder != null
				&& decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myOrder.getCurrencyPrice()));

		// if my order isn't the best, delete it and create another
		if (myOrder == null || !isOrderCurrencyPriceEqualsToMyOrderCurrencyPrice) {

			BigDecimal absDiffIncDecOrderAndBestOtherSideOrder = order.getCurrencyPrice().add(effectiveIncDecPrice)
					.subtract(bestOtherSideOrder.getCurrencyPrice()).abs();

			Boolean isNearTheBestOtherSideOrder = absDiffIncDecOrderAndBestOtherSideOrder
					.compareTo(effectiveIncDecPrice) <= 0;

			if (isNearTheBestOtherSideOrder)
				currencyPricePlusIncDec = order.getCurrencyPrice();

			Order newOrder = new Order(coinCurrencyPair.getCoin(), coinCurrencyPair.getCurrency(), side,
					balanceCoinAmount, currencyPricePlusIncDec);
			newOrder.setType(OrderType.LIMITED);
			newOrder.setPosition(orderIndex);
			return newOrder;

		} else {

			BigDecimal absDiffOrderCoinAmountAndBalanceCoinAmount = order.getCoinAmount().subtract(balanceCoinAmount)
					.abs();
			BigDecimal absDiffOrderCurrencyPriceAndNextOrderCurrencyPrice = order.getCurrencyPrice()
					.subtract(nextOrder.getCurrencyPrice()).abs();

			if (isOrderCurrencyPriceEqualsToMyOrderCurrencyPrice
					&& absDiffOrderCoinAmountAndBalanceCoinAmount.compareTo(minimumCoinAmount) <= 0
					&& absDiffOrderCurrencyPriceAndNextOrderCurrencyPrice.compareTo(effectiveIncDecPrice) <= 0) {

				myOrder.setPosition(orderIndex);
				return myOrder;

			}
		}
		return null;
	}

}
