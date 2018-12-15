function init()
{
	showView("dashDiv");
}

function showView(v)
{
	hideAll();
	let e = document.getElementById(v);
	e.style.display = "block";
}

function hideAll() //hide all views
{
	let views = document.getElementsByClassName("view");
	for(let e of views)
	{
		e.style.display = "none";
	}
}

function deleteAllChilds(node)
{
	while(node.hasChildNodes())
	{
		node.removeChild(node.childNodes[0]);
	}
}

function doSimpleAjax(address, data, handler)
{
	let request = new XMLHttpRequest();
	request.onreadystatechange = function() {handler(request)};
	request.open("POST", address, true);
	request.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	request.send(data);
}

function prime(form)
{
	let min = form.elements["min"].value;
	if(clicked == "recalc")
	{
		min = form.elements["last"].value;
	}
	console.log("min=" + min);
	let max = form.elements["max"].value;
	let qs = "min=" + min + "&max=" + max;
	doSimpleAjax("Prime.do", qs, primeResult);
}

function sis(form)
{
	let prefix = form.elements["prefix"].value;
	let minGpa = form.elements["minGpa"].value;
	let sortBy = form.elements["sortBy"].value;
	let qs="prefix=" + prefix + "&minGpa=" + minGpa + "&sortBy=" + sortBy;
	doSimpleAjax("Sis.do", qs, sisResult);	
}

function createSisTable()
{
	console.log("createSisTable")
	let tableSis = document.createElement("table");
	let headerRow = document.createElement("tr");
	
	let headCol = document.createElement("th");
	headCol.innerHTML="Name";
	headerRow.appendChild(headCol);
	
	headCol = document.createElement("th");
	headCol.innerHTML="Program";
	headerRow.appendChild(headCol);

	headCol = document.createElement("th");
	headCol.innerHTML="Courses";
	headerRow.appendChild(headCol);

	headCol = document.createElement("th");
	headCol.innerHTML="GPA";
	headerRow.appendChild(headCol);

	tableSis.appendChild(headerRow);
	return tableSis;	
}
function sisResult (request)
{

	let div = document.getElementById("sisResult");
	let responseDiv = document.createElement("div");
	
	if(request.readyState==4 & request.status==200)
	{
		deleteAllChilds(div);
		
		let response = JSON.parse(request.responseText);
		
		if(response.status==0)
		{
			let tableSis = createSisTable();
			//create and then add the caption
			let caption = document.createElement("caption");
			let captionTitle = document.getElementById("sortBy").value;
			caption.innerHTML = "Sort by " + captionTitle;
			tableSis.insertBefore(caption, tableSis.childNodes[0]);

			let students = response.result;
			
			for(i in students)
			{
				let row = document.createElement("tr");
				
				let dataCol = document.createElement("td");
				dataCol.innerHTML = students[i].name;
				row.appendChild(dataCol);

				dataCol = document.createElement("td");
				dataCol.innerHTML = students[i].major;
				row.appendChild(dataCol);

				dataCol = document.createElement("td");
				dataCol.innerHTML = students[i].courses;
				row.appendChild(dataCol);

				dataCol = document.createElement("td");
				dataCol.innerHTML = students[i].gpa;
				row.appendChild(dataCol);

				tableSis.appendChild(row);
			}
			div.appendChild(tableSis);
		}
		else
		{
			responseDiv.setAttribute("class", "alert alert-danger");
			responseDiv.innerHTML=response.error;
			div.appendChild(responseDiv);		
		}

	}
	if(request.readyState==4 & request.status != 200)
	{
		responseDiv.setAttribute("class", "alert alert-danger");
		responseDiv.innerHTML="Fail to connect to server";
		div.appendChild(responseDiv);
	}
}

function primeResult(request)
{
	let div = document.getElementById("primeResult");
	if(request.readyState==4 & request.status==200)
	{		
		let response = JSON.parse(request.responseText);
		
		deleteAllChilds(div);
		
		let responseDiv = document.createElement("div");
	
		if(response.status==0)
		{
			
			responseDiv.setAttribute("class", "alert alert-info");
			let h4E = document.createElement("h4");
			h4E.innerHTML = response.result;
			responseDiv.appendChild(h4E);
			
			let hiddenInput = document.createElement("input");
			hiddenInput.setAttribute("type", "hidden");
			hiddenInput.setAttribute("id", "last");
			hiddenInput.setAttribute("value", response.result);

			let nextInput = document.createElement("input");
			nextInput.setAttribute("type","submit");
			nextInput.setAttribute("value", "Next");
			nextInput.setAttribute("onclick", "clicked='recalc'");
			nextInput.setAttribute("class", "btn btn-default");
			
			responseDiv.appendChild(hiddenInput);
			h4E.appendChild(nextInput);
			div.appendChild(responseDiv);
		}
		else
		{
			responseDiv.setAttribute("class", "alert alert-danger");
			responseDiv.innerHTML=response.error;
			div.appendChild(responseDiv);
		}
	}
	if(request.readyState==4 & request.status != 200)
	{
		responseDiv.setAttribute("class", "alert alert-danger");
		responseDiv.innerHTML="Fail to connect to server";
		div.appendChild(responseDiv);
	}
}
window.onload = init;

//
