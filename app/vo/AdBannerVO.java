package vo;

import models.AdBanner;

/**
 * @author luobotao
 *
 */
public class AdBannerVO  {

	private String adtype;

    private String img;

    private String url;


    public AdBannerVO() {
    }

    public static AdBannerVO create(AdBanner adBanner) {
    	String domain = play.Configuration.root().getString("domain", "http://ht2.neolix.cn");
    	String adload = play.Configuration.root().getString("adload", "/pimgs/adload/");
        AdBannerVO vo = new AdBannerVO();
        vo.setAdtype(String.valueOf(adBanner.getAdtype()));
        vo.setImg(domain+adload+adBanner.getFilename());
        vo.setUrl(adBanner.getLinkurl());
        return vo;
    }

	public String getAdtype() {
		return adtype;
	}

	public void setAdtype(String adtype) {
		this.adtype = adtype;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
    

}
