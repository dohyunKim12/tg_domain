document.addEventListener('DOMContentLoaded', function() {
    var form = document.getElementById('client-retrieve-form');
    form.onsubmit = function(event) {
        event.preventDefault(); // 기본 폼 제출 방지
        var newClientUrlRegisterForm = document.getElementById('new-client-url-form');
        if (newClientUrlRegisterForm.style.display === "none") {
            newClientUrlRegisterForm.style.display = "block";
        }
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
                    var registered = clientUrl.registered;
                    url.innerHTML = `<strong>Client URL</strong> ${clientUrl.clientUrl}   <strong>Registered</strong> ${registered}`;
                    url.style.cursor = 'pointer';
                    document.getElementById('clientUrls').appendChild(url);

                    url.addEventListener('click', function () {
                        url.style.cursor = 'auto';
                        var categoryListContainer = document.createElement('ul');
                        categoryListContainer.style.listStyleType = 'none';
                        fetch('/domain-category-retrieve', {
                            method: 'GET'
                        })
                            .then(response => response.json())
                            .then(categories => {
                                categories.forEach(category => {
                                    var cat = document.createElement('li');
                                    cat.textContent = category;
                                    cat.style.cursor = 'pointer';
                                    categoryListContainer.appendChild(cat);
                                    // domain_category 항목에 대한 클릭 이벤트
                                    let table;
                                    let copyButton;
                                    cat.addEventListener('click', function() {
                                        if(!table) {
                                            table = document.getElementById('recommendedDomainTable');
                                            table.style.display = '';
                                            var tableCaption = document.getElementById('clientUrlCaption');
                                            tableCaption.innerHTML = '<strong>' + clientUrl.clientUrl + '</strong>' + " - " + '<strong>' + category + '</strong>';
                                            const tbody = document.querySelector('#recommendedDomainTable tbody');
                                            const dropZone = document.getElementById('dropZone');
                                            tbody.innerHTML = '';
                                            fetch('/domain-recommend', {
                                                method: 'POST',
                                                headers: {
                                                    'Content-Type': 'application/json',
                                                },
                                                body: JSON.stringify({ url: clientUrl.clientUrl, categoryName: category })
                                            })
                                                .then(response => response.json())
                                                .then(domainList => {
                                                    // const tbody = document.querySelector('#recommendedDomainTable tbody');
                                                    domainList.forEach(domain => {
                                                        const row = tbody.insertRow();

                                                        const domainCell = row.insertCell();
                                                        domainCell.textContent = domain.recommendedDomain.trim();

                                                        const usedCell = row.insertCell();
                                                        usedCell.textContent = domain.used;
                                                    });

                                                    // const maxDomainLength = domainList.reduce((max, domain) => Math.max(max, domain.recommendedDomain.length), 0);
                                                    // const maxDomainWidth = maxDomainLength + 3;
                                                    //
                                                    // table = document.createElement('table');
                                                    // table.id = 'recommendedDomainTable';
                                                    // table.style.width = '50%';
                                                    // table.setAttribute('border', '1');
                                                    //
                                                    // var caption = document.createElement('caption');
                                                    // caption.innerHTML = '<strong>' + clientUrl.clientUrl + '</strong>' + " - " + '<strong>' + category + '</strong>';
                                                    // table.appendChild(caption);
                                                    //
                                                    // var thead = document.createElement('thead');
                                                    // var headerRow = document.createElement('tr');
                                                    //
                                                    // var domainHeader = document.createElement('th');
                                                    // domainHeader.textContent = 'Domain';
                                                    // domainHeader.style.width = `${maxDomainWidth}ch`;
                                                    //
                                                    // var usedHeader = document.createElement('th');
                                                    // usedHeader.textContent = 'Used';
                                                    //
                                                    // var newPageUrlHeader = document.createElement('th');
                                                    // newPageUrlHeader.textContent = 'Page URL Successfully Registered';
                                                    // newPageUrlHeader.colSpan = "2";
                                                    //
                                                    // headerRow.appendChild(domainHeader);
                                                    // headerRow.appendChild(usedHeader);
                                                    // headerRow.appendChild(newPageUrlHeader);
                                                    // thead.appendChild(headerRow);
                                                    // table.appendChild(thead);
                                                    //
                                                    // var tbody = document.createElement('tbody');
                                                    //
                                                    // domainList.forEach(domain => {
                                                    //     var row = document.createElement('tr');
                                                    //
                                                    //     var domainCell = document.createElement('td');
                                                    //     domainCell.textContent = domain.recommendedDomain.trim();
                                                    //     domainCell.style.width = `${maxDomainWidth}ch`;
                                                    //
                                                    //     var usedCell = document.createElement('td');
                                                    //     usedCell.textContent = domain.used;
                                                    //
                                                    //     var newPageUrlCell = document.createElement('td');
                                                    //     var newPageUrlInput = document.createElement('input');
                                                    //     newPageUrlInput.id = 'new-page-url-input'
                                                    //     newPageUrlInput.type = 'text';
                                                    //     newPageUrlInput.placeholder = 'Enter new page URL';
                                                    //     newPageUrlInput.addEventListener('keyup', function(event) {
                                                    //         event.preventDefault();
                                                    //         if (event.key === 'Enter') {
                                                    //             registerButton.click();
                                                    //         }
                                                    //     });
                                                    //     newPageUrlCell.appendChild(newPageUrlInput);
                                                    //
                                                    //     var registerButtonCell = document.createElement('td');
                                                    //     var registerButton = document.createElement('button');
                                                    //     registerButton.textContent = 'Register';
                                                    //     registerButton.addEventListener('click', function() {
                                                    //         registerButton.disabled = true;
                                                    //         registerButton.style.opacity = '0.8';
                                                    //         console.log(`Registering ${newPageUrlInput.value} for domain ${domain.recommendedDomain}`);
                                                    //         const postData = {
                                                    //             clientUrl: clientUrl.clientUrl,
                                                    //             domain: domain.recommendedDomain,
                                                    //             category: category,
                                                    //             newPageUrl: newPageUrlInput.value
                                                    //         };
                                                    //         fetch('/page-register', {
                                                    //             method: 'POST',
                                                    //             headers: {
                                                    //                 'Content-Type': 'application/json',
                                                    //             },
                                                    //             body: JSON.stringify(postData)
                                                    //         })
                                                    //             .then(response => {
                                                    //                 if(response.ok) {
                                                    //                     return response.json();
                                                    //                 } else {
                                                    //                     throw response;
                                                    //                 }
                                                    //             })
                                                    //             .then(data => {
                                                    //                 console.log('Registration successful', data);
                                                    //                 registerButton.textContent = 'Success';
                                                    //                 registerButton.style.color = 'green';
                                                    //                 registered = parseInt(registered, 10) + 1;
                                                    //                 url.innerHTML = `<strong>Client URL</strong> ${clientUrl.clientUrl}   <strong>Registered</strong> ${registered}`;
                                                    //             })
                                                    //             .catch(error => {
                                                    //                 registerButton.textContent = 'Failed';
                                                    //                 registerButton.style.color = 'red';
                                                    //                 error.json().then(errMsg => {
                                                    //                     console.error('Error:', errMsg);
                                                    //                     alert(`Error: ${errMsg.message}`);
                                                    //                 });
                                                    //             })
                                                    //     });
                                                    //     registerButtonCell.appendChild(registerButton);
                                                    //     row.appendChild(domainCell);
                                                    //     row.appendChild(usedCell);
                                                    //     row.appendChild(newPageUrlCell);
                                                    //     row.appendChild(registerButtonCell);
                                                    //     tbody.appendChild(row);
                                                    // });
                                                    // table.appendChild(tbody);
                                                    // var parentDomainList = document.getElementById("recommendedDomainList").parentNode;
                                                    // parentDomainList.appendChild(table);
                                                    copyButton = document.getElementById('copyDomainBtn');
                                                    copyButton.addEventListener('click', () => {
                                                        const domainsToCopy = domainList.map(domain => domain.recommendedDomain).join('\n');
                                                        navigator.clipboard.writeText(domainsToCopy).then(() => {
                                                            console.log('Domains copied to clipboard!');
                                                        }).catch(err => {
                                                            console.error('Error copying text: ', err);
                                                        });
                                                    });

                                                    dropZone.addEventListener('dragover', (event) => {
                                                        event.stopPropagation();
                                                        event.preventDefault();
                                                        event.dataTransfer.dropEffect = 'copy'; // 파일이 복사되는 것처럼 보이게 함
                                                    });

                                                    dropZone.addEventListener('drop', (event) => {
                                                        event.stopPropagation();
                                                        event.preventDefault();

                                                        const files = event.dataTransfer.files; // 드롭된 파일들
                                                        const formData2 = new FormData();
                                                        formData2.append('clientUrl', clientUrl.clientUrl);
                                                        formData2.append('category', category);
                                                        if(files.length > 0) {
                                                            formData2.append('file', files[0]);
                                                        }
                                                        fetch('/page-register', {
                                                            method: 'POST',
                                                            body: formData2,
                                                        })
                                                            .then(response => {
                                                                if(response.ok) {
                                                                    return response.json();
                                                                } else {
                                                                    throw response;
                                                                }
                                                            })
                                                            .then(data => {
                                                                console.log(data); // 여기서 data는 pageUrlList에 해당하는 배열입니다.
                                                                // 이제 data를 원하는 대로 활용할 수 있습니다. 예를 들어, 페이지에 표시하거나 추가 처리를 할 수 있습니다.
                                                            })
                                                            .catch(error => console.error('Error:', error));
                                                        //             .then(data => {
                                                        //                 console.log('Registration successful', data);
                                                        //                 registerButton.textContent = 'Success';
                                                        //                 registerButton.style.color = 'green';
                                                        //                 registered = parseInt(registered, 10) + 1;
                                                        //                 url.innerHTML = `<strong>Client URL</strong> ${clientUrl.clientUrl}   <strong>Registered</strong> ${registered}`;
                                                        //             })
                                                        //             .catch(error => {
                                                        //                 registerButton.textContent = 'Failed';
                                                        //                 registerButton.style.color = 'red';
                                                        //                 error.json().then(errMsg => {
                                                        //                     console.error('Error:', errMsg);
                                                        //                     alert(`Error: ${errMsg.message}`);
                                                        //                 });
                                                        //             })


                                                        fetch('/page-register', {
                                                            method: 'POST'
                                                        })
                                                        // 여기서 파일 처리 로직을 구현하세요. 예: 파일을 서버로 업로드
                                                    });
                                                });
                                        }
                                        else {
                                            table.style.display = 'none';
                                            // copyButton.style.display = 'none';
                                            table = false;
                                        }
                                    });
                                })
                                var parent = url.parentNode;
                                if(url.nextSibling) {
                                    parent.insertBefore(categoryListContainer, url.nextSibling);
                                } else {
                                    parent.appendChild(categoryListContainer);
                                }
                            })
                    }, {once: true})
                })
            })
            .catch(error => console.error('Error:', error));
    };

    var newClientUrlRegisterForm = document.getElementById('new-client-url-form');
    newClientUrlRegisterForm.onsubmit = function(e) {
        e.preventDefault();
        var clientUrlRegisterParam = new FormData();
        var clientNameValue = document.getElementById("client-name").value;
        var inputVal = document.getElementById('newClientUrlInput').value;
        clientUrlRegisterParam.append('client-name', clientNameValue);
        clientUrlRegisterParam.append('new-client-url', inputVal);
        fetch('/new-client-url', {
            method: 'POST',
            body: clientUrlRegisterParam
        })
            .then(response => {
                if(response.ok) {
                    console.log('New client URL submitted successfully');
                    window.location.reload();
                }
            })
            .catch(error => console.error('Error:', error));

    }
});

function closeModal() {
    document.getElementById('clientRetrieveSuccessModal').style.display = 'none';
}

// function addRowsToTable(domainList) {
//     const tbody = document.querySelector('#recommendedDomainTable tbody');
//
//     domainList.forEach(domain => {
//         const row = document.createElement('tr');
//
//         const domainCell = document.createElement('td');
//         domainCell.textContent = domain.recommendedDomain.trim();
//     }
// }