# Apple Identity Provider for Keycloak :apple:

This repository represents an extension for [Keycloak](https://www.keycloak.org), which
enables [Sign in with Apple](https://developer.apple.com/documentation/sign_in_with_apple) for web-based applications and native
application (via `token-exchange`).

### Apple vs. the rest of the world

Since Apple does not comply 100% to the existing `OpenID Connect` standard, some customizations are necessary in order to make the _Apple
way_
compatible to Keycloak. Differences are as follows:

- If scopes were requested, Apple sends the `TokenResponse` as a `POST` request
- There's no `userinfo` endpoint. `email`, `firstName` and `lastName` are transmitted only the first time the user signs in with Apple.
- The `/token`-request must contain a client-secret (JWT) which is signed using a specific private key (`.p8` file).

## General

This extension was tested with Keycloak 18.0 using docker. It's not yet compatible to the newer `Admin UI (admin2)` from Keycloak (which is
still a preview feature in v18.0).

## Installation

1. Make sure you have a copy of the latest JAR of this provider package.
2. Deploy the JAR file to keycloak (by placing it in `/opt/keycloak/providers` if you use docker; alternatively see
   this [guide](https://www.keycloak.org/docs/latest/server_development/index.html#registering-provider-implementations))
3. Keycloak might need a restart (or a whole new container when using docker)

## Configuration

Log into your Keycloak admin console and add `Apple` as new `Identity Provider` and get comfortable with the configuration options:

| Option         | Description                                                                                                                                                                                                     |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Service ID     | For web-clients this is usually the corresponding Service ID from Apple. For native clients (like iOS Apps, which just perform a token-exchange) this should be the app-identifier of the consuming native app. |
| Team ID        | Your Team ID obtained from your Apple developer account.                                                                                                                                                        |
| Key ID         | A key identifier obtained from your Apple developer account.                                                                                                                                                    |
| p8 Key         | Raw content of p8 key file you get from your Apple developer account.                                                                                                                                           |
| Default Scopes | Scopes to request from Apple (for web-based logins). defaults to `openid%20name%20email`                                                                                                                        |

:warning: Make sure to add the keycloak broker-URL (`https://<keycloak-url>/realms/<realm>/broker/apple/endpoint`) to your valid redirect
URLs in your Apple developer account.

## Token exchange

Token exchange can be used to trade an Apple `authorizationCode` for Keycloak access- and refresh-tokens.  
Following endpoint might be interesting for native apple sign on on e.g. iOS devices.

`<keycloak server url>/realms/<realm>/protocol/openid-connect/token`  
`application/x-www-form-urlencoded`

| Parameter         | Description                                                                                                                                                                                                                                               |
|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `client_id`       | the client id of your Keycloak client                                                                                                                                                                                                                     |
| `grant_type`      | `urn:ietf:params:oauth:grant-type:token-exchange`                                                                                                                                                                                                         |
| `subject_token`   | authorizationCode from Apple                                                                                                                                                                                                                              |
| `subject_issuer`  | `apple` (the name of the social provider in keycloak)                                                                                                                                                                                                     |
| `user_profile`    | `{ "name": { "firstName": string, "lastName": string }, "email": string }` the JSON string that Apple sends on the first login (only required for the first login)                                                                                        |
| `app_identifier`  | In case the configured Service ID doesn't match the app identifier of the native iOS app, this parameter can be used, so that Service ID is ignored and app_identifier is used instead (Apple might throw a client_id mismatch exception if not provided) |
