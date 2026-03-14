// Minimal map helper: places and click-to-pin
document.addEventListener('DOMContentLoaded', function () {
    const mapEl = document.getElementById('map');
    if (!mapEl) return;
    // Fallback center
    const defaultCenter = { lat: 21.0285, lng: 105.8542 };
    const lat = window.initialLat ? parseFloat(window.initialLat) : null;
    const lng = window.initialLng ? parseFloat(window.initialLng) : null;
    const center = (lat && lng) ? { lat, lng } : defaultCenter;
    const map = new google.maps.Map(mapEl, { center, zoom: 15 });
    let marker = null;
    if (lat && lng) {
        marker = new google.maps.Marker({ position: { lat, lng }, map });
    }

    map.addListener('click', (e) => {
        const pos = e.latLng;
        setMarker(pos.lat(), pos.lng());
        reverseGeocode(pos);
    });

    function setMarker(lat, lng) {
        if (marker) marker.setMap(null);
        marker = new google.maps.Marker({ position: { lat, lng }, map });
        document.getElementById('latInput').value = lat;
        document.getElementById('lngInput').value = lng;
    }

    function reverseGeocode(latlng) {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ location: latlng }, (results, status) => {
            if (status === 'OK' && results[0]) {
                document.getElementById('addressInput').value = results[0].formatted_address;
            }
        });
    }

    // If no initial marker, try to use Places Autocomplete on address input
    const addr = document.getElementById('addressInput');
    if (addr && google.maps.places) {
        const ac = new google.maps.places.Autocomplete(addr);
        ac.addListener('place_changed', () => {
            const place = ac.getPlace();
            if (place.geometry && place.geometry.location) {
                setMarker(place.geometry.location.lat(), place.geometry.location.lng());
            }
        });
    }
});
