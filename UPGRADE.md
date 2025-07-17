# Upgrading Apple Identity Provider for Keycloak

Since breaking changes not only occur within Keycloak, but also in this extension, it is necessary to migrate data in rare cases after an extension upgrade.

### Upgrade from <1.14.0 to 1.14.0 or later
- The field p8-Key got dropped in favor of the field Client-Secret in the Admin-UI in version 1.14.0. Please paste the p8-Key into Client-Secret instead. Otherwise you will get the error `"invalid_client"` from Apple and login does not work.
