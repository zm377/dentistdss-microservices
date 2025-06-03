# Alternative SSL Fixes

If you prefer to keep SSL enabled in the API Gateway, here are alternative solutions:

## Option 1: Use Let's Encrypt Certificate

1. Install certbot:
```bash
sudo apt-get update
sudo apt-get install certbot
```

2. Generate certificate:
```bash
sudo certbot certonly --standalone -d api.mizhifei.press
```

3. Convert to PKCS12 format:
```bash
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/api.mizhifei.press/fullchain.pem \
  -inkey /etc/letsencrypt/live/api.mizhifei.press/privkey.pem \
  -out keystore.p12 \
  -name api-gateway \
  -passout pass:your-password
```

4. Copy to api-gateway/src/main/resources/certs/

## Option 2: Configure Trust Store for Self-Signed Certificates

Add to application-prod.yml:
```yaml
server:
  port: 443
  ssl:
    enabled: true
    key-store: file:/app/certs/keystore.p12
    key-store-password: ${CA_STOREPASS}
    key-store-type: PKCS12
    key-alias: api-gateway
    trust-store: file:/app/certs/truststore.p12
    trust-store-password: ${CA_STOREPASS}
    trust-store-type: PKCS12
    client-auth: none
```

## Option 3: Disable SSL Verification (Development Only)

Add JVM arguments to Dockerfile:
```dockerfile
ENTRYPOINT ["java", "-Dcom.sun.net.ssl.checkRevocation=false", "-Dtrust_all_cert=true", "-jar", "api-gateway.jar"]
```

## Option 4: Use Cloudflare or AWS ALB for SSL Termination

Configure your domain to use Cloudflare or AWS Application Load Balancer for SSL termination, then point to your API Gateway on port 8080.
