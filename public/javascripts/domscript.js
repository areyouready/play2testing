$(document).ready(function() {
  $('input.filter').on('keyup', function() {
    var rex2 = { "filter": $(this).val()};
    console.log(rex2);
    $.getJSON('/couchDiscs',rex2,
      	function(response) {
            $('#discCount').text(response.totalRows + " disc(s)")
          console.log(response.discs);
            var lastEl;
            if(response.discs.length > 10) {
                //TODO if lastEl != null activate paging
                lastEl = response.discs.pop();
                $('.next.disabled').removeClass('disabled')
                $('.next, a').attr("href", "/discs?filter=" + rex2.filter + "&nxtFilter=" + lastEl.title + "&page=1")

            } else {
                $('.next, .previous').addClass('disabled')
            }
          $('#discTableBody').empty();
          for (var i in response.discs) {
          $('#discTable > tbody:first')
            .append($('<tr>')
              .append($('<td>').text(response.discs[i].title))
              .append($('<td>', {class: 'col-sm-2'})
                .append($('<input class="deleteButton" type="submit" value="Delete" id='+response.discs[i].id+' >')
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
