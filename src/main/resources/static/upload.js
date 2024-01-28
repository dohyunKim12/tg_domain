$(document).ready(function() {
    $('#uploadForm').submit(function(event) {
        event.preventDefault(); // 폼 기본 제출 막기
        var formData = new FormData(this);

        $.ajax({
            url: '/domain-upload',
            type: 'POST',
            data: formData,
            processData: false, // 필수
            contentType: false, // 필수
            success: function(response) {
                // 모달 표시
                $('#successModal').show();
                // 추가적으로, 응답에 따른 메시지나 처리를 여기에 작성
            },
            error: function() {
                // 오류 처리
            }
        });
    });
});
