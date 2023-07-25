# Running Keycloak and Apple Identity Provider in a container

The following `Dockerfile` creates a pre-configured Keycloak image that enables the health and metrics endpoints, 
enables the token exchange feature, uses a PostgreSQL database and installs Apple Identity Provider extension:

```Dockerfile
FROM quay.io/keycloak/keycloak:22.0.1 as builder

ENV KC_HEALTH_ENABLED=true
ENV KC_FEATURES=token-exchange
ENV KC_DB=postgres
ENV KC_HTTP_RELATIVE_PATH="/auth"

# Install custom providers

# Apple Social Identity Provider - https://github.com/klausbetz/apple-identity-provider-keycloak
ADD --chown=keycloak:keycloak https://github.com/klausbetz/apple-identity-provider-keycloak/releases/download/1.7.0/apple-identity-provider-1.7.0.jar /opt/keycloak/providers/apple-identity-provider-1.7.0.jar

# build optimized image
RUN /opt/keycloak/bin/kc.sh build 

FROM quay.io/keycloak/keycloak:22.0.1

COPY --from=builder /opt/keycloak/ /opt/keycloak/
WORKDIR /opt/keycloak

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
```

Then, run:

```shell
docker build -t my-keycloak:latest .
```

References:

- https://www.keycloak.org/server/containers
