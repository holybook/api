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
fi
remote_sha=$(git -C "$DATA_DIR" rev-parse "origin/$BRANCH")

# Compare against the commit actually imported into the database (recorded by
# the importer), not the local clone's HEAD. This way a previous run that
# cloned/fetched but failed to import does not look "in sync" — the import is
# retried until it succeeds. Empty when the table or row does not exist yet.
applied_sha=$(docker compose exec -T db \
  psql -U server -d holybook -tAc \
  "SELECT value FROM sync_state WHERE key = 'applied_commit'" 2>/dev/null |
  tr -d '[:space:]' || true)

if [ "$remote_sha" = "$applied_sha" ]; then
  echo "$(ts) database already at $remote_sha, nothing to do"
  exit 0
fi

echo "$(ts) importing ${applied_sha:-<none>} -> $remote_sha"
git -C "$DATA_DIR" reset --hard --quiet "origin/$BRANCH"

# The importer reads the password from DB_PASSWORD in its environment (injected
# by Compose from .env), so no credentials appear here or in the URL. It records
# the commit as applied only after the import completes successfully.
docker compose run --rm importer \
  -jdbc "jdbc:postgresql://db:5432/holybook" \
  -u server \
  --commit "$remote_sha" \
  -i /content

echo "$(ts) import complete, database now at $remote_sha"
