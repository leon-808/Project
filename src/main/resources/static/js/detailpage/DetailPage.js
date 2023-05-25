$(document)
.ready(function() {
    loadFoodDetails();
    uploadimage();
    
})
.on('click','#btnSubmit',reviewInsert)
.on('')


function loadFoodDetails() {
	var primecode =window.location.pathname.split('/').pop();
    $.ajax({
        url: '/restaurant/detail/' + primecode,
        type: 'post',
        dataType: 'json',
        success: function(response) {
            var restaurant = response[0];

            $('#rImg').attr('src', restaurant.rphoto);
            $('#rName').val(restaurant.rname);
            $('#rCategory').val(restaurant.category);
            $('#rReviewN').val(restaurant.reviewCount);
            $('#rAddress').val(restaurant.address);
            $('#rPhone').val(restaurant.rphone);
            ar=restaurant.menu.split('/');        
            menuConcatenated="";
            for(var i=0;i<ar.length;i++){
				var menuPart = ar[i].trim();
				menuConcatenated += menuPart + "\n";				
			}
			
            $('#rMenu').val(menuConcatenated);      
            $('#rTage').val(restaurant.goodPoints);

           
        },
        error: function() {
            console.log('음식상세정보를 불러오지 못했습니다.');
        }
    });
}

function uploadimage(){
	const fileInput = document.getElementById("fileUpload");

	const handleFiles = (e) => {
  	const selectedFile = [...fileInput.files];
	const fileReader = new FileReader();
	
	fileReader.readAsDataURL(selectedFile[0]);	
	
		fileReader.onload = function () {
	  		document.getElementById("previewImg").src = fileReader.result;
		};
	
	};
	
	fileInput.addEventListener("change", handleFiles);
	$('input[tpye=radio]').prop('checked', false);
}

function reviewCheckId(){
	$.ajax({
		url:"/review/checkid",
		type:"post",
		dataType:"text",
		success:function(logininfo){
			if(logininfo !=""){
				$("#name").val(logininfo);
			}
		}
	})
}

function reviewInsert(){
	var primecode =window.location.pathname.split('/').pop();
	var selectedTags = $('input[type="checkbox"]:checked').map(function(){
		return this.value;
	}).get();
	
	$.ajax({
		url:"/review/insert",
		type:"post",
		dataType:"text",
		data:{
			primecode:primecode,
			id:$('#name').val(),
			photo:$('#fileUpload').val(),
			tags:selectedTags,
			detail:$('#myreview').val(),
		},
		beforeSend:function(){
			var content = $('#myreview').val();
			//var tags:$('#tags').val();
			
			if(content ==''){
				alert('내용을 입력하세요');
				return false;
			}/*else if(tags==''){
				alert('태그를 하나라도 입력하세요');
				return false;
			}*/
		},
		success:function(data){
			if(data=="ok"){
				
			}else{
				alert("리뷰 등록 실패");
			}
		}
	})
}