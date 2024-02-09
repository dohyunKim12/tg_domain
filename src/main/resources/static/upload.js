document.addEventListener('DOMContentLoaded', function() {
    var form = document.getElementById('file-upload-form');
    form.onsubmit = function(event) {
        event.preventDefault(); // 기본 폼 제출 방지

        var formData = new FormData(form);
        showLoadingIcon();
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
                hideLoadingIcon();
            })
            .catch(error => {
                console.error('Error:', error)
                hideLoadingIcon();
            });
    };
});

function closeModal() {
    document.getElementById('uploadSuccessModal').style.display = 'none';
}

function truncateDomain() {
    fetch('/domain_truncate', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => response.json())
        .then(data => {
            alert(data.message);
        })
        .catch(error => console.error('Error:', error));
}

function showLoadingIcon() {
    document.getElementById('loadingIcon').style.display = 'block';
}

function hideLoadingIcon() {
    document.getElementById('loadingIcon').style.display = 'none';
}