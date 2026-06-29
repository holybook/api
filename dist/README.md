# Deployment

The app runs on a single DigitalOcean droplet as three containers managed by
Docker Compose:

- **db** – PostgreSQL 16
- **web** – the Ktor server (`/api`)
- **caddy** – serves the React bundle and reverse-proxies `/api` to the server,
  with automatic Let's Encrypt TLS

Images are built and pushed to the GitHub Container Registry by
[`.github/workflows/deploy.yml`](../.github/workflows/deploy.yml) on every push
to `main`. The workflow then SSHes into the droplet, writes `.env` from GitHub
secrets, and runs `docker compose pull && up -d`. The droplet itself never
builds anything.

**Database roles:** the server is read-only against the database, so it connects
as a least-privilege `webapp` role (SELECT only). All writes go through the
importer, which connects as the `server` superuser. The `webapp` role is created
once by [`db-init/01-webapp-role.sh`](db-init/01-webapp-role.sh), and
`ALTER DEFAULT PRIVILEGES` keeps it readable across the importer's reimports.

## One-time droplet setup

1. **Create the droplet** – the $6 (1 GB / 1 vCPU) shared-CPU droplet is enough.
   Ubuntu LTS.

2. **Add swap** (important on 1 GB so the JVM + Postgres don't OOM):

   ```bash
   sudo fallocate -l 2G /swapfile
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile
   echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
   ```

3. **Install Docker Engine + Compose plugin** (official convenience script):

   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER   # log out/in afterwards
   ```

4. **Create the deploy directory** the workflow expects:

   ```bash
   mkdir -p ~/holybook
   ```

5. **Add the deploy SSH key.** Generate a keypair, put the public key in
   `~/.ssh/authorized_keys` on the droplet, and store the private key as the
   `DROPLET_SSH_KEY` GitHub secret (below).

6. **Point DNS** `holybook.app` (and `www` if desired) at the droplet's IP.
   Caddy needs port 80/443 reachable to issue the certificate.

## GitHub secrets

Set these under **Settings → Secrets and variables → Actions**:

| Secret | Value |
| --- | --- |
| `DROPLET_HOST` | droplet IP or hostname |
| `DROPLET_USER` | SSH user (e.g. `root` or a sudo user) |
| `DROPLET_SSH_KEY` | private key matching the droplet's authorized_keys |
| `SITE_ADDRESS` | `holybook.app` (or `:80` to test over HTTP first) |
| `DB_PASSWORD` | password for the Postgres `server` superuser (importer) |
| `WEBAPP_PASSWORD` | password for the read-only `webapp` role (server); use a different value |
| `GEMINI_API_KEY` | Gemini API key for translation |

`GITHUB_TOKEN` is provided automatically and is used to push/pull the images –
no PAT needed as long as the droplet logs in with it at deploy time (the
workflow does this).

> **Two separate database passwords.** `DB_PASSWORD` is the `server` superuser
> (used by the importer); `WEBAPP_PASSWORD` is the read-only `webapp` role the
> server connects as. Generate **two different** strong values — any charset
> works, since credentials are passed as connection properties, not in the URL:
>
> ```bash
> openssl rand -base64 24   # run twice, once per secret
> ```
>
> ⚠️ Both are written into the Postgres data volume the **first** time the `db`
> container initialises. Changing either secret afterwards does **not** update
> the database — you'd have to `ALTER ... PASSWORD` inside Postgres (or recreate
> the volume). So set them before the first deploy and leave them be.

## Loading data

Content lives in the **holybook/data** repository, with book XML files under
`content/<lang>/<author>/<year>/*.xml`. The database is kept in sync with that
repo by [`sync-content.sh`](sync-content.sh), which is deployed to
`~/holybook/sync-content.sh`:

1. It clones/pulls `holybook/data` into `~/holybook/data`.
2. It compares the repo HEAD against the commit actually imported into the
   database — the importer records this in a small `sync_state` table **after** a
   successful import. If they differ, it reimports the whole dataset from
   `data/content/` via the `importer` container (`docker compose run --rm
   importer`). The importer drops and recreates the schema, so the database ends
   up an exact mirror of the repo HEAD — added books appear, changed books are
   replaced, removed books vanish.

Because the marker is the *imported* commit (not the local clone's HEAD), a run
that fetched but failed to import is not mistaken for "in sync" — the import is
retried on the next run. It is a no-op when there are no new commits, so it is
safe to run often.

### Run it manually

```bash
ssh <user>@<droplet>
~/holybook/sync-content.sh
```

### Schedule it (daily)

Add a cron entry for the deploy user (`crontab -e`). 04:00 keeps the brief
reimport off peak hours:

```cron
0 4 * * * /home/<user>/holybook/sync-content.sh >> /home/<user>/holybook/sync-content.log 2>&1
```

### Data repo access (deploy key)

`sync-content.sh` clones `holybook/data` over SSH by default. Give the droplet
read access with a read-only **deploy key**: generate a keypair on the droplet
(`ssh-keygen -t ed25519`), add the public key as a deploy key on the
`holybook/data` repo (Settings → Deploy keys), and make sure
`ssh -T git@github.com` succeeds for the deploy user. If the repo is public you
can instead point the script at the HTTPS URL:

```bash
DATA_REPO=https://github.com/holybook/data.git ~/holybook/sync-content.sh
```

(Set `DATA_REPO` in the cron line too, or export it in the user's profile.)

> **Note:** the reimport briefly drops and recreates the tables, so the API
> returns errors for the few seconds the rebuild takes. If that window ever
> matters, the importer can be changed to do the whole rebuild inside a single
> Postgres transaction (DDL is transactional) so readers keep seeing the old
> data until the swap commits.

Postgres itself is not published on the host. To reach it from your machine for
ad-hoc work, use an **SSH tunnel** rather than publishing the port:

```bash
ssh -L 5432:localhost:5432 <user>@<droplet>   # then connect to localhost:5432
```

⚠️ Do **not** add a bare `ports: ["5432:5432"]` mapping to the `db` service.
Docker writes its own iptables rules and bypasses `ufw`, so that would expose
Postgres to the entire internet regardless of your firewall. If you genuinely
need a host-published port, bind it to localhost only: `["127.0.0.1:5432:5432"]`.

## Manual operations

```bash
cd ~/holybook
docker compose ps                 # status
docker compose logs -f web        # server logs
docker compose logs -f caddy      # TLS / proxy logs
docker compose pull && docker compose up -d   # redeploy current tags
```

### Backups

The database lives in the `holybook_db_data` Docker volume. A simple dump:

```bash
docker compose exec db pg_dump -U server holybook | gzip > holybook-$(date +%F).sql.gz
```
