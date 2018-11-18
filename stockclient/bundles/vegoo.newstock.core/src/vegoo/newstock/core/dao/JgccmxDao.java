package vegoo.newstock.core.dao;

import java.util.Date;

public class JgccmxDao {
	private String scode;
	private Date reportDate;
	private String shcode;
	
	private String indtCode;
	private int jglx;
	
	private double shareHDNum;
	private double vposition;
	private double tabRate;
	private double tabProRate;
	
	public String getScode() {
		return scode;
	}
	public void setScode(String scode) {
		this.scode = scode;
	}
	public Date getReportDate() {
		return reportDate;
	}
	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}
	public String getShcode() {
		return shcode;
	}
	public void setShcode(String shcode) {
		this.shcode = shcode;
	}
	public String getIndtCode() {
		return indtCode;
	}
	public void setIndtCode(String indtCode) {
		this.indtCode = indtCode;
	}
	public int getJglx() {
		return jglx;
	}
	public void setJglx(int jglx) {
		this.jglx = jglx;
	}
	public double getShareHDNum() {
		return shareHDNum;
	}
	public void setShareHDNum(double shareHDNum) {
		this.shareHDNum = shareHDNum;
	}
	public double getVposition() {
		return vposition;
	}
	public void setVposition(double vposition) {
		this.vposition = vposition;
	}
	public double getTabRate() {
		return tabRate;
	}
	public void setTabRate(double tabRate) {
		this.tabRate = tabRate;
	}
	public double getTabProRate() {
		return tabProRate;
	}
	public void setTabProRate(double tabProRate) {
		this.tabProRate = tabProRate;
	}
	
	
}
