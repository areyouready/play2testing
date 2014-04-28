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
              .append($('<td>').text(response[i].label))
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
  });
