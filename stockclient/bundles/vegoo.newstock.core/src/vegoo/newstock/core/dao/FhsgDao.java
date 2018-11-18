package vegoo.newstock.core.dao;

import java.util.Date;

public class FhsgDao extends TaggedDao{
	// private String  MarketType; 
	private String  SCode; 
	// private String  Name; 
	private double  SZZBL;    // 送转总比例
	private double  SGBL;     // 送股比例
	private double  ZGBL; // 转股比例
	private double  XJFH; // 现金分红
	private double  GXL;  // 股息率
	private Date  YAGGR;   // 预案公告日
	private Date  GQDJR;       // 股权登记日
	private Date  CQCXR; //股权除息日
	private double  YAGGRHSRZF; // 预案公告日后10日涨幅
	private double  GQDJRQSRZF;    //股权登记日后10日涨幅
	private double  CQCXRHSSRZF;   //股权除息日后10日涨幅
	private double  YCQTS;         // 未知
	private double  TotalEquity; // 总股本
	private double  EarningsPerShare; // 每股收益
	private double  NetAssetsPerShare; // 每股净资产
	private double  MGGJJ; // 每股公积金
	private double  MGWFPLY; //每股未分配利润
	private double  JLYTBZZ; //净利润同比增长率
	private Date  RDate;   // 报告期
	private Date  ResultsbyDate; //业绩披露日期
	private String  ProjectProgress;    //方案进度
	private String  AllocationPlan;   // 分配预案
	
	private double adjustPrice;
	
	public String getSCode() {
		return SCode;
	}
	public void setSCode(String code) {
		SCode = code;
	}
	public double getSZZBL() {
		return SZZBL;
	}
	public void setSZZBL(double sZZBL) {
		SZZBL = sZZBL;
	}
	public double getSGBL() {
		return SGBL;
	}
	public void setSGBL(double sGBL) {
		SGBL = sGBL;
	}
	public double getZGBL() {
		return ZGBL;
	}
	public void setZGBL(double zGBL) {
		ZGBL = zGBL;
	}
	public double getXJFH() {
		return XJFH;
	}
	public void setXJFH(double xJFH) {
		XJFH = xJFH;
	}
	public double getGXL() {
		return GXL;
	}
	public void setGXL(double gXL) {
		GXL = gXL;
	}
	public Date getYAGGR() {
		return YAGGR;
	}
	public void setYAGGR(Date yAGGR) {
		YAGGR = yAGGR;
	}
	public Date getGQDJR() {
		return GQDJR;
	}
	public void setGQDJR(Date gQDJR) {
		GQDJR = gQDJR;
	}
	public Date getCQCXR() {
		return CQCXR;
	}
	public void setCQCXR(Date cQCXR) {
		CQCXR = cQCXR;
	}
	public double getYAGGRHSRZF() {
		return YAGGRHSRZF;
	}
	public void setYAGGRHSRZF(double yAGGRHSRZF) {
		YAGGRHSRZF = yAGGRHSRZF;
	}
	public double getGQDJRQSRZF() {
		return GQDJRQSRZF;
	}
	public void setGQDJRQSRZF(double gQDJRQSRZF) {
		GQDJRQSRZF = gQDJRQSRZF;
	}
	public double getCQCXRHSSRZF() {
		return CQCXRHSSRZF;
	}
	public void setCQCXRHSSRZF(double cQCXRHSSRZF) {
		CQCXRHSSRZF = cQCXRHSSRZF;
	}
	public double getTotalEquity() {
		return TotalEquity;
	}
	public void setTotalEquity(double totalEquity) {
		TotalEquity = totalEquity;
	}
	public double getEarningsPerShare() {
		return EarningsPerShare;
	}
	public void setEarningsPerShare(double earningsPerShare) {
		EarningsPerShare = earningsPerShare;
	}
	public double getNetAssetsPerShare() {
		return NetAssetsPerShare;
	}
	public void setNetAssetsPerShare(double netAssetsPerShare) {
		NetAssetsPerShare = netAssetsPerShare;
	}
	public double getMGGJJ() {
		return MGGJJ;
	}
	public void setMGGJJ(double mGGJJ) {
		MGGJJ = mGGJJ;
	}
	public double getMGWFPLY() {
		return MGWFPLY;
	}
	public void setMGWFPLY(double mGWFPLY) {
		MGWFPLY = mGWFPLY;
	}
	public double getJLYTBZZ() {
		return JLYTBZZ;
	}
	public void setJLYTBZZ(double jLYTBZZ) {
		JLYTBZZ = jLYTBZZ;
	}
	public Date getRDate() {
		return RDate;
	}
	public void setRDate(Date reportingPeriod) {
		RDate = reportingPeriod;
	}
	public Date getResultsbyDate() {
		return ResultsbyDate;
	}
	public void setResultsbyDate(Date resultsbyDate) {
		ResultsbyDate = resultsbyDate;
	}
	public String getProjectProgress() {
		return ProjectProgress;
	}
	public void setProjectProgress(String projectProgress) {
		ProjectProgress = projectProgress;
	}
	public String getAllocationPlan() {
		return AllocationPlan;
	}
	public void setAllocationPlan(String allocationPlan) {
		AllocationPlan = allocationPlan;
	}
	public double getYCQTS() {
		return YCQTS;
	}
	public void setYCQTS(double yCQTS) {
		YCQTS = yCQTS;
	}
	
	public int getDataTag() {
		return hashCode();
	}

	@Override
	public int hashCode() {
		String data = String.format("%s-%tF-%f-%f-%f-%f-%f-%tF-%tF-%tF-%s", 
				   SCode, 
				   RDate,   // 报告期
				   SZZBL,    // 送转总比例
				   SGBL,     // 送股比例
				   ZGBL, 	// 转股比例
				   XJFH, 	// 现金分红
				   GXL,  	// 股息率
				   YAGGR,   // 预案公告日
				   GQDJR,   // 股权登记日
				   CQCXR,   //股权除息日
				   ProjectProgress    //方案进度
				);
		
		return data.hashCode();
	}
	public double getAdjustPrice() {
		return adjustPrice;
	}
	public void setAdjustPrice(double adjustPrice) {
		this.adjustPrice = adjustPrice;
	}
	
	
}
