/*
 General usage javascript for SMCP 
 */

/* Data typing: */
function getFloat(value) {
    return parseFloat(value.replace(',',''), 10);
}

/* Checks to see if two floating point values are equal: */
function floatsAreEqual(a, b){
    var tolerance = Number.EPSILON;
    if (Math.abs(a - b) < tolerance) {
        return true;
    }else{
        return false;
    }
}

function padLeft(strToPad, iOverallLengthOfResultingString, sPadCharacter){
	var padTemplate = '';
	for (i = 0; i < iOverallLengthOfResultingString; i++){
		padTemplate = padTemplate + sPadCharacter;
	}
	return (padTemplate + strToPad).slice(-padTemplate.length);
}

/* Allows you to sort a table by clicking on its header row: */
/*
sTableID is the ID of the table to be sorted
nSortColumnIndex is the zero-based column number to use for the sort
nNumberOfHeaderRows is the number of headers rows (i.e., rows which should NOT be sorted)

 */
function sortTable(sTableID, nSortColumnIndex, nNumberOfHeaderRows) {
  var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
  table = document.getElementById(sTableID);
  switching = true;
  //Set the sorting direction to ascending:
  dir = "asc"; 
  /*Make a loop that will continue until no switching has been done:*/
  while (switching) {
    //start by saying: no switching is done:
    switching = false;
    //rows = table.getElementsByTagName("TR");
    /*Loop through all table rows (except the table headers):*/
    for (i = nNumberOfHeaderRows; i < (table.rows.length - 1); i++) {
      //start by saying there should be no switching:
      shouldSwitch = false;
      /*Get the two elements you want to compare,
      one from current row and one from the next:*/
      x = table.rows[i].getElementsByTagName("TD")[nSortColumnIndex];
      y = table.rows[i + 1].getElementsByTagName("TD")[nSortColumnIndex];
      /*check if the two rows should switch place,
      based on the direction, asc or desc:*/
      if (dir == "asc") {
        if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
          //if so, mark as a switch and break the loop:
          shouldSwitch= true;
          break;
        }
      } else if (dir == "desc") {
        if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
          //if so, mark as a switch and break the loop:
          shouldSwitch= true;
          break;
        }
      }
    }
    if (shouldSwitch) {
      /*If a switch has been marked, make the switch
      and mark that a switch has been done:*/
      table.rows[i].parentNode.insertBefore(table.rows[i + 1], table.rows[i]);
      switching = true;
      //Each time a switch is done, increase this count by 1:
      switchcount ++; 
    } else {
      /*If no switching has been done AND the direction is "asc",
      set the direction to "desc" and run the while loop again.*/
      if (switchcount == 0 && dir == "asc") {
        dir = "desc";
        switching = true;
      }
    }
  }
}