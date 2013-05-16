$ -> 
  $("#fileUploader").fineUploader(
    request: 
      endpoint: '/admin/photos/upload'
  ).on "complete", (event, id, name, responseJSON) ->
    console.log("my object: %o", responseJSON)
    $("#photos").append $('<li>').text responseJSON.location
