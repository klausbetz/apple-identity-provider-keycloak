# Running Keycloak and Apple Identity Provider in a container

The following example `Dockerfile` specifies the Docker image to build Keycloak with Apple Identity Provider extension:

```Dockerfile
FROM quay.io/keycloak/keycloak:22.0.1 as builder

ENV KC_HEALTH_ENABLED=true
ENV KC_FEATURES=token-exchange
ENV KC_DB=postgres
ENV KC_HTTP_RELATIVE_PATH="/auth"

# Install custom providers

# Apple Social Identity Provider - https://github.com/klausbetz/apple-identity-provider-keycloak
ADD --chmod=755 https://github.com/klausbetz/apple-identity-provider-keycloak/releases/download/1.7.0/apple-identity-provider-1.7.0.jar /opt/keycloak/providers/apple-identity-provider-1.7.0.jar

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
