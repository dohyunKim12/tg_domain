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
                    url.innerHTML = `<strong>Client URL</strong> ${clientUrl.clientUrl}   <strong>Registered</strong> ${clientUrl.registered}`;
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
                                    let table, copyButton;
                                    cat.addEventListener('click', function() {
                                        if(!table) {
                                            var recommendedDomListContainer = document.createElement('ul');
                                            recommendedDomListContainer.style.listStyleType = 'none';
                                            fetch('/domain-recommend', {
                                                method: 'POST',
                                                headers: {
                                                    'Content-Type': 'application/json',
                                                },
                                                body: JSON.stringify({ url: clientUrl.clientUrl, categoryName: category })
                                            })
                                                .then(response => response.json())
                                                .then(domainList => {
                                                    const maxDomainLength = domainList.reduce((max, domain) => Math.max(max, domain.recommendedDomain.length), 0);
                                                    const maxDomainWidth = maxDomainLength + 3;

                                                    table = document.createElement('table');
                                                    table.id = 'recommendedDomainTable';
                                                    table.style.width = '50%';
                                                    table.setAttribute('border', '1');

                                                    var thead = document.createElement('thead');
                                                    var headerRow = document.createElement('tr');

                                                    var domainHeader = document.createElement('th');
                                                    domainHeader.textContent = 'Domain';
                                                    domainHeader.style.width = `${maxDomainWidth}ch`;

                                                    var usedHeader = document.createElement('th');
                                                    usedHeader.textContent = 'Used';

                                                    var newPageUrlHeader = document.createElement('th');
                                                    newPageUrlHeader.textContent = 'New Page URL to Register';
                                                    newPageUrlHeader.colSpan = "2";

                                                    headerRow.appendChild(domainHeader);
                                                    headerRow.appendChild(usedHeader);
                                                    headerRow.appendChild(newPageUrlHeader);
                                                    thead.appendChild(headerRow);
                                                    table.appendChild(thead);

                                                    var tbody = document.createElement('tbody');

                                                    domainList.forEach(domain => {
                                                        var row = document.createElement('tr');

                                                        var domainCell = document.createElement('td');
                                                        domainCell.textContent = domain.recommendedDomain.trim();
                                                        domainCell.style.width = `${maxDomainWidth}ch`;

                                                        var usedCell = document.createElement('td');
                                                        usedCell.textContent = domain.used;

                                                        var newPageUrlCell = document.createElement('td');
                                                        var newPageUrlInput = document.createElement('input');
                                                        newPageUrlInput.id = 'new-page-url-input'
                                                        newPageUrlInput.type = 'text';
                                                        newPageUrlInput.placeholder = 'Enter new page URL';
                                                        newPageUrlInput.addEventListener('keyup', function(event) {
                                                            event.preventDefault();
                                                            if (event.key === 'Enter') {
                                                                registerButton.click();
                                                            }
                                                        });
                                                        newPageUrlCell.appendChild(newPageUrlInput);

                                                        var registerButtonCell = document.createElement('td');
                                                        var registerButton = document.createElement('button');
                                                        registerButton.textContent = 'Register';
                                                        registerButton.addEventListener('click', function() {
                                                            registerButton.disabled = true;
                                                            registerButton.style.opacity = '0.8';
                                                            console.log(`Registering ${newPageUrlInput.value} for domain ${domain.recommendedDomain}`);
                                                            const postData = {
                                                                clientUrl: clientUrl.clientUrl,
                                                                domain: domain.recommendedDomain,
                                                                category: category,
                                                                newPageUrl: newPageUrlInput.value
                                                            };
                                                            fetch('/page-register', {
                                                                method: 'POST',
                                                                headers: {
                                                                    'Content-Type': 'application/json',
                                                                },
                                                                body: JSON.stringify(postData)
                                                            })
                                                                .then(response => {
                                                                    if(response.ok) {
                                                                        return response.json();
                                                                    } else {
                                                                        throw new Error('Something went wrong');
                                                                    }
                                                                })
                                                                .then(data => {
                                                                    console.log('Registration successful', data);
                                                                    registerButton.textContent = 'Success';
                                                                    registerButton.style.color = 'green';
                                                                })
                                                                .catch(error => {
                                                                    console.error('Error:', error);
                                                                    registerButton.textContent = 'Failed';
                                                                    registerButton.style.color = 'red';
                                                                });
                                                        });
                                                        registerButtonCell.appendChild(registerButton);
                                                        row.appendChild(domainCell);
                                                        row.appendChild(usedCell);
                                                        row.appendChild(newPageUrlCell);
                                                        row.appendChild(registerButtonCell);
                                                        tbody.appendChild(row);
                                                    });
                                                    table.appendChild(tbody);
                                                    var parentCat = cat.parentNode;
                                                    if(cat.nextSibling) {
                                                        parentCat.insertBefore(table, cat.nextSibling);
                                                    } else {
                                                        parentCat.appendChild(table);
                                                    }
                                                    // add domainList copy button
                                                    copyButton = document.createElement('button');
                                                    copyButton.textContent = 'Copy Domains';
                                                    copyButton.id = 'copyButton';
                                                    copyButton.style.display = 'block';
                                                    copyButton.style.marginTop = '10px';
                                                    copyButton.style.marginBottom = '10px';
                                                    copyButton.addEventListener('click', () => {
                                                        const domainsToCopy = domainList.map(domain => domain.recommendedDomain).join('\n');
                                                        navigator.clipboard.writeText(domainsToCopy).then(() => {
                                                            console.log('Domains copied to clipboard!');
                                                            alert('Domains copied to clipboard!');
                                                        }).catch(err => {
                                                            console.error('Error copying text: ', err);
                                                        });
                                                    });
                                                    parentCat.insertBefore(copyButton, table.nextSibling);
                                                });
                                        }
                                        else {
                                            if (table.style.display === 'none') {
                                                table.style.display = '';
                                                copyButton.style.display = 'block';
                                            } else {
                                                table.style.display = 'none';
                                                copyButton.style.display = 'none';
                                            }
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

                // 새 폼 생성 -> 이건 html 로 옮겨야할듯
                var newForm = document.createElement('form');
                newForm.id = 'new-client-url-form';
                var input = document.createElement('input');
                input.type = 'text';
                input.name = 'newClientUrl';
                input.placeholder = 'New Client URL';
                var submitButton = document.createElement('button');
                submitButton.type = 'submit';
                submitButton.textContent = 'Submit';
                newForm.appendChild(input);
                newForm.appendChild(submitButton);
                document.body.appendChild(newForm);
                newForm.onsubmit = function(e) {
                    e.preventDefault();
                    var clientNameValue = document.getElementById("client-name").value;
                    var newFormData = new FormData(newForm);
                    newFormData.append('client-name', clientNameValue);
                    newFormData.append('new-client-url', input.value);
                    fetch('/new-client-url', {
                        method: 'POST',
                        body: newFormData
                    })
                        .then(response => {
                            if(response.ok) {
                                console.log('New client URL submitted successfully');
                                window.location.reload();
                            }
                        })
                        .catch(error => console.error('Error:', error));
                };
            })
            .catch(error => console.error('Error:', error));
    };
});

function closeModal() {
    document.getElementById('clientRetrieveSuccessModal').style.display = 'none';
}