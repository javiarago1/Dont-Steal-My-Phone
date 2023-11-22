<template>
  <div id="app">
    <div v-if="!mapVisible" class="input-section">
      <input v-model="userCode" placeholder="Enter your code" class="input-control" />
      <button @click="connectWebSocket" class="button-control">Show my location</button>
    </div>
    <div v-show="mapVisible" id="map" class="map-container"></div>
    <button v-if="mapVisible" @click="stopEffects" class="button-stop">Stop doing sounds</button>
  </div>
</template>

<script>
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

export default {
  data() {
    return {
      userCode: '',
      deviceId: 'YOUR_DEVICE_ID',
      ws: null,
      map: null,
      marker: null,
      mapVisible: false,
      route: []
    };
  },
  methods: {
    connectWebSocket() {
      this.ws = new WebSocket(`ws://192.168.1.132:5000/web/socket.io?deviceId=${this.userCode}`);

      this.ws.onopen = () => {
        this.mapVisible = true;
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.status === 'success') {
            this.mapVisible = true;
          } else if (data.status === 'error') {
            alert(data.message);
            this.mapVisible = false;
          } else if (data.type === 'location_update') {
            this.handleLocationUpdate(data);
          }
        } catch (error) {
          console.error('Error parsing message data:', error);
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    },
    handleLocationUpdate(data) {
      const location = { lat: data.latitude, lng: data.longitude };
      this.route.push(location); // Store the location in the route

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
        const message = JSON.stringify({
          command: "stop_effects",
          deviceId: this.deviceId
        });
        this.ws.send(message);
        this.mapVisible = false;
      }
    }
  }
};
</script>

<style>
#app {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: auto;
  max-height: 100vh;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
  overflow-x: hidden;
  overflow-y: auto;
}

.map-container {
  width: calc(100% - 40px);
  height: 50vh;
  max-height: 50vh;
  background-color: #e0e0e0;
  border: 1px solid #ccc;
  margin: 20px 0;
}

.input-section, .input-control, .button-control, .button-stop {
  width: 100%;
  padding: 10px;
  box-sizing: border-box;
}

.button-stop {
  background-color: red;
  color: white;
  border: none;
}

@media only screen and (max-width: 600px) {
  #app {
    padding: 0;
  }

  .map-container {
    width: 100%;
    margin: 10px 0;
  }

  .input-section, .input-control, .button-control, .button-stop {
    padding: 10px;
  }
}

</style>