package vegoo.newstock.core.dao;

import java.util.Date;

public class StockDao {
	private int marketId;

	private String code;
	private String name;
	private int typeId;  // 0-A股、1-B股、8-板块指数 9-大盘指数
	private Date publicDate;
	

	public int getMarketId() {
		return marketId;
	}

	public void setMarketId(int marketId) {
		this.marketId = marketId;
	}
	

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getPublicDate() {
		return publicDate;
	}

	public void setPublicDate(Date publicDate) {
		this.publicDate = publicDate;
	}

	public String getUCode() {
		return String.format("%s%d", code, marketId);
	}
	
}
