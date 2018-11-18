package vegoo.stockcommon.dao;

public class BlockDao {
	private String uCode;
	private String code;
	private String name;
	private int marketId;
	private int typeId;  // 1-行业、2-概念 3-地域   9-板别

	
	public String getUCode() {
		return uCode;
	}
	public void setUCode(String uCode) {
		this.uCode = uCode;
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

}
