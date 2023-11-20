from flask import Flask, render_template, request, jsonify
from flask_socketio import SocketIO

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret'
socketio = SocketIO(app)

# Diccionario para almacenar las sesiones de WebSocket activas por su device_id
active_devices = {}


@app.route('/emergency/<device_id>')
def emergency(device_id):
    # Mostrar la página con el botón de emergencia
    return render_template('emergency.html', device_id=device_id)


@socketio.on('connect')
def handle_connect():
    # Aquí se manejaría la lógica de conexión del WebSocket
    pass


@socketio.on('disconnect')
def handle_disconnect():
    # Aquí se manejaría la lógica de desconexión del WebSocket
    pass


@socketio.on('register_device')
def handle_register_device(json):
    device_id = json['device_id']
    # Asociar la sesión de WebSocket con el device_id
    print(device_id)
    active_devices[device_id] = request.sid


@app.route('/trigger_alarm/<device_id>', methods=['POST'])
def trigger_alarm(device_id):
    session_id = active_devices.get(device_id)
    if session_id:
        # Emitir el evento de alarma al dispositivo específico
        socketio.emit('alarm_triggered', {'command': 'start_actions'}, room=session_id)
        return jsonify({'status': 'success'}), 200
    else:
        return jsonify({'status': 'error', 'message': 'Device not connected'}), 404


if __name__ == '__main__':
    socketio.run(app, debug=True, allow_unsafe_werkzeug=True)

