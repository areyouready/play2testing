$(document).ready(function() {
  $('input.filter').on('keyup', function() {
    var rex2 = { "filterBox": $(this).val()};
    console.log(rex2);
    $.getJSON('/jsonDiscs',rex2,
      	function(response) {
          console.log(response);
          $('#discTableBody').empty();
          for (var i in response) {
          $('#discTable > tbody:first')
            .append($('<tr>')
              .append($('<td>').text(response[i].title))
              .append($('<td>', {class: 'col-sm-2'})
                .append($('<input class="deleteButton" type="submit" value="Delete" id='+response[i].id+' >')
              )
            )
          );

          $('.deleteButton').click(function() {
            var id = $(this).attr('id');
            $(this).parents('tr').hide();
            $.post('/discs/'+id+'/delete')

            });

        }
          })
        }
      );

    setTimeout(function(){
        if ($('.alert').length > 0) {
            $('.alert').fadeOut(function() { $(this).remove(); });
        }
    }, 5000)
  });
