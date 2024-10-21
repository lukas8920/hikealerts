from flask import Flask, jsonify, request
import subprocess

app = Flask(__name__)

# Route to expose `pass show`
@app.route('/get_password', methods=['GET'])
def get_password():
    entry = request.args.get('entry')  # Get the entry name from query param
    if not entry:
        return jsonify({'error': 'No entry provided'}), 400

    try:
        # Execute the `pass show` command
        result = subprocess.run(['pass', 'show', entry], capture_output=True, text=True)
        if result.returncode == 0:
            return jsonify({'password': result.stdout.strip()})
        else:
            return jsonify({'error': 'Entry not found'}), 404
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
