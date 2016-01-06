package vo;

import java.util.List;

public class SearchMouldVO {
	public String status;
	public int endflag;
	public List<DataInfo> data;

	class DataInfo {
		public String cardId;
		public String mould;
		public String struct;
		public String sectionPic;
		public String stxt;
		public String sdate;
		public List<LayoutInfo> layout;
		public List<PInfo> plist;
	}

	class LayoutInfo {
		public String start;
		public String end;
	}

	class PInfo {
		public String linkurl;
		public String img;
		public models.Product pinfo;
	}
}