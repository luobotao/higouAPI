function _alert(txt){		
	 var cont = '<div class="pup" style="z-index:3000"><div class="pup_info"></div></div>';
	 $('.pup').removeClass('pup_show').remove();			 
	 $('body').append(cont);
	 setTimeout(function(){
	   this.txt = txt || '信息错误'; 
	   $('.pup_info').html(this.txt)	 
	   $('.pup').addClass('pup_show');  
	 },300); 
	 setTimeout(function(){
	   $('.pup').removeClass('pup_show');  
	 },2100) 
}


function _load(){   
  var cont = '<div class="load" style="z-index:3000"></div>';
  $('.load').remove();    
  $('body').append(cont);
  setTimeout(function(){
 $('.load').show()
  },200) 
}