//= ../../bower_components/jquery/dist/jquery.js
//= ../../bower_components/bootstrap-sass/assets/javascripts/bootstrap.min.js

var position = [50.4454807823188, 30.519480407238007];

function initialize() {

    var myOptions = {
        zoom: 17,
        streetViewControl: true,
        scaleControl: true,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    map = new google.maps.Map(document.getElementById('googlemaps'),
        myOptions);


    latLng = new google.maps.LatLng(position[0], position[1]);

    map.setCenter(latLng);

    marker = new google.maps.Marker({
        position: latLng,
        map: map,
        draggable: false,
        animation: google.maps.Animation.DROP,
        icon: '../img/map-pointer.png'

    });
}

google.maps.event.addDomListener(window, 'load', initialize);