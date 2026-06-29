#!/usr/bin/env bash
#
# Syncs the holybook/data repository into the database.
#
# Pulls the data repo; if main moved since the last run, reimports the whole
# dataset from its content/ subfolder (the importer drops and recreates the
# schema, so the DB ends up an exact mirror of the repo HEAD). The local clone's
# HEAD is the record of which commit is currently in the DB, so no separate
# state is tracked.
#
# Designed to be run from cron AND by hand over SSH. It is a no-op when there
# are no new commits, so running it frequently is cheap.
#
# Environment overrides:
#   COMPOSE_DIR  directory holding compose.yaml + .env (default: script dir)
#   DATA_REPO    git URL of the data repo
#   BRANCH       branch to track (default: main)
set -euo pipefail

COMPOSE_DIR="${COMPOSE_DIR:-$(cd "$(dirname "$0")" && pwd)}"
DATA_REPO="${DATA_REPO:-git@github.com:holybook/data.git}"
BRANCH="${BRANCH:-main}"
DATA_DIR="$COMPOSE_DIR/data"

ts() { date -u +%FT%TZ; }

cd "$COMPOSE_DIR"

# Prevent overlapping runs (e.g. a slow import overrunning into the next cron
# tick). flock is part of util-linux and present on the droplet; if it is ever
# missing we proceed without locking rather than silently skipping the import.
if command -v flock >/dev/null 2>&1; then
  exec 9>"$COMPOSE_DIR/.sync-content.lock"
  if ! flock -n 9; then
    echo "$(ts) another sync is already running, skipping"
    exit 0
  fi
else
  echo "$(ts) flock not available, running without an overlap lock"
fi

if [ ! -d "$DATA_DIR/.git" ]; then
  echo "$(ts) cloning $DATA_REPO"
  git clone --branch "$BRANCH" --single-branch "$DATA_REPO" "$DATA_DIR"
else
  git -C "$DATA_DIR" fetch --quiet origin "$BRANCH"
  local_sha=$(git -C "$DATA_DIR" rev-parse HEAD)
  remote_sha=$(git -C "$DATA_DIR" rev-parse "origin/$BRANCH")
  if [ "$local_sha" = "$remote_sha" ]; then
    echo "$(ts) no content changes ($local_sha), nothing to do"
    exit 0
  fi
  echo "$(ts) updating content $local_sha -> $remote_sha"
  git -C "$DATA_DIR" reset --hard "origin/$BRANCH"
fi

echo "$(ts) reimporting database from content"
# The importer reads the password from DB_PASSWORD in its environment (injected
# by Compose from .env), so no credentials appear here or in the URL.
docker compose run --rm importer \
  -jdbc "jdbc:postgresql://db:5432/holybook" \
  -u server \
  -i /content

echo "$(ts) import complete, commit $(git -C "$DATA_DIR" rev-parse HEAD)"
