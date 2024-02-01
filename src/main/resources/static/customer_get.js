document.addEventListener('DOMContentLoaded', function() {
    var form = document.getElementById('customer-retrieve-form');
    form.onsubmit = function(event) {
        event.preventDefault(); // 기본 폼 제출 방지

        var formData = new FormData(form);
        fetch('/domain-register', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(text => {
                var data = JSON.parse(text);
                var customerName = data.customer_name;
                console.log(customerName);
                data.requestList.forEach(request => {
                    console.log(request.customerId);
                    console.log(request.pageUrl);
                });
                data.requestList.forEach(request => {
                    request.domainList.forEach(domain => {
                        console.log(domain);
                    });
                });

                data.requestList.forEach(request => {
                    // 각 요청에 대한 리스트 아이템 생성
                    var listItem = document.createElement('li');
                    listItem.textContent = `Customer ID: ${request.customerId}, Page URL: ${request.pageUrl}`;

                    // domainList 처리
                    var domainList = document.createElement('ul');
                    request.domainList.forEach(domain => {
                        var domainListItem = document.createElement('li');
                        domainListItem.textContent = domain; // domain 객체의 필드에 따라 수정
                        domainList.appendChild(domainListItem);
                    });

                    listItem.appendChild(domainList);
                    document.getElementById('requestList').appendChild(listItem);
                });

                data.recommendedDomainList.forEach(domain => {
                    console.log(domain)
                    var listItem = document.createElement('li');
                    listItem.textContent = `Recommended Domain : ${domain.domain}`;
                    document.getElementById('recommendedDomainList').appendChild(listItem);
                });
                // 고객 이름 설정
                document.getElementById('customerName').textContent = `Customer Name: ${data.customer_name}`;

            })
            .catch(error => console.error('Error:', error));
    };
});

function closeModal() {
    document.getElementById('customerRetrieveSuccessModal').style.display = 'none';
}