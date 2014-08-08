$(document).ready(function() {
    $('input.filter').live('keyup', function() {
            var rex2 = $(this).val();
            console.log('angekommen');
            $.get('/jsonDiscs', rex2,
                function(data) {
                    console.log("mehr angekommen");
                    window.alert(data);
                });
        }
    )
});