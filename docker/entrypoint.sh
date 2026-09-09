#!/usr/bin/env bash
set -e

# Data directory for JTrac
DATA_DIR="${JTRAC_HOME:-/jtrac-data}"

# Ensure data directory exists
mkdir -p "$DATA_DIR"

# Ensure correct permissions for data directory
if [ "$(id -u)" = '0' ]; then
    chown -R jetty:jetty "$DATA_DIR"
fi

# Dynamically generate or update jtrac.properties if DATABASE_URL is provided
if [ -n "$DATABASE_URL" ]; then
    PROPS_FILE="$DATA_DIR/jtrac.properties"
    echo "==> Configuring database connection from environment variables in $PROPS_FILE"
    cat <<EOF > "$PROPS_FILE"
# Generated dynamically by JTrac Docker entrypoint
database.driver=${DATABASE_DRIVER:-org.hsqldb.jdbcDriver}
database.url=${DATABASE_URL}
database.username=${DATABASE_USERNAME:-sa}
database.password=${DATABASE_PASSWORD:-}
EOF
    if [ -n "$HIBERNATE_DIALECT" ]; then
        echo "hibernate.dialect=${HIBERNATE_DIALECT}" >> "$PROPS_FILE"
    fi
    if [ "$(id -u)" = '0' ]; then
        chown jetty:jetty "$PROPS_FILE"
    fi
fi

# Locate upstream Jetty entrypoint if available
TARGET_ENTRYPOINT=""
if [ -x "/docker-entrypoint.sh" ]; then
    TARGET_ENTRYPOINT="/docker-entrypoint.sh"
fi

# Step down from root to jetty user (UID 999) using gosu
if [ "$(id -u)" = '0' ]; then
    if [ -n "$TARGET_ENTRYPOINT" ]; then
        exec gosu jetty "$TARGET_ENTRYPOINT" "$@"
    else
        exec gosu jetty "$@"
    fi
else
    if [ -n "$TARGET_ENTRYPOINT" ]; then
        exec "$TARGET_ENTRYPOINT" "$@"
    else
        exec "$@"
    fi
fi
