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
                var clientUrlList = document.getElementById('clientUrls');
                while (clientUrlList.firstChild) {
                    clientUrlList.removeChild(clientUrlList.firstChild);
                }
                data.clientUrls.forEach(clientUrl => {
                    var url = document.createElement('li');
                    var registered = clientUrl.registered;
                    url.innerHTML = `<strong>Client URL</strong> ${clientUrl.clientUrl}   <strong>Registered</strong> ${registered}`;
                    url.style.cursor = 'pointer';
                    clientUrlList.appendChild(url);

                    url.addEventListener('click', function () {
                        url.style.cursor = 'auto';
                        var categoryListContainer = document.createElement('ul');
                        categoryListContainer.style.listStyleType = 'none';
                        var selectedClientUrl = clientUrl.clientUrl;
                        const retrieveUrl = `/domain-category-retrieve?clientUrl=${encodeURIComponent(selectedClientUrl)}`;
                        fetch(retrieveUrl, {
                            method: 'GET'
                        })
                            .then(response => response.json())
                            .then(data => {
                                for (const [category, cntPerCategory] of Object.entries(data.categories)) {
                                    var cat = document.createElement('li');
                                    cat.innerHTML = `${category}  (${cntPerCategory})`;
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
                                                    const domainCells = {};
                                                    // const tbody = document.querySelector('#recommendedDomainTable tbody');
                                                    domainList.forEach(domain => {
                                                        const row = tbody.insertRow();

                                                        const domainCell = row.insertCell();
                                                        domainCell.textContent = domain.recommendedDomain.trim();
                                                        domainCells[domain.recommendedDomain.trim()] = domainCell;

                                                        const usedCell = row.insertCell();
                                                        usedCell.textContent = domain.used;
                                                    });

                                                    copyButton = document.getElementById('copyDomainBtn');
                                                    copyButton.addEventListener('click', () => {
                                                        const domainsToCopy = domainList.map(domain => domain.recommendedDomain).join('\n');
                                                        navigator.clipboard.writeText(domainsToCopy).then(() => {
                                                            console.log('Domains copied to clipboard!');
                                                        }).catch(err => {
                                                            console.error('Error copying text: ', err);
                                                        });
                                                    });

                                                    document.getElementById('urlUploadBtn').addEventListener('click', function() {
                                                        document.getElementById('urlFileInput').click();
                                                    });

                                                    document.getElementById('urlFileInput').addEventListener('change', function(event) {
                                                        const file = event.target.files[0];
                                                        if (file) {
                                                            const formData2 = new FormData();
                                                            formData2.append('clientUrl', clientUrl.clientUrl);
                                                            formData2.append('category', category);
                                                            formData2.append('file', file);
                                                            submitFormToRegisterPageUrl(formData2, domainCells);
                                                        }
                                                    });
                                                    document.getElementById("urlTextUploadBtn").addEventListener("click", function() {
                                                        var text = document.getElementById("newUrls").value;

                                                        var blob = new Blob([text], { type: "text/plain" });
                                                        var formData3 = new FormData();
                                                        formData3.append('clientUrl', clientUrl.clientUrl);
                                                        formData3.append('category', category);
                                                        formData3.append("file", blob, "urls.txt");
                                                        submitFormToRegisterPageUrl(formData3, domainCells);
                                                    });
                                                });
                                        }
                                        else {
                                            table.style.display = 'none';
                                            // copyButton.style.display = 'none';
                                            table = false;
                                        }
                                    });
                                }
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

function submitFormToRegisterPageUrl(formData, domainCells) {
    fetch('/page-register', {
        method: 'POST',
        body: formData,
    })
        .then(response => {
            if(response.ok) {
                return response.json();
            } else {
                throw response;
            }
        })
        .then(data => {
            console.log(data);
            data.forEach(url => {
                let domain = url.split("/")[0];
                if(domainCells[domain]) {
                    domainCells[domain].style.color = 'blue';
                }
            });
        })
        .catch(error => console.error('Error:', error));
}