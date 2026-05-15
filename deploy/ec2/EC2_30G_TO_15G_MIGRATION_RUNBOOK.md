# EC2 Migration Runbook: 30 GiB Instance to 15 GiB Instance

## Purpose
This document guides you step-by-step to:

1. back up application and database data from the old EC2 instance
2. terminate the old `30 GiB` instance to free storage quota
3. create a new EC2 instance with `15 GiB`
4. restore the full system
5. make the site available again at `https://pms.druignguyen.me`

## Important limitation
You cannot directly shrink an existing EBS root volume from `30 GiB` to `15 GiB`.

AWS only supports:

- increasing an EBS volume size
- creating a new volume from a snapshot with size equal to or larger than the source volume

So the correct path is:

- backup
- terminate old instance
- create new instance with `15 GiB`
- restore

## Before you start

### What you need

- your old EC2 SSH key file on Windows
- access to AWS Console
- access to Cloudflare DNS
- access to your GitHub repository secrets
- enough local disk space on Windows to store backup files

### Example values used in this guide
Replace these placeholders with your real values:

- `<OLD_EC2_IP>`: old EC2 public IP
- `<NEW_EC2_IP>`: new EC2 public IP
- `<KEY_PATH>`: Windows path to `.pem` file
- `<DB_SA_PASSWORD>`: SQL Server `sa` password

Example:

- `<KEY_PATH>` = `D:\AWS\pms-ec2-key.pem`
- domain = `pms.druignguyen.me`
- app folder = `/opt/pms/app`
- SQL Server folder = `/opt/pms/sqlserver`
- DB name = `PropertyManagementDB_Export`

## Phase 1: Back up the old instance

### Step 1. Create a local backup folder on Windows
Run this in PowerShell on your Windows machine:

```powershell
mkdir .\pms-migration-backup -Force
```

### Step 2. SSH into the old EC2 instance
From Windows PowerShell:

```powershell
ssh -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>
```

### Step 3. Copy protected config files to the Ubuntu home directory
Run these on the old EC2 instance:

```bash
sudo cp /etc/nginx/sites-available/pms /home/ubuntu/pms.nginx.conf
sudo cp /etc/systemd/system/pms.service /home/ubuntu/pms.service
sudo chown ubuntu:ubuntu /home/ubuntu/pms.nginx.conf /home/ubuntu/pms.service
ls -l /home/ubuntu/pms.nginx.conf /home/ubuntu/pms.service
```

### Step 4. Verify app and SQL files exist
Still on the old EC2 instance, run:

```bash
ls -l /opt/pms/app/.env
ls -l /opt/pms/sqlserver/docker-compose.yml
ls -l /opt/pms/app/property-management-0.0.1-SNAPSHOT.jar
```

Then exit:

```bash
exit
```

### Step 5. Download app and server config files to Windows
Run these on Windows PowerShell:

```powershell
scp -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>:/home/ubuntu/pms.nginx.conf .\pms-migration-backup\pms.nginx.conf
scp -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>:/home/ubuntu/pms.service .\pms-migration-backup\pms.service
scp -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>:/opt/pms/sqlserver/docker-compose.yml .\pms-migration-backup\docker-compose.yml
scp -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>:/opt/pms/app/property-management-0.0.1-SNAPSHOT.jar .\pms-migration-backup\property-management-0.0.1-SNAPSHOT.jar
scp -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>:/opt/pms/app/.env .\pms-migration-backup\app.env
```

If `.env` fails because of permissions:

```powershell
ssh -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>
```

```bash
sudo cp /opt/pms/app/.env /home/ubuntu/app.env
sudo chown ubuntu:ubuntu /home/ubuntu/app.env
exit
```

```powershell
scp -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>:/home/ubuntu/app.env .\pms-migration-backup\app.env
```

### Step 6. Back up the SQL Server database
SSH into the old EC2 again:

```powershell
ssh -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>
```

Run:

```bash
sudo docker exec -it sqlserver mkdir -p /var/opt/mssql/backup
sudo docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P '<DB_SA_PASSWORD>' -C -Q "BACKUP DATABASE [PropertyManagementDB_Export] TO DISK = N'/var/opt/mssql/backup/PropertyManagementDB_Export.bak' WITH INIT, STATS = 10"
sudo docker cp sqlserver:/var/opt/mssql/backup/PropertyManagementDB_Export.bak /home/ubuntu/PropertyManagementDB_Export.bak
ls -lh /home/ubuntu/PropertyManagementDB_Export.bak
exit
```

### Step 7. Download the database backup to Windows
Run on Windows:

```powershell
scp -i "<KEY_PATH>" ubuntu@<OLD_EC2_IP>:/home/ubuntu/PropertyManagementDB_Export.bak .\pms-migration-backup\PropertyManagementDB_Export.bak
```

### Step 8. Verify your local backup folder
Run on Windows:

```powershell
Get-ChildItem .\pms-migration-backup
```

You should see at least:

- `app.env`
- `docker-compose.yml`
- `pms.nginx.conf`
- `pms.service`
- `PropertyManagementDB_Export.bak`
- `property-management-0.0.1-SNAPSHOT.jar`

## Phase 2: Note down network and DNS information

### Step 9. Check whether you are using an Elastic IP
In AWS Console:

1. open `EC2`
2. click `Elastic IPs`
3. check whether your current production IP is listed there

If yes:

- note the Elastic IP address
- you will re-associate it later to the new instance

If no:

- you will update Cloudflare DNS later
- you will also update GitHub Actions secret `EC2_HOST`

### Step 10. Note your security group
In AWS Console:

1. open `EC2`
2. click `Instances`
3. select the old instance
4. open the `Security` tab
5. note the security group name and rules

You will reuse or recreate equivalent inbound rules:

- SSH `22`
- HTTP `80`
- HTTPS `443`

## Phase 3: Terminate the old instance and free storage

### Step 11. Stop the old instance
In AWS Console:

1. open `EC2`
2. click `Instances`
3. select the old instance
4. click `Instance state`
5. click `Stop instance`
6. wait until the state becomes `Stopped`

### Step 12. Terminate the old instance
Only do this after confirming backup files exist on Windows.

In AWS Console:

1. select the old instance
2. click `Instance state`
3. click `Terminate instance`
4. confirm

### Step 13. Confirm old volume is deleted
In AWS Console:

1. open `EC2`
2. click `Volumes`
3. check whether the old `30 GiB` volume disappeared

If the volume still exists:

1. select the volume
2. click `Actions`
3. click `Delete volume`
4. confirm deletion

## Phase 4: Create the new EC2 instance with 15 GiB

### Step 14. Launch a new EC2 instance
In AWS Console:

1. open `EC2`
2. click `Instances`
3. click `Launch instances`
4. under `Name and tags`, set a name like `pms-web-15g`
5. under `Application and OS Images`, choose Ubuntu
6. under `Instance type`, choose the same or equivalent type you used before
7. under `Key pair (login)`, choose your existing key pair
8. under `Network settings`, choose:
   - same VPC if possible
   - allow public IP if needed
   - select or create a security group
9. configure inbound rules:
   - SSH `22`
   - HTTP `80`
   - HTTPS `443`
10. under `Configure storage`, change the root volume size to `15 GiB`
11. click `Launch instance`

### Step 15. Wait for the new instance to become running
In AWS Console:

1. go back to `Instances`
2. wait until the new instance state is `Running`
3. note its public IP if no Elastic IP is attached yet

## Phase 5: Re-associate public IP or update DNS

### Step 16A. If you have an Elastic IP
In AWS Console:

1. open `EC2`
2. click `Elastic IPs`
3. select the old Elastic IP
4. click `Actions`
5. click `Associate Elastic IP address`
6. choose the new instance
7. confirm

### Step 16B. If you do not have an Elastic IP
Update Cloudflare:

1. open Cloudflare DNS
2. find the `A` record for `pms`
3. replace old IP with `<NEW_EC2_IP>`
4. save

Then update GitHub secret `EC2_HOST` to the new public IP.

## Phase 6: Rebuild the server runtime on the new EC2

### Step 17. SSH into the new EC2
From Windows:

```powershell
ssh -i "<KEY_PATH>" ubuntu@<NEW_EC2_IP>
```

### Step 18. Update OS packages
On the new EC2:

```bash
sudo apt update
sudo apt upgrade -y
```

### Step 19. Install Docker
On the new EC2:

```bash
sudo apt install -y ca-certificates curl gnupg unzip zip
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo   "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu   $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable" |   sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable docker
sudo systemctl start docker
```

### Step 20. Install Nginx and Certbot
On the new EC2:

```bash
sudo apt install -y nginx certbot python3-certbot-nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

### Step 21. Create folders and runtime user
On the new EC2:

```bash
sudo adduser --disabled-password --gecos "" pms
sudo mkdir -p /opt/pms/app
sudo mkdir -p /opt/pms/sqlserver
sudo chown -R pms:pms /opt/pms
```

### Step 22. Install Java 24 for user `pms`
On the new EC2:

```bash
sudo su - pms
curl -s https://get.sdkman.io | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 24-open
java -version
exit
```

## Phase 7: Upload backups to the new EC2

### Step 23. Upload backup files from Windows
Run on Windows:

```powershell
scp -i "<KEY_PATH>" .\pms-migration-backup\app.env ubuntu@<NEW_EC2_IP>:/home/ubuntu/app.env
scp -i "<KEY_PATH>" .\pms-migration-backup\docker-compose.yml ubuntu@<NEW_EC2_IP>:/home/ubuntu/docker-compose.yml
scp -i "<KEY_PATH>" .\pms-migration-backup\pms.nginx.conf ubuntu@<NEW_EC2_IP>:/home/ubuntu/pms.nginx.conf
scp -i "<KEY_PATH>" .\pms-migration-backup\pms.service ubuntu@<NEW_EC2_IP>:/home/ubuntu/pms.service
scp -i "<KEY_PATH>" .\pms-migration-backup\PropertyManagementDB_Export.bak ubuntu@<NEW_EC2_IP>:/home/ubuntu/PropertyManagementDB_Export.bak
scp -i "<KEY_PATH>" .\pms-migration-backup\property-management-0.0.1-SNAPSHOT.jar ubuntu@<NEW_EC2_IP>:/home/ubuntu/property-management-0.0.1-SNAPSHOT.jar
```

### Step 24. Move config files into place on the new EC2
SSH into the new EC2 and run:

```bash
sudo mv /home/ubuntu/app.env /opt/pms/app/.env
sudo chown pms:pms /opt/pms/app/.env
sudo chmod 600 /opt/pms/app/.env
sudo mv /home/ubuntu/docker-compose.yml /opt/pms/sqlserver/docker-compose.yml
sudo mv /home/ubuntu/pms.nginx.conf /etc/nginx/sites-available/pms
sudo ln -sf /etc/nginx/sites-available/pms /etc/nginx/sites-enabled/pms
sudo mv /home/ubuntu/pms.service /etc/systemd/system/pms.service
sudo systemctl daemon-reload
sudo mv /home/ubuntu/property-management-0.0.1-SNAPSHOT.jar /opt/pms/app/property-management-0.0.1-SNAPSHOT.jar
sudo chown pms:pms /opt/pms/app/property-management-0.0.1-SNAPSHOT.jar
```

## Phase 8: Restore SQL Server

### Step 25. Start SQL Server container
On the new EC2:

```bash
cd /opt/pms/sqlserver
sudo docker compose up -d
sudo docker ps
```

### Step 26. Copy backup into the SQL Server container
On the new EC2:

```bash
sudo docker exec -it sqlserver mkdir -p /var/opt/mssql/backup
sudo docker cp /home/ubuntu/PropertyManagementDB_Export.bak sqlserver:/var/opt/mssql/backup/PropertyManagementDB_Export.bak
```

### Step 27. Inspect logical file names inside the backup
On the new EC2:

```bash
sudo docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P '<DB_SA_PASSWORD>' -C -Q "RESTORE FILELISTONLY FROM DISK = N'/var/opt/mssql/backup/PropertyManagementDB_Export.bak'"
```

### Step 28. Restore the database
Replace the logical names if needed, then run:

```bash
sudo docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P '<DB_SA_PASSWORD>' -C -Q "RESTORE DATABASE [PropertyManagementDB_Export] FROM DISK = N'/var/opt/mssql/backup/PropertyManagementDB_Export.bak' WITH REPLACE, MOVE 'PropertyManagementDB_Export' TO '/var/opt/mssql/data/PropertyManagementDB_Export.mdf', MOVE 'PropertyManagementDB_Export_log' TO '/var/opt/mssql/data/PropertyManagementDB_Export_log.ldf', STATS = 10"
```

### Step 29. Verify the database exists
On the new EC2:

```bash
sudo docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P '<DB_SA_PASSWORD>' -C -Q "SELECT name FROM sys.databases"
```

## Phase 9: Start the backend app

### Step 30. Test Nginx configuration
On the new EC2:

```bash
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl status nginx
```

### Step 31. Start the Spring Boot service
On the new EC2:

```bash
sudo systemctl enable pms
sudo systemctl restart pms
sudo systemctl status pms
```

### Step 32. Check application logs if needed
On the new EC2:

```bash
journalctl -u pms -n 100 --no-pager
```

## Phase 10: Restore HTTPS and domain access

### Step 33. Reissue HTTPS certificate if needed
If this is a fresh machine and certificate is not already valid:

```bash
sudo certbot --nginx -d pms.druignguyen.me
```

Then test:

```bash
curl https://pms.druignguyen.me/health
```

### Step 34. If browser still cannot access the site
Check these:

- Cloudflare `A` record points to the correct public IP
- security group allows `80` and `443`
- `nginx` is running
- `pms` service is running
- `docker` and SQL Server are running

## Phase 11: Restore CI/CD

### Step 35. Update GitHub Actions secret if IP changed
If your new server has a different IP, update in GitHub:

- `EC2_HOST`

If you changed SSH key, update:

- `EC2_SSH_KEY`

### Step 36. Test deployment again
Push to `main` or rerun GitHub Actions.

Expected flow:

- build jar
- upload jar
- restart `pms`
- healthcheck passes

## Final verification checklist
Run these on the new EC2:

```bash
sudo systemctl status pms
sudo systemctl status nginx
sudo docker ps
curl http://127.0.0.1:8080/health
curl https://pms.druignguyen.me/health
```

You are done when:

- `pms` is `active (running)`
- `nginx` is `active (running)`
- SQL Server container is up
- both health endpoints return `status=UP`
- browser can open `https://pms.druignguyen.me`

## Fastest safe sequence
If you want the shortest working order, follow exactly this:

1. back up `.env`, `docker-compose.yml`, `pms.service`, and Nginx config
2. back up SQL Server to `.bak`
3. download all backups to Windows
4. terminate old instance and delete old volume
5. create new EC2 with `15 GiB`
6. reattach Elastic IP or update Cloudflare DNS
7. install Docker, Nginx, Java 24
8. restore SQL Server
9. restore app config and jar
10. start Nginx and `pms`
11. restore HTTPS if needed
12. update GitHub secret `EC2_HOST` if IP changed
13. verify browser access to `https://pms.druignguyen.me`
