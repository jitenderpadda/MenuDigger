var result;
var map;
var markers=[];
var requestTerm;
var userID;
var pos;
//On Load
$(function(){
    //Initialize UI elements
    $(".ui.dropdown")
        .dropdown();
    $("#resultTable").hide();

    //Subscribe
    $("#subscribe").click(function () {
        console.log("userId---"+userID);
        var subscriber={};
        subscriber["email"]=userID;
        subscriber["id"]=userID;
        var request=$.ajax({
            method:"POST",
            url: "https://s3628144-cc2017.appspot.com/_ah/api/subscriberendpoint/v1/subscriber",
            data: JSON.stringify(subscriber),
            dataType: "json",
            contentType: "application/json"
        });
        request.done(function () {
            console.log("Success");
            $('.ui.basic.modal')
                .modal('show')
            ;
        });
    });
    //On View Modal Open
    $("#myModal").on("shown.bs.modal",function (e) {
        alert("modal");
        $("#rateBtn").show();
        $(".right.floated").hide();
        var searchItemId;
        $.get("https://s3628144-cc2017.appspot.com/_ah/api/menuitemendpoint/v1/menuitem?search="+requestTerm,function(menuItems, status){
            //To Account for multiple search paramters issue
            for(var i=0; i<menuItems.items.length;i++){
                console.log("menuItems---"+menuItems.items[i].id);
                if(menuItems.items[i].restaurantId==e.relatedTarget.id){
                    searchItemId=menuItems.items[i].id;
                    $("#cardHeader").text(menuItems.items[i].name+" - "+e.relatedTarget.name);
                }
            }
            loadComments(searchItemId);
            //On Click Rate
            $("#rateBtn").click(function () {
                $("#inputRating").rating("enable");
                $("#commentSection").removeClass("hidden");
                $(".right.floated").show();
                $(this).hide();
            });
            //On Submit Comments
            $("#submitBtn").click(function () {
                alert("Submit Click");
                var userComment={};
                userComment.userId=userID;
                userComment["itemId"]=searchItemId;
                userComment["comment"]=$("#inputComments").val();
                userComment["rating"]=$("#inputRating").rating("get rating");
                console.log("userComment----"+JSON.stringify(userComment, null, '\t'));
                var request=$.ajax({
                    method:"POST",
                    url: "https://s3628144-cc2017.appspot.com/_ah/api/usercommentendpoint/v1/usercomment",
                    data: JSON.stringify(userComment),
                    dataType: "json",
                    contentType: "application/json"
                });
                request.done(function () {
                    $("#commentSection").addClass("hidden");
                    loadComments(searchItemId);
                });
            });
            //On Cancel
            $("#cancelBtn").click(function () {
                $("#commentSection").addClass("hidden");
                $(".right.floated").hide();
                $("#rateBtn").show();
                $("#inputRating").rating();
                $("#commentSection").val("");
            });
        });
    });
    //On View Modal Hide
    $("#myModal").on("hidden.bs.modal",function (e) {
        alert("hide");
        //$(this).remove();
    });
    //AutoComplete
    $( "#search" ).autocomplete({
        minLength: 3,
        source: function getMenuItems(request,response){
            var requestTerm=request.term.toUpperCase();
            console.log(requestTerm);
            req=new XMLHttpRequest();
            req.onreadystatechange=function () {
                if(this.readyState==4 && this.status==200){
                    //console.log(this.responseText);
                    var result=JSON.parse(this.responseText);
                    console.log(result.items);
                    if(result.items!=null){
                        var resp =new Array(result.items.length);
                        for(var i=0;i<result.items.length;i++){
                            console.log(result.items[i]);
                            console.log(result.items[i].name);
                            resp[i]=result.items[i].name;
                        }
                        console.log(resp);
                        var uniqueResp = [];
                        $.each(resp, function(i, el){
                            if($.inArray(el, uniqueResp) === -1) uniqueResp.push(el);
                        });
                        console.log(uniqueResp);
                        response(uniqueResp);
                    }
                }
            }
            req.open("GET","https://s3628144-cc2017.appspot.com/_ah/api/menuitemendpoint/v1/menuitem?search="+requestTerm,true);
            req.send();
        }
    });
    //Search Button
    $('#validationForm').on('submit', function(e) {
        e.preventDefault();
        search();
    });
});
//Load Comments
function loadComments(searchItemId) {
    $(".ui.celled.padded.table > tbody > tr").empty();
    console.log("id---"+searchItemId);
    $.get("https://s3628144-cc2017.appspot.com/_ah/api/usercommentendpoint/v1/usercomment?searchItemId="+searchItemId,function(userComments, status){
        for(var i=0; i<userComments.items.length;i++){
            row="<tr><td><p class=\"ui center aligned header\">"+userComments.items[i].userId+"</p></td>" +
                "<td><div class=\"ui star rating\" data-rating=\""+userComments.items[i].rating+"\" data-max-rating=\"5\"></div></td>" +
                "<td>"+userComments.items[i].comment+"</td></tr>";
            $(".ui.celled.padded.table").append(row);
        }

        $(".ui.rating").rating("disable");
        $(".right.floated").hide();
    });
}
//Search
function search(){
    clearMarkers();
    $("#loader").removeClass("hidden");
    $("#resultTable > tbody > tr").empty();
    requestTerm = $("#search").val();
    requestTerm=requestTerm.toUpperCase();
    console.log(requestTerm);
    var url;
    //Add Tags paramter in url
    var filterTags=$("#tags").dropdown("get value");
    console.log(filterTags);
    var tags="";
    for(i=0; i<filterTags.length;i++){
        tags=tags+"&itemTags="+filterTags[i];
    }
    console.log(tags);
    if(tags.length>0){
        console.log("URL");
        url="https://s3628144-cc2017.appspot.com/_ah/api/restaurantendpoint/v1/restaurant?searchItem=" + requestTerm + tags;
    }
    else url="https://s3628144-cc2017.appspot.com/_ah/api/restaurantendpoint/v1/restaurant?searchItem=" + requestTerm;
    console.log(url);
    req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            result = JSON.parse(this.responseText);
            $("#loader").addClass("hidden");
            $("#resultTable").show();
            if (result.items != null) {
                var resp = new Array(result.items.length);
                console.log(result.items);
                var origin=new Array(pos);
                var destinations=[];
                /***Calculate Distance From Current Location***/
                for (var i = 0; i <result.items.length; i++) {
                    destinations[i]=result.items[i].restAddress;
                }
                var geocoder = new google.maps.Geocoder;

                var service = new google.maps.DistanceMatrixService;
                service.getDistanceMatrix({
                    origins: origin,
                    destinations: destinations,
                    travelMode: 'DRIVING',
                    unitSystem: google.maps.UnitSystem.METRIC,
                    avoidHighways: false,
                    avoidTolls: false
                }, function(response, status) {
                    if (status !== 'OK') {
                        alert('Error was: ' + status);
                    } else {
                        console.log(response);
                        var destinationList = response.destinationAddresses;
                        var results = response.rows[0].elements;
                        console.log(results);
                        for (var i = 0; i < destinationList.length; i++) {
                            console.log("output---"+destinationList[i] +
                                ': ' + results[i].distance.text + ' in ' +
                                results[i].duration.text);
                            var radius=$("#radius").dropdown("get value");
                            console.log(radius);
                            if(radius!="" && radius!=undefined && radius!=null){
                                if(results[i].distance.value < $("#radius").dropdown("get value")){
                                    console.log(results[i].duration.text);
                                    var ETA= results[i].duration.text;
                                    var restDist=results[i].distance.text;
                                    geocoder.geocode({'address': destinationList[i]},addRestaurants(i,restDist,ETA));
                                }
                            }
                            else {
                                console.log(results[i].duration.text);
                                var ETA= results[i].duration.text;
                                var restDist=results[i].distance.text;
                                geocoder.geocode({'address': destinationList[i]},addRestaurants(i,restDist,ETA));
                            }
                        }
                    }
                });
            }
        }
    }
    req.open("GET", url, true);
    req.send();
}
//Add Restaurants to Table and Map
function addRestaurants(i,restDist,ETA){
    console.log(ETA);
    return function(results, status) {
        /***Add Restaurants***/
        var bounds = new google.maps.LatLngBounds();
        bounds.extend(pos);
        //for (var i = 0; i < result.items.length; i++) {
            /***Create Table row***/
                //resp[i] = result.items[i].restroName;
            var row = "<tr>"
                + "<td>" + result.items[i].restroName + "</td><td>" + result.items[i].restAddress + "</td><td>" + restDist  + "</td><td>" + ETA
                + "</td><td><input type='button' data-target='#myModal' data-toggle='modal' class='ui primary button' value='View' id='" + result.items[i].id + "' name='" + result.items[i].restroName + "'></td>"
                + "</tr>"
            $("#resultTable").append(row);
            /***Create Map Marker***/
            var lat = result.items[i].latLong.split(',')[0];
            var long = result.items[i].latLong.split(',')[1];

            var marker = new google.maps.Marker({
                position: new google.maps.LatLng(lat, long),
                map: map,
                animation: google.maps.Animation.DROP
            });
            /***Add Listener to Marker***/
            google.maps.event.addListener(marker, 'click', (function (marker, i) {
                    return function () {
                        var infowindow = new google.maps.InfoWindow();
                        infowindow.setContent("<input type='button' data-target='#myModal' data-toggle='modal' class='ui label' style='width: 100% !important;' value='" + result.items[i].restroName + "' id='" + result.items[i].id + "' name='" + result.items[i].restroName + "'>");
                        infowindow.open(map, marker);
                    }
                })(marker, i));
            markers.push(marker);
            // Adjust bounds to show new Markers
            var position = new google.maps.LatLng(lat, long);
            bounds.extend(position);
            map.fitBounds(bounds);
        //}
    }
}
//Map
function initMap() {
    var myLatLng = {lat: -37.8136, lng: 144.9631};

    // Create a map object and specify the DOM element for display.
    map = new google.maps.Map(document.getElementById('map'), {
        center: myLatLng,
        zoom: 12,
        useCurrentLocation: true,
        disableDefaultUI: true
    });
    // Create a marker and set its position.
    var image = {
        url: "img/location.png",
        // This marker is 20 pixels wide by 32 pixels high.
        size: new google.maps.Size(128,128),
        // The origin for this image is (0, 0).
        origin: new google.maps.Point(0, 0),
        // The anchor for this image is the base of the flagpole at (0, 32).
        anchor: new google.maps.Point(0, 32)
    };
    var marker = new google.maps.Marker({
        map: map,
        icon: image,
        position: myLatLng,
        animation: google.maps.Animation.DROP,
        title: 'Your Current Location'
    });
    // Try HTML5 geolocation.
    infoWindow = new google.maps.InfoWindow;
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            pos = {
                lat: position.coords.latitude,
                lng: position.coords.longitude
            };

            marker.setPosition(pos);
            map.panTo(pos);
            map.setZoom(14);
        }, function() {
            //handleLocationError(true, infoWindow, map.getCenter());
        });
    } else {
        // Browser doesn't support Geolocation
        //handleLocationError(false, infoWindow, map.getCenter());
    }
}
function clearMarkers() {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
    }
    markers = [];
}

//Google Login
function onSignIn(googleUser) {
    // Useful data for your client-side scripts:
    var profile = googleUser.getBasicProfile();
    userID = profile.getEmail();
    // The ID token you need to pass to your backend:
    var id_token = googleUser.getAuthResponse().id_token;
    $(".g-signin2").css("display", "none");
    $(".wrapper").css("display", "none");
    $(".data").css("display", "block");
    console.log(profile.getName());
    $("#firstName").text(profile.getName());
    /*$("#image").attr('src', profile.getImageUrl());	**/

}

//Google signOut
function signOut() {
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function() {
        $(".g-signin2").css("display", "block");
        $(".wrapper").css("display", "block");
        $(".data").css("display", "none");
    });
}