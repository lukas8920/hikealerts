FROM debian:bullseye-slim

RUN apt-get update && apt-get install -y \
    gnupg2 \
    gnupg-agent \
    pass \
    && rm -rf /var/lib/apt/lists/*

# Create a directory for the GPG keys and configure the agent
RUN mkdir -p /root/.gnupg && chmod 700 /root/.gnupg

# Copy your gpg-agent configuration file
COPY gpg-agent.conf /root/.gnupg/gpg-agent.conf

# Start the GPG agent
CMD ["sh", "-c", "gpg-agent --daemon --enable-ssh-support > /var/log/gpg-agent.log 2>&1 && exec tail -f /dev/null"]

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Install Python, pass, gnupg, git, and other necessary dependencies
RUN apt-get update && \
    apt-get install -y \
    python3 \
    python3-pip \
    pass \
    gnupg \
    git \
    && apt-get clean

# Create a working directory
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Install any needed Python packages
RUN pip3 install --no-cache-dir -r requirements.txt

# Define environment variable for Flask
ENV FLASK_APP=api.py

# Run the Flask app
CMD ["flask", "run", "--host=0.0.0.0", "--port=5000"]
