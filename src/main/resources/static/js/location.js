$(document).ready(function () {

    $("#province").change(function () {
        const provinceId = $(this).val();
        $("#district").html('<option value="">Quận / Huyện</option>');
        $("#ward").html('<option value="">Phường / Xã</option>');

        if (!provinceId) return;

        $.get("/api/locations/districts", { provinceId }, function (data) {
            data.forEach(d => {
                $("#district").append(`<option value="${d.code}">${d.name}</option>`);
            });
        });
    });

    $("#district").change(function () {
        const districtId = $(this).val();
        $("#ward").html('<option value="">Phường / Xã</option>');

        if (!districtId) return;

        $.get("/api/locations/wards", { districtId }, function (data) {
            data.forEach(w => {
                $("#ward").append(`<option value="${w.code}">${w.name}</option>`);
            });
        });
    });

});
