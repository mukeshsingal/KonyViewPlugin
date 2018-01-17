$('[data-toggle="tooltip"]').tooltip();

 $('.hook-items li').on('mouseover',function(){
    $(this).children().find('.icon-set').removeClass('d-none');
})
$('.hook-items li').on('mouseout',function(){
    $(this).find('.icon-set').addClass('d-none')
})

var resetHook = function() {
    var findParent = $(e.target).parents('.normal-state');
    findParent.find('.icon-edit-set').addClass('d-none');
    $(this).parent().prev().prev().addClass('icon-set').removeClass('d-none');
    $(this).parents('.ui-state-default').removeClass('disabled').addClass('active');
    var eee = findParent.find('.hook-label').text();
    findParent.find('.hook-label').removeClass('d-none');
    findParent.find('.hook-edit').addClass('d-none');
}
$('.hook-edit input').on('focus',function(){
        if(!$(this).data('defaultText')) $(this).data('defaultText', $(this).val());
        if($(this).val()==$(this).data('defaultText', $(this).val('')));
})
$('.icon-disable-hook').on('click',function(e){
    var findParent = $(e.target).parents('.normal-state');
    findParent.find('.icon-disable-set').removeClass('d-none');
    $(this).parent().removeClass('icon-set').addClass('d-none');
    $(this).parents('.ui-state-default').addClass('disabled').removeClass('active');
})
$('.icon-edit-hook').on('click',function(e){
    var findParent = $(e.target).parents('.normal-state');
    findParent.find('.icon-edit-set').removeClass('d-none');
    $(this).parent().removeClass('icon-set').addClass('d-none');
    $(this).parents('.ui-state-default').addClass('disabled').removeClass('active');
    var txtLabel = findParent.find('.hook-label').text();
    findParent.find('.hook-label').addClass('d-none');
    findParent.find('.hook-edit').removeClass('d-none');
    findParent.find('.hook-edit input').val(txtLabel).focus().get(0).setSelectionRange(0,0);
})

$('.icon-commit-hook').on('click',function(e){
    var findParent = $(e.target).parents('.normal-state');
    
    
    var inputValue = findParent.find('.hook-edit input').val();
    if(inputValue.length == 0){
        findParent.find('.hook-edit input').addClass('inline-error').attr('placeholder','Enter hook name');
    } else {
        findParent.find('.icon-edit-set').addClass('d-none');
        $(this).parent().prev().prev().addClass('icon-set').removeClass('d-none');
        $(this).parents('.ui-state-default').removeClass('disabled').addClass('active');
        findParent.find('.hook-label').removeClass('d-none');
        findParent.find('.hook-edit').addClass('d-none');
        findParent.find('.hook-label').text(inputValue);
        }
    
})
$('.icon-cancel-hook').on('click',function(e){
    var findParent = $(e.target).parents('.normal-state');
    findParent.find('.icon-edit-set').addClass('d-none');
    $(this).parent().prev().prev().addClass('icon-set').removeClass('d-none');
    $(this).parents('.ui-state-default').removeClass('disabled').addClass('active');
    var eee = findParent.find('.hook-label').text();
    findParent.find('.hook-label').removeClass('d-none');
    findParent.find('.hook-edit').addClass('d-none');
})
$('.icon-reset-hook').on('click',function(e){
    var findParent = $(e.target).parents('.normal-state');
    $(this).parent().addClass('d-none');
    $(this).parent().prev().addClass('icon-set').addClass('d-none');
    $(this).parents('.ui-state-default').removeClass('disabled').addClass('active');
})
$('.icon-delete-hook').on('click',function(e){
    var fintParent = $(e.target).parents('.hook-point-container');
    $(this).parents('.ui-state-default').addClass('hook-delete');
    fintParent.find(".confirm-delete").fadeIn();
})
$('.cancel-delete').on('click',function(){
    $(this).parents('.hook-point-container').find('.disabled').removeClass('hook-delete');
    $(".confirm-delete").fadeOut();
})
$('.delete-confirm').on('click',function(e){
    var fintParent = $(e.target).parents('.hook-point-container');
    fintParent.find('.disabled').hide('slide', {direction: 'left'}, 1000);
    $(".confirm-delete").fadeOut();
    if ( $(fintParent).find('li.active:visible').length == 0 ) {
        setTimeout(function () {
             fintParent.find(".no-hook").removeClass('d-none');
        }, 1000);
    }
})
$('.show-modal').on('click',function(){
    $('#upload-hook').modal('show'); 
    $('.show-upload').removeClass('d-none');
    $('.uploaded-hook-item').addClass('d-none');
    $('.input-hook').val('');
    $('.validation').addClass('d-none');
    $(".confirm-save").attr('disabled', 'disabled');
})
$('.show-upload').on('click',function(){
    $('#fileupload').trigger('click');
    $(this).addClass('d-none');
    $('.uploaded-hook-item').removeClass('d-none')
    $('.validation').addClass('d-none');
})
$('.remove-hook').on('click',function(){
    $('.show-upload').removeClass('d-none')
    $('.uploaded-hook-item').addClass('d-none')
    $('.validation').addClass('d-none');
})
$('.confirm-save').on('click',function(){
    var elementVisible = $('.uploaded-hook-item');
    if($(elementVisible).hasClass('d-none')){
       $('.validation').removeClass('d-none');
    } else {
        $('#upload-hook').modal('hide'); 
        $('#service-created').html('<div class="ml-65"><i class="icon32 icon-success-message di-block v-middle mr-5"></i> Hook Uploaded Successfully  <button type="button" class="close close-toast" data-dismiss="alert">×</button></div> ').animate({'margin-bottom': 0}, 200);
        setTimeout(function () {
            $('#service-created').animate({'margin-bottom': -55}, 200);
        }, 3 * 2000);
    }

})
$('#service-created').on('click','.close-toast',function(){
        $('#service-created').animate({'margin-bottom': -55}, 200);
})

$(".input-hook").keyup(function () {
        if ($(".input-hook").val().length > 0) {
            $(".confirm-save").removeAttr('disabled');
        }
    });
$(".input-hook").blur(function () {
    if ($(".input-hook").val().replace(/\s{1,}/g, "").length == 0) {
        $(".confirm-save").attr('disabled', 'disabled');
    }
});
$('.active').on('mouseenter', ".hook-label", function() {
     var $this = $(this);
     if(this.offsetWidth < this.scrollWidth && !$this.attr('title')) {
          $this.tooltip({
               title: $this.text(),
               placement: "bottom",
               container :'body'
          });
          $this.tooltip('show');
     }
});
$('.icon-edit-hook').prop('title', 'Edit Hook Name');
$('.icon-disable-hook').prop('title', 'Disable Hook');
$('.icon-commit-hook').prop('title', 'Cofirm Edit');
$('.icon-cancel-hook').prop('title', 'Cancel Edit');
$('.icon-reset-hook').prop('title', 'Enable Hook');
$('.icon-delete-hook').prop('title', 'Delete Hook');
// $('.icon-edit-hook').tooltip({title: "Edit", placement: "bottom"}); 
// $('.icon-disable-hook').tooltip({title: "Disable", placement: "bottom",width: "100px"}); 
