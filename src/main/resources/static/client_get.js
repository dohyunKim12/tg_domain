document.addEventListener('DOMContentLoaded', function() {
    var form = document.getElementById('client-retrieve-form');
    form.onsubmit = function(event) {
        event.preventDefault(); // 기본 폼 제출 방지

        var formData = new FormData(form);
        fetch('/client-retrieve', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(text => {
                console.log(text);
                var data = JSON.parse(text);
                data.clientUrls.forEach(clientUrl => {
                    var url = document.createElement('li');
                    url.textContent = `Client URL: ${clientUrl.clientUrl}, Registered: ${clientUrl.registered}`;
                    url.style.cursor = 'pointer';
                    document.getElementById('clientUrls').appendChild(url);

                    url.addEventListener('click', function () {
                        var categoryListContainer = document.createElement('ul');
                        categoryListContainer.style.listStyleType = 'none'; // 스타일 선택적 적용
                        fetch('/domain-category-retrieve', {
                            method: 'GET'
                        })
                            .then(response => response.json())
                            .then(categories => {
                               categories.forEach(category => {
                                   var item = document.createElement('li');
                                   item.textContent = category;
                                   item.style.cursor = 'pointer';
                                   categoryListContainer.appendChild(item);
                                   // domain_category 항목에 대한 클릭 이벤트
                                   item.addEventListener('click', function() {
                                       fetch('/domain-recommend', {
                                           method: 'POST',
                                           headers: {
                                               'Content-Type': 'application/json',
                                           },
                                           body: JSON.stringify({ url: clientUrl.clientUrl, categoryName: category })
                                       })
                                           .then(/* recommend list 출력 */);
                                   });
                               })
                                var parent = url.parentNode;
                               if(url.nextSibling) {
                                   parent.insertBefore(categoryListContainer, url.nextSibling);
                               } else {
                                   parent.appendChild(categoryListContainer);
                               }
                            })
                    })
                })

                // 새 폼 생성 -> 이건 html 로 옮겨야할듯
                var newForm = document.createElement('form');
                newForm.id = 'new-client-url-form';
                // 입력 필드 생성
                var input = document.createElement('input');
                input.type = 'text';
                input.name = 'newClientUrl';
                input.placeholder = 'New Client URL';
                // 제출 버튼 생성
                var submitButton = document.createElement('button');
                submitButton.type = 'submit';
                submitButton.textContent = 'Submit';
                // 폼에 입력 필드와 버튼 추가
                newForm.appendChild(input);
                newForm.appendChild(submitButton);
                // 페이지에 폼 추가
                document.body.appendChild(newForm);
                // 새 폼의 제출 이벤트 처리
                newForm.onsubmit = function(e) {
                    e.preventDefault();
                    var clientNameValue = document.getElementById("client-name").value;
                    var newFormData = new FormData(newForm);
                    newFormData.append('client-name', clientNameValue);
                    newFormData.append('new-client-url', input.value);
                    // 여기에 새 클라이언트 URL을 처리하는 로직 추가
                    // 예: fetch 요청을 사용하여 서버에 데이터 전송
                    fetch('/new-client-url', {
                        method: 'POST',
                        body: newFormData
                    })
                        .then(response => {
                            if(response.ok) {
                                console.log('New client URL submitted successfully');
                                // 성공적으로 처리된 후 페이지 새로고침
                                window.location.reload();
                            }
                        })
                        .catch(error => console.error('Error:', error));
                };
            })
            .catch(error => console.error('Error:', error));
    };
});

// document.addEventListener('DOMContentLoaded', function() {
//     var form = document.getElementById('client-retrieve-form');
//     form.onsubmit = function(event) {
//         event.preventDefault(); // 기본 폼 제출 방지
//
//         var formData = new FormData(form);
//         fetch('/domain-register', {
//             method: 'POST',
//             body: formData
//         })
//             .then(response => response.text())
//             .then(text => {
//                 var data = JSON.parse(text);
//                 var clientName = data.client_name;
//                 console.log(clientName);
//                 data.requestList.forEach(request => {
//                     console.log(request.clientId);
//                     console.log(request.pageUrl);
//                 });
//                 data.requestList.forEach(request => {
//                     request.domainList.forEach(domain => {
//                         console.log(domain);
//                     });
//                 });
//
//                 data.requestList.forEach(request => {
//                     // 각 요청에 대한 리스트 아이템 생성
//                     var listItem = document.createElement('li');
//                     listItem.textContent = `Client ID: ${request.clientId}, Page URL: ${request.pageUrl}`;
//
//                     // domainList 처리
//                     var domainList = document.createElement('ul');
//                     request.domainList.forEach(domain => {
//                         var domainListItem = document.createElement('li');
//                         domainListItem.textContent = domain; // domain 객체의 필드에 따라 수정
//                         domainList.appendChild(domainListItem);
//                     });
//
//                     listItem.appendChild(domainList);
//                     document.getElementById('requestList').appendChild(listItem);
//                 });
//
//                 data.recommendedDomainList.forEach(domain => {
//                     console.log(domain)
//                     var listItem = document.createElement('li');
//                     listItem.textContent = `Recommended Domain : ${domain.domain}`;
//                     document.getElementById('recommendedDomainList').appendChild(listItem);
//                 });
//                 // 고객 이름 설정
//                 document.getElementById('clientName').textContent = `Client Name: ${data.client_name}`;
//
//             })
//             .catch(error => console.error('Error:', error));
//     };
// });

function closeModal() {
    document.getElementById('clientRetrieveSuccessModal').style.display = 'none';
}