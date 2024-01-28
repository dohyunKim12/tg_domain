document.addEventListener('DOMContentLoaded', function() {
    var form = document.getElementById('file-upload-form');
    form.onsubmit = function(event) {
        event.preventDefault(); // 기본 폼 제출 방지

        var formData = new FormData(form);
        fetch('/domain-upload', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (data.message === 'uploadSuccess') {
                    console.log("Uplaod Success, display modal");
                    document.getElementById('uploadSuccessModal').style.display = 'block';
                } else {
                    console.error("Error occurred while upload file");
                }
            })
            .catch(error => console.error('Error:', error));
    };
});

function closeModal() {
    document.getElementById('uploadSuccessModal').style.display = 'none';
}