package vo.product;


public class ProductsCheckVO {

	public int status;
    public ProdcutsCheckItem data;
	
	public static class ProdcutsCheckItem{
		public String errmsg;
	}
}
