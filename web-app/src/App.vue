<template>
  <div id="app" class="d-flex flex-column vh-100 justify-content-between align-items-center px-3">
    <div v-if="!mapVisible" class="input-group mb-3">
      <input v-model="userCode" placeholder="Enter your code" class="form-control form-control-lg" />
      <div class="input-group-append">
        <button @click="connectWebSocket" class="btn btn-lg btn-primary">Show my location</button>
      </div>
    </div>
    <div v-show="mapVisible" id="map" class="map-container w-100 mb-3"></div>
    <button v-if="mapVisible" @click="stopEffects" class="btn btn-lg btn-danger w-100">Stop Effects</button>
  </div>
</template>

<script>
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

export default {
  data() {
    return {
      userCode: '',
      deviceId: 'YOUR_DEVICE_ID', // Reemplaza con el ID real del dispositivo
      ws: null,
      map: null,
      marker: null,
      mapVisible: false,
      route: [] // Array para almacenar la ruta
    };
  },
  methods: {
    connectWebSocket() {
      this.ws = new WebSocket(`ws://192.168.1.132:5000/web/socket.io?deviceId=${this.deviceId}`);

      this.ws.onopen = () => {
        this.mapVisible = true;
        // Envía cualquier mensaje necesario al servidor aquí
      };

      this.ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        if (data.type === 'location_update') {
          this.handleLocationUpdate(data);
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    },
    handleLocationUpdate(data) {
      const location = { lat: data.latitude, lng: data.longitude };
      this.route.push(location); // Almacena la ubicación en la ruta

      if (!this.map) {
        this.initMap(location);
      } else {
        this.updateMarker(location);
        this.updateRoute();
      }
    },
    initMap(location) {
      this.map = L.map('map').setView([location.lat, location.lng], 13);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);
      this.marker = L.marker([location.lat, location.lng]).addTo(this.map);
    },
    updateMarker(location) {
      this.marker.setLatLng([location.lat, location.lng]);
      this.map.panTo([location.lat, location.lng]);
    },
    updateRoute() {
      if (this.routeLayer) {
        this.map.removeLayer(this.routeLayer);
      }
      this.routeLayer = L.polyline(this.route, { color: 'blue' }).addTo(this.map);
    },
    stopEffects() {
      if (this.ws) {
        this.ws.close();
      }
      this.mapVisible = false;
    }
  }
};
</script>

<style>
#app {
  margin: 0 auto; /* Center the app in the middle of the screen */
  max-width: 1000px; /* Max width for the app */
}

.map-container {
  height: 70vh; /* Larger height on desktop */
}

.input-group {
  width: 100%;
}

.btn, .form-control {
  width: 100%; /* Full width for large screens */
}

/* Adjust button size for smaller screens */
@media (max-width: 576px) {
  .btn, .form-control {
    width: auto; /* Auto width for smaller screens */
  }
}

/* Additional styles for a polished look */
/* ... */
</style>