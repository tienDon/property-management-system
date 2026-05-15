# PMS Deployment Notes And CV Update

## CV snippet for `Backend Responsibilities`
You can add one short bullet like this:

- Deployed the Spring Boot application to AWS EC2 using Docker, SQL Server, Nginx, HTTPS, and GitHub Actions CI/CD, enabling the system to run stably on the public domain `https://pms.druignguyen.me`.

If you want a slightly more backend-focused version:

- Built and deployed the backend to AWS EC2 with SQL Server, Docker, Nginx, systemd, and GitHub Actions CI/CD, turning the local Spring Boot project into a publicly accessible HTTPS production service.

## Full deployment guide

### 1. Goal
The goal was to move the project from:

- local Spring Boot app on `localhost`
- local SQL Server / imported SQL script

to:

- public backend running on AWS EC2
- public HTTPS website at `https://pms.druignguyen.me`
- CI/CD so new code can be deployed from GitHub Actions

### 2. Final production architecture
The deployed system is split into separate layers:

- `Spring Boot app`
  - built as `.jar`
  - runs with `systemd`
  - uses `Java 24`
- `SQL Server`
  - runs in Docker
  - uses a persistent Docker volume
- `Nginx`
  - reverse proxy in front of Spring Boot
  - receives traffic on `80/443`
- `Let's Encrypt / Certbot`
  - provides SSL certificate for HTTPS
- `Cloudflare DNS`
  - points subdomain `pms.druignguyen.me` to EC2
- `GitHub Actions`
  - builds and deploys the backend jar automatically

### 3. Important deployment concept
The app and the database are **not deployed the same way**.

- The app is deployed by CI/CD.
- The database is hosted separately inside a SQL Server Docker container on EC2.
- The SQL data is imported manually and then reused across deployments.

That means:

- `git push` updates the backend code
- `git push` does **not** recreate or reimport the database automatically

### 4. Initial server provisioning
An Ubuntu EC2 instance was prepared first.

Main tasks:

- create EC2 instance
- attach public IP / Elastic IP
- allow inbound ports:
  - `22` for SSH
  - `80` for HTTP
  - `443` for HTTPS

Why:

- `22` is needed for SSH and GitHub Actions deployment
- `80/443` are needed so users can access the site from browser

### 5. Install Docker and Docker Compose
Docker was installed on Ubuntu so SQL Server could run as a container.

Main packages installed:

- `docker-ce`
- `docker-ce-cli`
- `containerd.io`
- `docker-buildx-plugin`
- `docker-compose-plugin`

Why:

- SQL Server on Linux is easier to isolate and run through Docker
- Docker volume keeps DB data persistent after restart

Verification:

- `docker run hello-world`

### 6. Create deploy folders and runtime user
Folders were created under `/opt/pms` and a dedicated runtime user `pms` was used.

Main folders:

- `/opt/pms/app`
- `/opt/pms/sqlserver`

Why:

- keeps application files organized
- separates app runtime from normal SSH user
- safer and cleaner for `systemd`

### 7. Run SQL Server with Docker
SQL Server 2022 was started using Docker Compose.

Final approach used:

- Docker named volume instead of host bind mount

Why:

- SQL Server 2022 container runs as non-root by default
- host bind mount caused permission errors like `/.system could not be created`
- named volume avoided Linux permission issues and made the DB stable

Result:

- SQL Server container became healthy
- port `1433` was exposed only on `127.0.0.1:1433`

Why binding to localhost matters:

- database is not publicly exposed to the internet
- only the app on the same server can connect to it

### 8. Create database and app login
Inside SQL Server, a dedicated DB and login were created.

Main objects:

- database: `PropertyManagementDB_Export`
- login: `property-management`
- user in DB: `property-management`

Why:

- app should not use `sa`
- dedicated login is cleaner and safer for application access

Important note:

- `sqlcmd` needed `-C` because ODBC Driver 18 verifies SSL by default
- SQL Server container uses a self-signed certificate

### 9. Import the SQL data
The file `property-management-system.sql` was uploaded from Windows to EC2 and then imported into SQL Server container.

Flow:

1. upload `.sql` to `/home/ubuntu`
2. copy file into Docker container
3. run `sqlcmd -i ...`

Why:

- this loads all application data into production DB

Important result:

- user/account data existed after import
- many demo posts were later found to be expired by date, which explained why login worked but public room lists looked empty

### 10. Create production `.env`
A production environment file was prepared and stored at:

- `/opt/pms/app/.env`

It contains:

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- external API keys like:
  - VNPay
  - Cloudinary
  - Gemini
  - OpenCage
  - VNPT

Why:

- keeps production secrets out of source code
- lets Spring Boot read config through environment variables

### 11. Build the backend jar
The Spring Boot project was built locally using Maven.

Command used:

```powershell
mvn clean package -DskipTests
```

Output:

- `target/property-management-0.0.1-SNAPSHOT.jar`

Why:

- the EC2 server runs the packaged backend jar

### 12. Install Java 24 on EC2
The server installed Java 24 using SDKMAN under the `pms` user.

Why:

- the project build/runtime targets Java 24
- keeping Java under `pms` avoids mixing app runtime with system packages

### 13. Create a `systemd` service
The backend jar was run as a Linux service:

- service name: `pms`

Why:

- app starts automatically on boot
- restart is easy with `systemctl`
- service logs can be checked via `journalctl`

Main service responsibilities:

- load `.env`
- use Java 24
- run jar from `/opt/pms/app`
- auto restart if process stops unexpectedly

### 14. Verify local backend health
After starting the service, health was verified with:

```bash
curl http://127.0.0.1:8080/health
```

Why:

- confirms Spring Boot is alive before exposing it to the internet

Expected result:

- JSON with `"status":"UP"`

### 15. Configure Nginx as reverse proxy
Nginx was configured in front of Spring Boot.

Main behavior:

- browser requests come to Nginx on `80/443`
- Nginx forwards traffic to Spring Boot on `127.0.0.1:8080`

Why:

- cleaner public HTTP/HTTPS setup
- easier SSL handling
- app itself stays private on internal port `8080`

Later, Nginx was tightened so the site only serves:

- `pms.druignguyen.me`

and not `www` or root domain.

### 16. Configure DNS in Cloudflare
Cloudflare DNS was used to point the subdomain to the EC2 server.

Final relevant record:

- `A` record
  - name: `pms`
  - value: `18.139.134.107`

Why:

- makes `pms.druignguyen.me` resolve to the EC2 instance

### 17. Enable HTTPS with Let's Encrypt
Certbot was installed on EC2 and used with Nginx.

Final result:

- valid HTTPS certificate for `pms.druignguyen.me`

Why:

- browser traffic is encrypted
- users can access secure production URL
- payment/API callbacks can use a stable HTTPS domain

### 18. Restrict the site to only `pms.druignguyen.me`
The Nginx config was updated so the app only runs on:

- `https://pms.druignguyen.me`

Why:

- avoids duplicate public entry points
- keeps branding and callback URLs consistent

Also updated:

- `VNPAY_RETURN_URL=https://pms.druignguyen.me/payment/vnpay/return`

### 19. GitHub Actions CI/CD setup
A GitHub Actions workflow was used to automate deployment.

Repository secrets required:

- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_KEY`

Workflow responsibilities:

1. checkout source code
2. setup Java 24
3. build jar with Maven
4. configure SSH
5. upload new jar to EC2
6. replace old jar on server
7. restart `pms` service
8. verify `/health`

Why:

- after setup, deployment no longer requires manual jar upload
- backend can be updated from Git push

### 20. CI/CD issues solved during setup
Several real deployment issues were fixed.

#### 20.1 SSH secrets debugging
The workflow originally failed at `Configure SSH`.

Reasons investigated:

- missing secret
- malformed SSH key
- EC2 not reachable on port `22`

Fixes:

- improved workflow validation for secrets
- allowed GitHub Actions runner to reach port `22`

#### 20.2 Healthcheck timing issue
The workflow later failed at `Restart service`.

Cause:

- Spring Boot needed more than 10 seconds to fully start
- healthcheck ran too early

Fix:

- replace one-shot sleep with retry-based healthcheck

Why this matters:

- deployment no longer fails just because app startup takes a bit longer

### 21. Application issues discovered after deployment
Deployment also surfaced app-level issues.

#### 21.1 Public room list looked empty
Cause:

- demo `posts` in DB had `post_expired_at` already in the past
- marketplace query only showed `ACTIVE` and non-expired posts

Fix:

- add fallback logic so public listing can still show demo data from `ACTIVE/EXPIRED` posts when no live post is available

#### 21.2 AI chat could not recommend rooms
Cause:

- recommendation query was also filtering too strictly by live post state
- chat flow also depended too hard on Gemini API

Fix:

- add fallback recommendation logic using demo posts
- add rule-based parser fallback in case Gemini API fails

#### 21.3 UTF-8 / Vietnamese copy
Cause:

- some new homepage / video review text needed proper Vietnamese with accents

Fix:

- update public templates to use Vietnamese UTF-8 content

### 22. Final production result
After all steps, the project successfully ran at:

- `https://pms.druignguyen.me`

Verified components:

- Spring Boot service healthy
- SQL Server container healthy
- Nginx reverse proxy working
- HTTPS certificate valid
- GitHub Actions able to deploy backend

### 23. Current operational model
Today the system works like this:

- source code is pushed to GitHub
- GitHub Actions builds and deploys backend jar
- EC2 serves the app through Nginx + HTTPS
- SQL Server data persists in Docker volume
- DB is shared and reused across deployments

### 24. Short explanation for interview / CV discussion
If someone asks what you actually did in deployment, you can explain it briefly like this:

> I moved the backend from localhost to AWS EC2 by packaging the Spring Boot app as a jar, running SQL Server in Docker, exposing the app through Nginx with HTTPS, and automating backend deployment through GitHub Actions. I also fixed runtime issues such as expired demo posts, healthcheck timing, and AI fallback behavior so the public system worked reliably.

## 25. How to move from a 30 GiB EC2 root volume to a 15 GiB instance

### 25.1 Important limitation
You **cannot directly shrink** an existing Amazon EBS volume from `30 GiB` to `15 GiB`.

AWS only supports:

- increasing EBS volume size
- creating a new volume from a snapshot with size **equal to or larger than** the snapshot source volume

So if your current root volume is `30 GiB`:

- you cannot press `Modify volume` and change it to `15 GiB`
- you also cannot create a `15 GiB` volume directly from a snapshot of that `30 GiB` root volume

### 25.2 Practical solution
The safest and simplest fix is:

1. back up everything important from the current instance
2. free or remove the old `30 GiB` storage
3. create a brand-new EC2 instance with `15 GiB`
4. restore the app, DB, and configs onto the new instance

### 25.3 If your account is blocked by storage quota
If your account currently allows only about `30 GiB` of EBS storage, then you usually **cannot keep**:

- old instance root volume: `30 GiB`
- new instance root volume: `15 GiB`

at the same time.

That means you need one of these two approaches:

#### Option A: Best if allowed by your account

- temporarily request or allow more storage quota
- create the new `15 GiB` instance
- migrate data
- delete the old `30 GiB` instance and volume

#### Option B: Best if quota is strict

- back up app and DB **out of the old instance** to your local machine or S3
- terminate/delete the old `30 GiB` instance/volume
- create a new `15 GiB` instance
- restore the backups

For your case, if AWS is preventing a second instance right now, **Option B is the practical path**.

### 25.4 Recommended migration checklist

Before doing anything destructive, back up these items from the old EC2:

- `/opt/pms/app/.env`
- `/etc/nginx/sites-available/pms`
- `/etc/systemd/system/pms.service`
- `/opt/pms/sqlserver/docker-compose.yml`
- the database itself
- optionally the current `.jar`

Also write down:

- current Elastic IP or public IP
- current security group
- current domain DNS record
- current GitHub Actions secrets:
  - `EC2_HOST`
  - `EC2_USER`
  - `EC2_SSH_KEY`

### 25.5 Step-by-step migration plan

#### Step 1: SSH into the old instance
Connect to the current server normally.

Why:

- you need to export configs and database before removing the old volume

#### Step 2: Back up configuration files
Copy these files out of the server:

- `/opt/pms/app/.env`
- `/etc/nginx/sites-available/pms`
- `/etc/systemd/system/pms.service`
- `/opt/pms/sqlserver/docker-compose.yml`

Best destination:

- your Windows machine
- or an S3 bucket

Why:

- these files contain the exact production runtime setup

#### Step 3: Back up the SQL Server database
Because your SQL Server is running in Docker, do a logical or SQL Server native backup.

Recommended approach:

1. create a backup folder inside the container or mounted path
2. run SQL Server backup command
3. copy the `.bak` file out to the host
4. download it to your local machine

Why:

- this preserves current production data
- you should not rely only on the old EC2 root volume

If you do not care about current runtime data and only need seed/demo data, you can reuse:

- `property-management-system.sql`

But if users or posts have changed, use a real DB backup instead.

#### Step 4: Back up the application jar if needed
Optional file:

- `/opt/pms/app/property-management-0.0.1-SNAPSHOT.jar`

Why:

- useful as a last fallback
- not strictly required if GitHub repo and CI/CD are working

#### Step 5: Decide how to preserve the domain
You have two possibilities:

##### If you are using an Elastic IP

- detach/disassociate it from the old instance later
- re-associate it to the new instance

Why:

- Cloudflare DNS does not need to change
- GitHub secret `EC2_HOST` can stay the same if it uses that IP

##### If you are using only the temporary public IP

- after launching the new instance, update Cloudflare `A` record
- also update GitHub secret `EC2_HOST`

### 25.6 You cannot reduce the old root volume directly
At this point, do **not** spend time looking for a button to change:

- `30 GiB` → `15 GiB`

on the same EBS root volume.

That workflow is not supported for normal EC2 root EBS downsizing.

### 25.7 Remove the old 30 GiB instance when backup is complete
If your quota is strict and blocks a new instance:

1. verify your backups exist locally
2. stop the old instance
3. terminate the old instance
4. ensure the old root volume is deleted, or delete it manually if needed

Why:

- you must free the `30 GiB` allocation before creating a new `15 GiB` root volume

Important:

- only do this after confirming `.env`, DB backup, and Nginx/service files are safely copied out

### 25.8 Create the new EC2 instance with 15 GiB
Launch a new Ubuntu EC2 instance with:

- root volume size: `15 GiB`
- same region
- same VPC/subnet if possible
- same key pair or a new key pair you control

Recommended inbound rules:

- `22` SSH
- `80` HTTP
- `443` HTTPS

Why:

- this recreates your production host with the smaller storage footprint

### 25.9 Re-associate the old Elastic IP if you had one
If you preserved an Elastic IP:

1. go to `Elastic IPs`
2. select the old IP
3. choose `Associate Elastic IP address`
4. attach it to the new instance

Why:

- the public address becomes the same as before
- your domain and CI/CD become easier to keep stable

### 25.10 Rebuild the server runtime
On the new instance, repeat the provisioning:

- install Docker
- install Nginx
- create `pms` user
- create `/opt/pms/app`
- create `/opt/pms/sqlserver`
- install Java 24

Why:

- the new EC2 is a fresh OS image
- none of the old runtime tools are there yet

### 25.11 Restore SQL Server
On the new server:

1. restore `docker-compose.yml`
2. start SQL Server container
3. restore the DB from `.bak` or import `.sql`

Why:

- your app cannot run correctly until DB is available

### 25.12 Restore application configuration
Restore these files:

- `/opt/pms/app/.env`
- `/etc/nginx/sites-available/pms`
- `/etc/systemd/system/pms.service`

Then:

- test `nginx -t`
- run `systemctl daemon-reload`

Why:

- this reuses your exact working production setup

### 25.13 Deploy the backend jar
You can deploy the app in either of two ways:

#### Manual way

- upload the `.jar`
- place it in `/opt/pms/app`
- `sudo systemctl restart pms`

#### CI/CD way

- update `EC2_HOST` if the IP changed
- push code to `main`
- let GitHub Actions deploy automatically

Why:

- CI/CD is faster if SSH access and secrets are already correct

### 25.14 Re-issue HTTPS if needed
If:

- IP changed
- server is new
- Nginx config was recreated

then run Certbot again for:

- `pms.druignguyen.me`

Why:

- a new instance usually needs its own runtime Nginx + certificate setup

### 25.15 Verify the new server
After restore, test all of these:

- `sudo systemctl status pms`
- `sudo systemctl status nginx`
- `sudo docker ps`
- `curl http://127.0.0.1:8080/health`
- `curl https://pms.druignguyen.me/health`

Why:

- confirms app, reverse proxy, DB, and HTTPS all work on the new instance

### 25.16 Update CI/CD secrets if the server IP changed
If the new instance uses a different public IP, update:

- `EC2_HOST`

If you generated a new SSH key, also update:

- `EC2_SSH_KEY`

Why:

- otherwise GitHub Actions will still deploy to the old host or use the wrong key

### 25.17 Best recommendation for your situation
For your exact case, I recommend this order:

1. back up `.env`, Nginx config, systemd service, Docker Compose, and DB
2. save those backups to your local machine
3. terminate the old `30 GiB` instance after backup is confirmed
4. create a new Ubuntu EC2 with `15 GiB`
5. reattach the same Elastic IP if you have one
6. restore DB and config
7. redeploy the app
8. test `https://pms.druignguyen.me/health`

### 25.18 Key conclusion
The real answer to your question is:

- **You cannot directly shrink the current 30 GiB EBS root volume to 15 GiB**
- **The correct fix is to migrate to a new 15 GiB instance**

This is not a bug in your setup; it is a normal AWS EBS limitation.

## 26. Click-by-click AWS Console checklist for moving to a new 15 GiB EC2

This section is the practical runbook for your exact case.

### 26.1 What you will keep and what you will rebuild
You should keep:

- database backup
- `.env`
- Nginx config
- `pms.service`
- SQL Server Docker Compose file
- domain `pms.druignguyen.me`
- GitHub repository and CI/CD workflow

You will rebuild:

- the EC2 machine itself
- the root volume
- the Docker / Java / Nginx runtime on the new machine

### 26.2 Phase A - Back up everything from the old EC2

#### A1. Back up app config from the old server
Use these commands on your local Windows machine to copy files down from EC2.

```powershell
scp -i "D:\AWS\pms-ec2-key.pem" ubuntu@<OLD_EC2_IP>:/opt/pms/app/.env .\pms-backup\.env
scp -i "D:\AWS\pms-ec2-key.pem" ubuntu@<OLD_EC2_IP>:/opt/pms/sqlserver/docker-compose.yml .\pms-backup\docker-compose.yml
scp -i "D:\AWS\pms-ec2-key.pem" ubuntu@<OLD_EC2_IP>:/home/ubuntu/property-management-system.sql .\pms-backup\property-management-system.sql
```

For system files, SSH in first and copy them to the Ubuntu home directory:

```bash
sudo cp /etc/nginx/sites-available/pms /home/ubuntu/pms.nginx.conf
sudo cp /etc/systemd/system/pms.service /home/ubuntu/pms.service
sudo chown ubuntu:ubuntu /home/ubuntu/pms.nginx.conf /home/ubuntu/pms.service
```

Then download them from Windows:

```powershell
scp -i "D:\AWS\pms-ec2-key.pem" ubuntu@<OLD_EC2_IP>:/home/ubuntu/pms.nginx.conf .\pms-backup\pms.nginx.conf
scp -i "D:\AWS\pms-ec2-key.pem" ubuntu@<OLD_EC2_IP>:/home/ubuntu/pms.service .\pms-backup\pms.service
```

Why:

- these files let you restore the exact production behavior on the new server

#### A2. Back up the SQL Server database
SSH into the old EC2 and run:

```bash
sudo docker exec -it sqlserver mkdir -p /var/opt/mssql/backup
sudo docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrongSA@2026' -C -Q "BACKUP DATABASE [PropertyManagementDB_Export] TO DISK = N'/var/opt/mssql/backup/PropertyManagementDB_Export.bak' WITH INIT, STATS = 10"
sudo docker cp sqlserver:/var/opt/mssql/backup/PropertyManagementDB_Export.bak /home/ubuntu/PropertyManagementDB_Export.bak
ls -lh /home/ubuntu/PropertyManagementDB_Export.bak
```

Then download it to Windows:

```powershell
scp -i "D:\AWS\pms-ec2-key.pem" ubuntu@<OLD_EC2_IP>:/home/ubuntu/PropertyManagementDB_Export.bak .\pms-backup\PropertyManagementDB_Export.bak
```

Why:

- this preserves the real current data, not just the original import script

#### A3. Optional backup of the running jar
```powershell
scp -i "D:\AWS\pms-ec2-key.pem" ubuntu@<OLD_EC2_IP>:/opt/pms/app/property-management-0.0.1-SNAPSHOT.jar .\pms-backup\property-management-0.0.1-SNAPSHOT.jar
```

Why:

- useful as an emergency fallback if needed

### 26.3 Phase B - Check whether you use Elastic IP
In AWS Console:

1. Open `EC2`
2. In the left menu, click `Elastic IPs`
3. Check whether your current production IP is an Elastic IP or just a temporary public IP

If yes:

- note down that Elastic IP address
- you will later re-associate it to the new instance

If no:

- you will update Cloudflare DNS later
- you will also update GitHub secret `EC2_HOST`

### 26.4 Phase C - Terminate the old instance to free storage quota
Only do this after confirming all backups exist on your Windows machine.

In AWS Console:

1. Open `EC2`
2. Click `Instances`
3. Select the old instance
4. Click `Instance state`
5. Click `Terminate instance`
6. Confirm termination

Then verify the volume is gone or marked for deletion.

To check:

1. Open `EC2`
2. Click `Volumes`
3. Confirm the old `30 GiB` root volume is deleted

If it still remains:

1. Select the old volume
2. Click `Actions`
3. Click `Delete volume`
4. Confirm

Why:

- this frees the EBS storage quota so you can create a new `15 GiB` instance

### 26.5 Phase D - Create the new EC2 instance with 15 GiB
In AWS Console:

1. Open `EC2`
2. Click `Instances`
3. Click `Launch instances`
4. Under `Name and tags`, enter a name like `pms-web-15g`
5. Under `Application and OS Images`, choose the same Ubuntu AMI family you used before
6. Under `Instance type`, choose the type you want, for example `t3.micro` if appropriate
7. Under `Key pair (login)`, choose your existing SSH key pair
8. Under `Network settings`, choose:
   - same VPC as before if possible
   - subnet that allows public access
   - enable public IP if needed
9. Under `Security group`, either:
   - reuse the old security group
   - or create one with inbound rules for `22`, `80`, `443`
10. Under `Configure storage`, change the root volume size to:
   - `15 GiB`
11. Click `Launch instance`

Why:

- this gives you a fresh server with the smaller disk size

### 26.6 Phase E - Re-associate Elastic IP if you had one
If you had an Elastic IP, in AWS Console:

1. Open `EC2`
2. Click `Elastic IPs`
3. Select the old Elastic IP
4. Click `Actions`
5. Click `Associate Elastic IP address`
6. For `Resource type`, choose `Instance`
7. Choose the new instance
8. Confirm association

Why:

- your domain and GitHub Actions can keep using the same public IP

### 26.7 Phase F - SSH into the new instance and rebuild the runtime
After the new instance is running, SSH in:

```powershell
ssh -i "D:\AWS\pms-ec2-key.pem" ubuntu@<NEW_EC2_IP>
```

Then repeat the runtime setup:

- install Docker
- install Nginx
- create `pms` user
- create `/opt/pms/app`
- create `/opt/pms/sqlserver`
- install Java 24

This is the same provisioning flow you already followed before.

### 26.8 Phase G - Upload backup files to the new instance
From Windows:

```powershell
scp -i "D:\AWS\pms-ec2-key.pem" .\pms-backup\.env ubuntu@<NEW_EC2_IP>:/home/ubuntu/.env
scp -i "D:\AWS\pms-ec2-key.pem" .\pms-backup\docker-compose.yml ubuntu@<NEW_EC2_IP>:/home/ubuntu/docker-compose.yml
scp -i "D:\AWS\pms-ec2-key.pem" .\pms-backup\pms.nginx.conf ubuntu@<NEW_EC2_IP>:/home/ubuntu/pms.nginx.conf
scp -i "D:\AWS\pms-ec2-key.pem" .\pms-backup\pms.service ubuntu@<NEW_EC2_IP>:/home/ubuntu/pms.service
scp -i "D:\AWS\pms-ec2-key.pem" .\pms-backup\PropertyManagementDB_Export.bak ubuntu@<NEW_EC2_IP>:/home/ubuntu/PropertyManagementDB_Export.bak
```

Then move them into place on the new server:

```bash
sudo mv /home/ubuntu/.env /opt/pms/app/.env
sudo chown pms:pms /opt/pms/app/.env
sudo chmod 600 /opt/pms/app/.env

sudo mkdir -p /opt/pms/sqlserver
sudo mv /home/ubuntu/docker-compose.yml /opt/pms/sqlserver/docker-compose.yml

sudo mv /home/ubuntu/pms.nginx.conf /etc/nginx/sites-available/pms
sudo ln -sf /etc/nginx/sites-available/pms /etc/nginx/sites-enabled/pms

sudo mv /home/ubuntu/pms.service /etc/systemd/system/pms.service
sudo systemctl daemon-reload
```

### 26.9 Phase H - Restore SQL Server
On the new instance:

1. start SQL Server container again from `/opt/pms/sqlserver/docker-compose.yml`
2. copy backup file into container
3. restore database

Commands:

```bash
cd /opt/pms/sqlserver
sudo docker compose up -d
sudo docker cp /home/ubuntu/PropertyManagementDB_Export.bak sqlserver:/var/opt/mssql/backup/PropertyManagementDB_Export.bak
sudo docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrongSA@2026' -C -Q "RESTORE DATABASE [PropertyManagementDB_Export] FROM DISK = N'/var/opt/mssql/backup/PropertyManagementDB_Export.bak' WITH REPLACE, MOVE 'PropertyManagementDB_Export' TO '/var/opt/mssql/data/PropertyManagementDB_Export.mdf', MOVE 'PropertyManagementDB_Export_log' TO '/var/opt/mssql/data/PropertyManagementDB_Export_log.ldf', STATS = 10"
```

Important:

- if the logical file names inside the `.bak` are different, first run:

```bash
sudo docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrongSA@2026' -C -Q "RESTORE FILELISTONLY FROM DISK = N'/var/opt/mssql/backup/PropertyManagementDB_Export.bak'"
```

Then replace the `MOVE '...'` names accordingly.

### 26.10 Phase I - Deploy the backend app again
You can now either:

#### Option 1: Deploy manually
Upload the jar and restart service.

#### Option 2: Use CI/CD
If IP changed, update GitHub secret:

- `EC2_HOST`

Then push to `main` and let GitHub Actions deploy.

Why:

- if Elastic IP stayed the same, CI/CD may continue working without changes

### 26.11 Phase J - Reconfigure HTTPS if needed
If the new machine does not yet have a working certificate, run Certbot again:

```bash
sudo apt update
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d pms.druignguyen.me
```

Then test:

```bash
curl https://pms.druignguyen.me/health
```

### 26.12 Phase K - Final verification
Check these on the new instance:

```bash
sudo systemctl status pms
sudo systemctl status nginx
sudo docker ps
curl http://127.0.0.1:8080/health
curl https://pms.druignguyen.me/health
```

Expected result:

- app is `active (running)`
- SQL Server container is up
- health endpoint returns JSON with `status=UP`

### 26.13 Fastest safe path for your situation
If you want the shortest practical plan, do this exact order:

1. download `.env`, `docker-compose.yml`, `pms.service`, `pms.nginx.conf`
2. create SQL Server `.bak` and download it
3. verify backup files exist on Windows
4. terminate old `30 GiB` instance and delete old volume
5. create new EC2 with `15 GiB`
6. reattach Elastic IP if you have one
7. reinstall runtime tools
8. restore SQL Server
9. restore app config
10. redeploy jar or push via GitHub Actions
11. test `https://pms.druignguyen.me/health`
