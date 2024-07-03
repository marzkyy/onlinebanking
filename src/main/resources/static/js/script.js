$(document).ready(function() {
    $('#recipientAccountNumber').autocomplete({
        source: function(request, response) {
            $.ajax({
                url: '/users/search',
                data: { term: request.term },
                success: function(data) {
                    response(data.map(user => ({
                        label: user.email,
                        value: user.id
                    })));
                }
            });
        },
        select: function(event, ui) {
            $('#recipientAccountNumber').val(ui.item.label);
            $('#recipientAccountId').val(ui.item.value);
            return false;
        }
    });

    // Confirm before form submission
    $('form').on('submit', function(e) {
        var amount = $('input[name="amount"]').val();
        var recipientEmail = $('#recipientAccountNumber').val();
        var recipientId = $('#recipientAccountId').val();

        if (confirm(`Are you sure you want to transfer ₱${amount} to ${recipientEmail}?`)) {
            // User clicked OK
            return true;
        } else {
            // User clicked Cancel
            e.preventDefault();
            return false;
        }
    });
});
