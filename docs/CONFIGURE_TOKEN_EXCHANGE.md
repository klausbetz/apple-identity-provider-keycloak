# Configure token-exchange in Keycloak

There are a few different types of token-exchange available in Keycloak.
The type needed for exchanging an external Apple token for a Keycloak token is **external token to keycloak token**.

## Dependencies

In order for token-exchange to work you need to enable `token-exchange` and `admin-fine-grained-authz` preview features in Keycloak (see this [Dockerfile](docs/README_docker_installation.md) or [here](https://www.keycloak.org/securing-apps/token-exchange)).

## Grant permissions to Apple identity provider

After enabling the features from above you need to grant the Apple identity provider the permission to exchange tokens.

### 1. Create a client with default settings (optional if you already have a client you want to use)

<img width="1730" alt="create new client" src="https://github.com/user-attachments/assets/b2445ef9-a1e4-4989-8daa-40aa6d0fdcac" />

### 2. Configure permissions and policies

1. Enable permissions on your client
<img width="1830" alt="enable permissions for client" src="https://github.com/user-attachments/assets/9829c225-2f3a-4e00-a08c-24ca6373a680" />

2. Create a token-exchange permission on your client
<img width="1764" alt="create token-exchange permission for client" src="https://github.com/user-attachments/assets/3904a0c2-6c59-4d8e-8d36-18f24e8a7705" />

3. Hit save and click into the **Policies** field to create a new policy.

4. Create a policy for your client
<img width="1654" alt="create policy for client" src="https://github.com/user-attachments/assets/e5d2824f-9f10-4f3e-bea7-95a83952b442" />

5. Navigate to the Apple Identity Provider and enable permissions
<img width="1373" alt="enable permissions on Apple IDP" src="https://github.com/user-attachments/assets/d4f826f9-07d4-4ef3-adaa-a27c07b02103" />

6. Create a token-exchange permission on the Apple Identity Provider
<img width="1393" alt="create token-exchange permission on Apple IDP" src="https://github.com/user-attachments/assets/c5fbad7c-14ad-4d9b-b25f-7f3ee6c37b86" />

---

**References**
- https://www.keycloak.org/securing-apps/token-exchange
