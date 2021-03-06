package vo.user;

import java.util.ArrayList;
import java.util.List;

import vo.product.ProductSearchMouldDetailVO.ProductSearchMouldDetailItem;

public class ProductSearchMouldVO {
	public String status;
	public int endflag;
	public String reffer;
	public List<DataInfo> data=new ArrayList<DataInfo>();

	public static class DataInfo {
		public String cardId;
		public String mould;
		public String struct;
		public String stxt;
		public String sdate;
		public List<LayoutInfo> layout;
		public List<PInfo> plist;
	}

	public static class LayoutInfo {
		public String start;
		public String end;
	}

	public static class PInfo {
		public String linkurl;
		public String img;
		public ProductSearchMouldDetailItem pinfo;
	}
}