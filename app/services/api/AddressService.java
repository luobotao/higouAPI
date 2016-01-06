package services.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Address;
import models.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Logger;
import repositories.AddressRepository;
import utils.Constants;

/**
 * 用户相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class AddressService {

    private static final Logger.ALogger logger = Logger.of(AddressService.class);

    @Inject
    private AddressRepository addressRepository;

	public List<Address> address_list(Long uId) {
		return addressRepository.findByUId(uId);
	}
	
	public Address saveAddress(Address address) {
		List<Address> addressList = addressRepository.findByUIdAndFlg(address.getuId(),"1");
		for(Address addressTemp:addressList){
			addressTemp.setFlg("0");
			addressRepository.save(addressTemp);
		}
		return addressRepository.save(address);
	}
	
	@Transactional
	public void setAddressPY(Long uId,Long addressId) {
		addressRepository.setfirstPY_CH(uId, addressId);
		addressRepository.setfirstPY_EN(uId, addressId);
		return ;
	}

	public List<Address> address_default(Long uId) {
		return addressRepository.findByUIdAndFlg(uId,"1");
	}

	@Transactional
	public void address_setdefault(Long uId, Long addressId) {
		addressRepository.setNotDefaultByUId(uId);
		addressRepository.setdefault(uId,addressId);
	}

	public Address findByUIdAndAddressId(Long uId, Long addressId) {
		return addressRepository.findByUIdAndAddressId(uId,addressId);
	}

	public Address findByAddressId(Long addressid) {
		return addressRepository.findByAddressId(addressid);
	}

	@Transactional
	public void deleteAddress(Long uId, Long addressId) {
		Address address = addressRepository.findByAddressId(addressId);
		if(address!=null && "1".equals(address.getFlg())){//为默认收货地址
			addressRepository.deleteByUIdAndAddressId(uId,addressId);
			List<Address> addressList = addressRepository.findByUId(uId);
			if(addressList.size()>0){
				Address addressTemp = addressList.get(0);
				addressTemp.setFlg("1");
				addressRepository.save(addressTemp);
			}
		}else{
			addressRepository.deleteByUIdAndAddressId(uId,addressId);
		}
	}
	
	@Transactional
	public Address setSaveAddress(Long uId,String name,String phone,String province,String address,String cardId) {
		List<Address> addressList = addressRepository.findByUIdAndNameAndPhoneAndProvinceAndAddress(uId,name,phone,province,address);
		if(addressList!=null && addressList.size()>0){//为默认收货地址
			Address addressInfo = addressList.get(0);
			addressInfo.setCardId(cardId);
			return addressRepository.save(addressInfo);
		}else{
			Address addInfo = new Address();
			addInfo.setName(name);
			addInfo.setProvince(province);
			addInfo.setAddress(address);
			addInfo.setCardId(cardId);
			addInfo.setPhone(phone);
			addInfo.setuId(uId);
			return addressRepository.save(addInfo);
		}
	}
    
	//修改地址中身份证图片地址及标识
	@Transactional
	public void EditaddressImg(Long addressId,Integer cardImg,String imgpath)
	{
		addressRepository.EditaddressImg(addressId, cardImg, imgpath);
	}
	
	
	
}
