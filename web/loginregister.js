/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function submitFormAjax(actionUrl, $statusElem, redirUrl) {
    $statusElem.removeClass('error').removeClass('success');
    showLoader($statusElem);
    var jqxhr = $.ajax({
        url: actionUrl,
        type: 'POST',
        data: $('form').serialize(),
        timeout: 10000
    });
    jqxhr.always(function() {
        hideLoader($statusElem);
    });
    jqxhr.fail(function (req) {
        $statusElem.addClass('error');
        $statusElem.html(req.responseText);
    });
    jqxhr.done(function (body) {
        var txt = 'Success. Redirecting...';
        $('#status').addClass('success').html(txt);
        setTimeout(function () {
            window.location.replace(redirUrl);
        }, 3000);
    });
}

function showLoader($elem) {
    $('#status').html('<img src="images/loading.gif" />');
}

function hideLoader($elem) {
    $('#status > img').remove();
}
