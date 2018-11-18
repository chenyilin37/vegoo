package vegoo.stockcommon.dao;

import java.util.Date;

public class JgccDao extends TaggedDao {
	private String scode;
	private Date reportDate;
	private int jglx;
	
	private double count ;
	private double shareHDNum;
	private double vPosition;
	private double tabRate;
	private double ltzb;
	//private double shareHDNumChange;
	//private double rateChanges;


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


	public int getJglx() {
		return jglx;
	}


	public void setJglx(int jglx) {
		this.jglx = jglx;
	}


	public double getCount() {
		return count;
	}


	public void setCount(double count) {
		this.count = count;
	}


	public double getShareHDNum() {
		return shareHDNum;
	}


	public void setShareHDNum(double shareHDNum) {
		this.shareHDNum = shareHDNum;
	}


	public double getvPosition() {
		return vPosition;
	}


	public void setvPosition(double vPosition) {
		this.vPosition = vPosition;
	}


	public double getTabRate() {
		return tabRate;
	}


	public void setTabRate(double tabRate) {
		this.tabRate = tabRate;
	}


	public double getLtzb() {
		return ltzb;
	}


	public void setLtzb(double ltzb) {
		this.ltzb = ltzb;
	}

	@Override
	public int getDataTag() {
		return hashCode();
	}
	
	@Override
	public int hashCode() {
		String s = 	String.format("%s-%d-%tF-%f-%f-%f", scode, jglx, reportDate, count, shareHDNum, vPosition);
		return s.hashCode();
	}

}
