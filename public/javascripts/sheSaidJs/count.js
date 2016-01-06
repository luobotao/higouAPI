function addcount(ips,url,sharetype,iswx,channel,uid){
	//alert(ips);
$.ajax({
            //提交数据的类型 POST GET
            type:"POST",
            //提交的网址
            url:"/sheSaid/countjs",
            //提交的数据
            data:{ips:ips,curl:url,sharetype:sharetype,iswx:iswx,channel:channel,uid:uid},
            //返回数据的格式
            datatype: "json",//"xml", "html", "script", "json", "jsonp", "text"
            //在请求之前调用的函数
            beforeSend:function(){},
            //成功返回之后调用的函数            
            success:function(data){
            }   ,
            //调用执行后调用的函数
            complete: function(XMLHttpRequest, textStatus){
            },
            //调用出错执行的函数
            error: function(){
                //请求出错处理
                //_alert("请输入正确的验证码!");
            }        
         }); 
}

function addcountdy(ips,url,sharetype,iswx,channel,uid,dyid){
	//alert(ips);
$.ajax({
            //提交数据的类型 POST GET
            type:"POST",
            //提交的网址
            url:"/sheSaid/countjs",
            //提交的数据
            data:{ips:ips,curl:url,sharetype:sharetype,iswx:iswx,channel:channel,uid:uid,daiyanid:dyid},
            //返回数据的格式
            datatype: "json",//"xml", "html", "script", "json", "jsonp", "text"
            //在请求之前调用的函数
            beforeSend:function(){},
            //成功返回之后调用的函数            
            success:function(data){
            }   ,
            //调用执行后调用的函数
            complete: function(XMLHttpRequest, textStatus){
            },
            //调用出错执行的函数
            error: function(){
            }        
         }); 
	}
function addcountwx(ips,url,channel,unionid){
$.ajax({
            //提交数据的类型 POST GET
            type:"POST",
            //提交的网址
            url:"/sheSaid/countjs",
            //提交的数据
            data:{ips:ips,curl:url,sharetype:"",iswx:1,channel:channel,unionid:unionid},
            //返回数据的格式
            datatype: "json",//"xml", "html", "script", "json", "jsonp", "text"
            //在请求之前调用的函数
            beforeSend:function(){},
            //成功返回之后调用的函数            
            success:function(data){
            }   ,
            //调用执行后调用的函数
            complete: function(XMLHttpRequest, textStatus){
            },
            //调用出错执行的函数
            error: function(){
            }        
         }); 
}

function addcountwxdetail(ips,url,channel,unionid,dyid){
	$.ajax({
	            //提交数据的类型 POST GET
	            type:"POST",
	            //提交的网址
	            url:"/sheSaid/countjs",
	            //提交的数据
	            data:{ips:ips,curl:url,sharetype:"",iswx:1,channel:channel,unionid:unionid,daiyanid:dyid},
	            //返回数据的格式
	            datatype: "json",//"xml", "html", "script", "json", "jsonp", "text"
	            //在请求之前调用的函数
	            beforeSend:function(){},
	            //成功返回之后调用的函数            
	            success:function(data){
	            }   ,
	            //调用执行后调用的函数
	            complete: function(XMLHttpRequest, textStatus){
	            },
	            //调用出错执行的函数
	            error: function(){
	            }        
	         }); 
	}