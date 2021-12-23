const listContainer = document.querySelector('#service-list');
var tr = document.createElement("tr");
tr.style.fontWeight= "bold";
var td1 = document.createElement("td");
    var td2 = document.createElement("td");
    var td3 = document.createElement("td");
    td1.appendChild(document.createTextNode("Url" ));
    td2.appendChild(document.createTextNode("Name" ));
    td3.appendChild(document.createTextNode("Status" ));
    tr.appendChild(td1);
    tr.appendChild(td2);
    tr.appendChild(td3);
    listContainer.appendChild(tr);
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) {
 return response.json();
})
.then(function(serviceList) {
  serviceList.forEach(service => {
    var tr = document.createElement("tr");
    var td1 = document.createElement("td");
    td1.style.paddingRight = "10px";
    var td2 = document.createElement("td");
    td2.style.paddingRight = "10px";
    var td3 = document.createElement("td");
    td3.style.paddingRight = "10px";
    var td4 = document.createElement("td");
    td4.style.paddingRight = "10px";
    td2.appendChild(document.createTextNode(service.name ));
    td3.appendChild(document.createTextNode(service.status ));
    if(service.status == "OK"){
        td3.style.color = "green";
    }else if(service.status == "FAIL"){
                     td3.style.color = "red";
    }
    td1.appendChild(document.createTextNode(service.url ));
    var deleteBtn = document.createElement("button");
        deleteBtn.innerHTML = "Delete";
        deleteBtn.onclick = function() {deleteService(service.url)};
        deleteBtn.style.marginRight = "10px";
        td4.appendChild(deleteBtn);
    tr.appendChild(td1);
    tr.appendChild(td2);
    tr.appendChild(td3);
     tr.appendChild(td4);
    listContainer.appendChild(tr);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    if (isUrlValid(urlName)) {

            let name = document.querySelector('#name').value;

        callEndpoint('/service', JSON.stringify({url:urlName,name:name}),'post');
    } else {
         window.alert('Please enter a valid url');
    }

}
function deleteService(urlName) {
    callEndpoint('/delete', JSON.stringify({url:urlName}),'delete');
}

function callEndpoint(endpoint, body,method) {
    fetch(endpoint, {
            method: method,
            headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: body
        }).then(res => location.reload());
}



function isUrlValid(url) {
    var regexp = /^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:/?#[\]@!\$&'\(\)\*\+,;=.]+$/;

    if(regexp.test(url)) {
        return true;
    }

    return false;
}

setInterval(function(){
   location.reload();
}, 100000);