document.addEventListener('DOMContentLoaded', function() {
    var form = document.getElementById('customer-retrieve-form');
    form.onsubmit = function(event) {
        event.preventDefault(); // 기본 폼 제출 방지

        var formData = new FormData(form);
        fetch('/domain-register', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (data.message === 'success') {
                    console.log("Retrieve Success, display modal");
                    document.getElementById('customerRetrieveSuccessModal').style.display = 'block';
                } else {
                    console.error("Error occurred while customer retrieve file");
                }
            })
            .catch(error => console.error('Error:', error));
    };
});

function closeModal() {
    document.getElementById('customerRetrieveSuccessModal').style.display = 'none';
}