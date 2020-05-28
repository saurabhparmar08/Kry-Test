const listContainer = document.querySelector('#service-list');
const refreshInterval = 60;

getServerList();

function getServerList(){

let servicesRequest = new Request('/service');
let serviceMap = new Map();
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    let listItem = service.name + ': ' + service.url + ': ' + service.status;
    serviceResp = {};
    serviceResp.name = service.name;
    serviceResp.url = service.url;
    serviceResp.status = service.status;
    serviceMap.set(listItem, serviceResp);
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(listItem));
    listContainer.appendChild(li);
  });
  listContainer.addEventListener("dblclick", evt => {
      let data = serviceMap.get(evt.target.innerText);
      let updatedName = window.prompt("update the service: ",data.name)
      if(updatedName){
          this.updateService(updatedName, data.url);
       }


  })
  listContainer.addEventListener("contextmenu", evt => {
        evt.preventDefault()
        let data = serviceMap.get(evt.target.innerText);
        if(window.confirm("Do you want to delete service?")){
            this.deleteService(data.url);
        }
    })
});
}

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    let url = document.querySelector('#url').value;
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({name:urlName, url:url})
}).then(res=> location.reload());
}

function updateService(updatedName, url) {
    fetch('/service', {
    method: 'put',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({name:updatedName, url: url})
}).then(res=> location.reload());
}

function deleteService(url) {
    fetch('/service', {
    method: 'delete',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({url:url})
}).then(res=> location.reload());
}

function pollServices() {
    fetch('/poll', {
    method: 'get',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    }
}).then(res=> location.reload());
}

setInterval(pollServices, refreshInterval * 1000);