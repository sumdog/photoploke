$ -> 
  $.get "/admin/listPhotosJson" , (data) ->
    $.each data, (i,photo) ->
      $("#photos").append $('<li>').text photo.name
  $("#fileUploader").fineUploader
    request: 
      endpoint: '/admin/fileUploader'    
