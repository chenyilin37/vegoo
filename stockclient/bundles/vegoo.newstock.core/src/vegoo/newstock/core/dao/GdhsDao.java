package vegoo.newstock.core.dao;

import java.util.Date;

public class GdhsDao extends TaggedDao {

	private String SecurityCode;
	private String SecurityName;
	//private double LatestPrice;     // 抓去数据当天最新股价，可以不要
	//private double PriceChangeRate; // 抓去数据当天最新涨跌幅，可以不要
	private long HolderNum;
	private double PreviousHolderNum;
	private long HolderNumChange;
	private double HolderNumChangeRate;
	private double RangeChangeRate;
	private Date EndDate;
	private Date PreviousEndDate;
	private double HolderAvgCapitalisation;
	private double HolderAvgStockQuantity;
	private double TotalCapitalisation;
	private double CapitalStock;
	private Date NoticeDate;
	// private double ClosePrice; // 季报列表中没有该字段，最新列表中有，当前价，可以不要；

	public String getSecurityCode() {
		return SecurityCode;
	}

	public void setSecurityCode(String securityCode) {
		SecurityCode = securityCode;
	}

	public String getSecurityName() {
		return SecurityName;
	}

	public void setSecurityName(String securityName) {
		SecurityName = securityName;
	}
/*
	public double getLatestPrice() {
		return LatestPrice;
	}

	public void setLatestPrice(double latestPrice) {
		LatestPrice = latestPrice;
	}

	public double getPriceChangeRate() {
		return PriceChangeRate;
	}

	public void setPriceChangeRate(double priceChangeRate) {
		PriceChangeRate = priceChangeRate;
	}
*/
	public long getHolderNum() {
		return HolderNum;
	}

	public void setHolderNum(long holderNum) {
		HolderNum = holderNum;
	}

	public double getPreviousHolderNum() {
		return PreviousHolderNum;
	}

	public void setPreviousHolderNum(double previousHolderNum) {
		PreviousHolderNum = previousHolderNum;
	}

	public long getHolderNumChange() {
		return HolderNumChange;
	}

	public void setHolderNumChange(long holderNumChange) {
		HolderNumChange = holderNumChange;
	}

	public double getHolderNumChangeRate() {
		return HolderNumChangeRate;
	}

	public void setHolderNumChangeRate(double holderNumChangeRate) {
		HolderNumChangeRate = holderNumChangeRate;
	}

	public double getRangeChangeRate() {
		return RangeChangeRate;
	}

	public void setRangeChangeRate(double rangeChangeRate) {
		RangeChangeRate = rangeChangeRate;
	}

	public Date getEndDate() {
		return EndDate;
	}

	public void setEndDate(Date endDate) {
		EndDate = endDate;
	}

	public Date getPreviousEndDate() {
		return PreviousEndDate;
	}

	public void setPreviousEndDate(Date previousEndDate) {
		PreviousEndDate = previousEndDate;
	}

	public double getHolderAvgCapitalisation() {
		return HolderAvgCapitalisation;
	}

	public void setHolderAvgCapitalisation(double holderAvgCapitalisation) {
		HolderAvgCapitalisation = holderAvgCapitalisation;
	}

	public double getHolderAvgStockQuantity() {
		return HolderAvgStockQuantity;
	}

	public void setHolderAvgStockQuantity(double holderAvgStockQuantity) {
		HolderAvgStockQuantity = holderAvgStockQuantity;
	}

	public double getTotalCapitalisation() {
		return TotalCapitalisation;
	}

	public void setTotalCapitalisation(double totalCapitalisation) {
		TotalCapitalisation = totalCapitalisation;
	}

	public double getCapitalStock() {
		return CapitalStock;
	}

	public void setCapitalStock(double capitalStock) {
		CapitalStock = capitalStock;
	}

	public Date getNoticeDate() {
		return NoticeDate;
	}

	public void setNoticeDate(Date noticeDate) {
		NoticeDate = noticeDate;
	}
	
	@Override
	public int hashCode() {
		String data = String.format("%s-%tF-%d-%d", 
				getSecurityCode(),
				getEndDate(),
				Math.round(getHolderNum()),
				Math.round(getPreviousHolderNum()));
		return data.hashCode();
	}

	@Override
	public int getDataTag() {
		return hashCode();
	}

}
