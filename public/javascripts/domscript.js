$(document).ready(function() {
    $('input.filter').on('keyup', function() {
            var rex2 = { "filter": $(this).val()};
            console.log(rex2);
            $.getJSON('/discs',rex2,
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
                        $('.next, a').attr("href", "#")
                    }
                    $('#discTableBody').empty();
                    for (var i in response.discs) {
                        $('#discTable > tbody:first')
                            .append($('<tr>')
                                .append($('<td>').text(response.discs[i].title))
                                .append($('<td>', {class: 'col-sm-2'})
                                    .append($('<span style="float:left">')
                                        .append($('<input class="btn-table deleteButton" type="submit" value="Delete" id='+i+' >'))
                                )
                                    .append($('<span>')
                                        .append($('<input class="btn-table editButton" type="submit" value="Edit" id='+i+' >'))
                                )
                            )
                        );

                        $('.deleteButton').click(function() {
                            var id = response.discs[$(this).attr('id')].id
                            var rev = response.discs[$(this).attr('id')].rev
                            $(this).parents('tr').hide();
                            $('#discCount').text((response.totalRows-1) + " disc(s)")
                            $.post('/discs/'+id+'/'+rev+'/delete')

                        });


                        $('.editButton').click(function() {
                            var id = response.discs[$(this).attr('id')].id
                            var rev = response.discs[$(this).attr('id')].rev
                            var title = response.discs[$(this).attr('id')].title
                            console.log(rev)
                            $('#editTitle').val(title)

//            $.post('/discs/'+id+'/'+rev+'/'+title+'/edit')

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
