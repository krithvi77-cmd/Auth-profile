# AuthProfile — Project Specification & Reference

> Living document. Read this **first** before changing any auth/connection code.
> Last updated: 2026-04-22 (rev 2 — Steps 1, 2, 4 implemented)

## ⚠️ Authorization stance (today)

The product **will** have admin vs end-user roles in production. Today the
codebase has **no real session/role enforcement** — there is no login, every
request is implicitly trusted. The decision (2026-04-22) is to:

- design the **API shape** as if authorization existed (separate semantic
  endpoints, distinct payloads for system data vs user data, secrets never
  on the wire after creation),
- but **not implement** role checks yet,
- and mark every spot that *would* need a check with `// FUTURE: authz`
  comments so a future pass can grep them all.

When the real auth lands, fixing the call sites is mechanical — no shape
change needed.

This file is the source of truth for **what the product is**, **what data
crosses each boundary**, and **what invariants the code must protect**. It
exists so a developer (or AI assistant) coming in cold — without chat history
— can make correct decisions instead of refactoring blindly.

---

## 1. What the product is (in one paragraph)

A small Zoho-Flow-style authentication manager. An admin defines reusable
**Auth Profiles** (Basic Auth / OAuth v2 / API Key) describing *how* a target
app authenticates. End users then create **Connections** off a profile,
filling in only the user-specific credentials (their username/password, their
API key, or by completing an OAuth dance). Connections are referenced by
workflows. When a workflow needs to call the target app, it looks up the
connection's stored credentials and sends the request.

---

## 2. The two roles, and why this matters for data flow

| Role | Creates | Owns | Sees |
|---|---|---|---|
| **Admin / profile author** | Auth Profiles | The shape of the credential form: which fields, their labels, their placement, the OAuth `client_id` / `client_secret`, the authorization & token URLs | Everything they entered, including secrets |
| **End user / connection owner** | Connections | Their own credential values (their username, their API key, their OAuth tokens) | Only the *form fields* the admin defined — never the admin's `client_secret`, never another user's tokens |

**This is the core security boundary the code must enforce and currently does
not.** See §6.

---

## 3. Domain model

### 3.1 Tables (see `AuthConnection.sql`)

```
profiles               (id, name, auth_type, version, created_by, is_active, created_at)
profile_fields         (id, profile_id, key, label, field_type, default_value, is_custom, placement, position, ...)
connections            (id, profile_id, user_id, name, status, created_at)
connection_values      (id, connection_id, field_id, key, value, created_at)
                        UNIQUE (connection_id, field_id)
connection_oauth_values(id, connection_id, access_token, refresh_token, expires_at, created_at)
                        UNIQUE (connection_id)
```

### 3.2 What lives where

| Data | Table | Who supplied it | Sensitivity |
|---|---|---|---|
| Profile name, auth type | `profiles` | Admin | Public |
| Field schema (key, label, type, placement, position) | `profile_fields` | Admin | Public to users of the profile |
| **OAuth `client_id`** | `profile_fields.default_value` (key=`client_id`) | Admin | **Semi-secret** (should not leak to non-owners) |
| **OAuth `client_secret`** | `profile_fields.default_value` (key=`client_secret`) | Admin | **SECRET. Must never leave the server.** |
| Authorization URL, Token URL, Scopes, Redirect URI | `profile_fields.default_value` | Admin | Public |
| Connection name, status | `connections` | End user | Their own |
| Basic auth username/password | `connection_values` | End user | **Secret to that user** |
| API key value | `connection_values` | End user | **Secret to that user** |
| OAuth `access_token`, `refresh_token`, `expires_at` | `connection_oauth_values` | OAuth provider (via callback) | **Secret to that user** |

### 3.3 Auth type codes

```
1 = Basic Auth
2 = OAuth v2
3 = API Key
4 = JWT       (reserved, not implemented)
5 = No Auth   (reserved, not implemented)
```

These codes appear in `AuthenticationHandler` registry and `payloadMapper.authTypeName()`.

---

## 4. Invariants (these are RULES, not preferences)

These rules drive every design decision below. If a code change breaks one of
these, the change is wrong.

### Profile invariants

1. **Auth type is immutable after creation.** A Basic profile stays Basic
   forever. An OAuth profile stays OAuth forever. The data model conflates
   `auth_type` with the field set, so changing it would orphan or
   misinterpret rows.
2. **Field set is effectively immutable for built-in auth types.** Basic Auth
   *requires* `username` + `password`; OAuth *requires* `client_id`,
   `client_secret`, `authorization_url`, `access_token_url`. These cannot be
   removed. (API Key allows one custom field by design.)
3. A profile may be soft-deleted (`is_active = 0`) but existing connections
   off it must keep working until explicitly migrated.

### Connection invariants

4. **A connection's profile is immutable.** `connection.profile_id` cannot
   change. Already enforced in `ConnectionDAO.assertProfileUnchanged`.
5. **Therefore a connection's auth type is immutable** (follows from #1+#4).
6. **Therefore the set of `field_id`s for a connection is fixed** — every
   `(connection_id, field_id)` row exists from creation, so updates never
   need INSERT/DELETE on `connection_values`, only UPDATE.
7. A connection belongs to exactly one user (`user_id`). Other users must
   not read its credentials, edit it, or reconnect it. (Currently
   un-enforced — see §6.)

### Together these mean

- "Update structure" is not a real operation on a connection.
- "Reconnect" (refresh stored credential values) is the only credential-level
  operation users actually do post-creation.
- Profile editing should only ever change cosmetic things (display name).
  Anything structural is a new profile.

---

## 5. The four real use cases (this is the product)

### UC-1 · Admin creates an auth profile
Admin picks an auth method, fills in the profile-level config (Client ID/Secret
for OAuth, the API key parameter shape for API Key, nothing extra for Basic).
**The profile-level secrets are stored once, server-side, and shared by every
connection that uses this profile.**

`POST /api/profiles` with `{name, authType, fields:[...]}`.

### UC-2 · End user creates a connection
User picks an existing profile, gives the connection a name, and supplies
**only their personal credentials**:
- Basic → username + password
- API Key → the value of the one custom field the admin defined
- OAuth → *nothing typed*; the server returns `authorizeUrl`, the UI opens it
  in a popup, the provider redirects to `/api/connection/callback`, and the
  server exchanges the code for tokens.

`POST /api/connection` with `{name, authProfileId, values:{...}}`.

### UC-3 · End user reconnects a connection ← **this is the use case the user is asking about**
Months later, tokens have expired or the user rotated their API key. They go
to a **centralized "My Connections"** page (NOT inside a workflow), find the
broken connection, click **Reconnect**.

What "reconnect" actually means at the data layer:

| Auth type | What changes | What stays |
|---|---|---|
| Basic | `connection_values.value` for `username` and `password` (in-place UPDATE on existing rows) | `id`, `profile_id`, `name`, `field_id`s, `created_at` |
| API Key | `connection_values.value` for the one custom field | same as above |
| OAuth | `connection_oauth_values.access_token`, `refresh_token`, `expires_at` (UPDATE on existing row) | same as above |

**No DELETE. No INSERT (rows already exist). No new connection created. Same
`connection_id` so workflows referencing it keep working.** The only side
effect is `connections.status` flips back to `'active'` if it was `'failed'` or
`'inactive'`.

#### UC-3a · OAuth reconnect: refresh-token strategy

OAuth providers commonly cap how many active refresh tokens exist per
`(client, user)` pair (Google = 20, others vary). Re-authorising on every
reconnect would silently revoke older tokens and break sibling integrations
that share the same OAuth client. So OAuth reconnect follows a 4-tier
decision tree, all on the **same** `connection_id` and the **same**
`connection_oauth_values` row:

1. **`expires_at IS NULL` → STILL_VALID.** The provider didn't tell us when
   the token expires, so we treat it as non-expiring (e.g. some long-lived
   API tokens). No HTTP, no row change. Status stays `'active'`.
2. **`expires_at` in the future → STILL_VALID.** The token is still good.
   No HTTP. No row change. (This makes "Reconnect" idempotent — a user
   clicking it on a healthy connection is a no-op.)
3. **`expires_at` in the past AND `refresh_token` present → REFRESHED.**
   POST to `access_token_url` with `grant_type=refresh_token`. On 2xx,
   UPSERT the same row in place: `access_token` and `expires_at` are
   replaced; `refresh_token` is replaced **only if the response carried a
   new one** (handled by `COALESCE(VALUES(refresh_token), refresh_token)`
   in the SQL). Status flips to `'active'`.
4. **No `refresh_token`, OR refresh exchange returned 4xx /
   `error:invalid_grant` → NEEDS_AUTHORIZE.** Server returns
   `authorizeUrl`; UI opens the popup; the existing callback handler
   UPSERTs new tokens onto the **same row** (the table's
   `UNIQUE KEY (connection_id)` guarantees no duplicates). 5xx and network
   errors propagate as transient failures — the user can retry.

Implemented in `OAuthService.reconnect(int connectionId)`; outcome enum
`ReconnectOutcome { STILL_VALID, REFRESHED, NEEDS_AUTHORIZE }`. The servlet
maps STILL_VALID/REFRESHED → `{status:'active', refreshed:bool}` and
NEEDS_AUTHORIZE → `{status:'inactive', authorizeUrl:'...'}`.

### UC-4 · End user deletes a connection
Cascade-deletes `connection_values` and `connection_oauth_values`. Workflows
referencing it break — that's the user's choice.

There is **no "edit a connection's structure"** use case. There is no
"change a connection's profile" use case. There is no "change a connection's
auth type" use case. Anything that looks like one is really UC-3 or UC-4 in
disguise.

---

## 6. Data contracts: what crosses the wire (UI ↔ server)

This is the part that's currently **wrong** in the codebase and must be fixed.

### 6.1 Principles

1. **Server controls the wire.** UI gets only what it needs to render; never
   send anything just because we have it.
2. **Secrets never travel outbound after creation.** Once a `client_secret`
   or an `access_token` is stored, the server never includes it in any
   response. UI displays masked placeholders (`••••••••`) for password-type
   fields; if the user wants to change it, they retype it.
3. **System data vs user data are different shapes** and must use different
   endpoints / payload keys, even when they happen to share a table today.
4. **Authorization is enforced server-side per request.** Never trust a
   UI-supplied `userId`.

### 6.2 Endpoint-by-endpoint contract

#### `GET /api/profiles` — list profiles (for the Connection picker)
Response: array of profile *summaries*. Used to render the picker grid.
```jsonc
[
  { "id": 7, "name": "API-key2", "authType": 1, "isActive": true }
]
```
**Do NOT include** `fields` here. The picker only needs id + name + authType.
Today's code returns `fields` from `getById` only, which is correct — but
verify that future changes don't add fields to the list endpoint.

#### `GET /api/profiles/{id}` — load profile for **profile editor (admin)**
Returns full profile including all fields with `defaultValue`. **Admin-only.**
```jsonc
{
  "id": 2, "name": "OAuth2 Profile", "authType": 2,
  "fields": [
    { "id": 3, "key": "client_id",     "fieldType": "text",     "defaultValue": "oauth-client-id-123", "isCustom": false, "position": 1 },
    { "id": 4, "key": "client_secret", "fieldType": "password", "defaultValue": "••••••••",            "isCustom": false, "position": 2 },
    ...
  ]
}
```
**Critical fixes needed (today this endpoint leaks secrets):**
- For any field with `fieldType = "password"`, replace `defaultValue` with
  the sentinel `"••••••••"` (or `null` + a `hasValue: true` flag) so the
  actual secret never reaches the browser.
- Authorize: only the profile's `created_by` (or an admin role) may call this.

#### `GET /api/profiles/{id}/form-schema` — load profile for **connection form (end user)**  ← **NEW endpoint to add**
Returns *only* what the connection form needs to render: the visible field
list, **with all `defaultValue` stripped**. End users must never see the
admin's `client_secret`, regardless of role.
```jsonc
{
  "id": 2, "name": "OAuth2 Profile", "authType": 2,
  "fields": [
    { "key": "x-api-key", "label": "My API Key", "fieldType": "password", "isCustom": true, "placement": "header", "required": true }
  ]
}
```
For OAuth profiles this list is **empty** — the user types nothing; the
server will use its server-side `client_id`/`client_secret` to drive the OAuth
dance.

For Basic Auth this returns:
```jsonc
{ "fields": [
    { "key": "username", "label": "Username", "fieldType": "text",     "required": true },
    { "key": "password", "label": "Password", "fieldType": "password", "required": true }
]}
```

For API Key this returns the one custom field, label only, no value.

#### `POST /api/profiles` — create profile (admin)
Request:
```jsonc
{
  "name": "OAuth2 Profile",
  "authType": 2,
  "fields": [
    { "key": "client_id",     "fieldType": "text",     "defaultValue": "abc123",        "isCustom": false, "position": 1 },
    { "key": "client_secret", "fieldType": "password", "defaultValue": "shh",           "isCustom": false, "position": 2 },
    ...
  ]
}
```
Server stores `defaultValue` as system-level config. Server response masks
secrets the same way as `GET`.

#### `PUT /api/profiles/{id}` — update profile (admin)
**Restrict to non-structural changes.** Allowed: `name`. Forbidden:
`authType` change (return 400), removing required built-in fields, adding
fields that change auth semantics.

For password-typed fields, an empty/sentinel `defaultValue` in the request
means "keep existing"; a non-empty value means "replace". This lets the UI
re-PUT the profile without forcing the admin to retype the client secret
every time they fix a typo in the name.

#### `GET /api/connection` — list connections (end user, filtered by `user_id`)
This is the **centralized "My Connections" page** that powers UC-3.
```jsonc
[
  { "id": 1, "name": "api - connection", "profileId": 8, "profileName": "api",
    "authType": 3, "status": "active", "createdAt": "2026-04-21 11:50:36",
    "needsReconnect": false }
]
```
**Never include credential values** in this response. `needsReconnect` is a
derived flag (status != active, or OAuth token expired).

Authorization: filter `WHERE user_id = currentUser.id`. Currently the SQL
has no such WHERE — fix.

#### `GET /api/connection/{id}` — load connection (owner only)
Returns metadata + the *empty* form schema for re-render, **never the stored
credential values**. The reconnect form starts blank — user retypes
credentials they want to refresh.
```jsonc
{
  "id": 1, "name": "api - connection", "authProfileId": 8, "authType": 3,
  "status": "active", "createdAt": "...",
  "formSchema": { "fields": [ {...same shape as /form-schema...} ] }
}
```

#### `POST /api/connection` — create connection (UC-2)
Request (current shape is correct):
```jsonc
{ "name": "my conn", "authProfileId": 8, "values": { "x-api-key": "xyz" } }
```
Response:
```jsonc
{ "id": 12, "name": "my conn", "profileId": 8, "authType": 3, "status": "active" }
```
For OAuth, response also includes `authorizeUrl`. UI opens it in a popup.

`user_id` MUST be derived server-side from the session. Never accept it from
the body.

#### `POST /api/connection/{id}/reconnect` ← **NEW endpoint, replaces current PUT semantics for credential refresh**
Request (non-OAuth):
```jsonc
{ "values": { "username": "new-name", "password": "new-pass" } }
```
Server logic (pseudo):
```
1. load connection by id; 403 if user_id != currentUser
2. load profile (immutable)
3. for each (key, value) in body.values:
     find profile_field by key → get field_id
     UPDATE connection_values
        SET value = ?, key = ?
      WHERE connection_id = ? AND field_id = ?
4. UPDATE connections SET status = 'active' WHERE id = ?
```
No INSERT, no DELETE, no profile lookup change, no auth type touch. Partial
reconnect (some fields only) is allowed: missing keys are left untouched.

Response: same shape as `GET /api/connection/{id}`.

For OAuth reconnect:
```jsonc
POST /api/connection/{id}/reconnect    (no body, or empty body)
```
Response includes `authorizeUrl`. Same callback path
(`/api/connection/callback`). The callback already does an UPSERT on
`connection_oauth_values` (see `OAuthService.upsertOauth`) which is exactly
right for reconnect — no change needed there. Just make sure
`completeAuthorization` flips status to `'active'` (it does).

#### `PUT /api/connection/{id}` — rename only
After this refactor, PUT only changes `name` (and maybe `status` for
admin-driven disable). Credential refresh is **not** done here.

#### `DELETE /api/connection/{id}` — delete (UC-4)
Owner only. Cascade handled by FKs.

### 6.3 Quick map: who can see what

| Field | Admin (profile owner) | End user (connection owner) | Other user |
|---|---|---|---|
| Profile name, authType, field keys/labels/types/placement | ✅ | ✅ (via `/form-schema`) | ✅ if profile is shared |
| `client_id` (OAuth) | ✅ | ❌ (server uses it internally) | ❌ |
| `client_secret` (OAuth) | ❌ after creation (masked sentinel) | ❌ | ❌ |
| Connection list (own) | n/a | ✅ | ❌ |
| `connection_values.value` | ❌ | ❌ (write-only; user retypes to update) | ❌ |
| OAuth tokens | ❌ | ❌ | ❌ |

---

## 7. Current code: what's correct, what's broken

### ✅ Already correct
- DB schema with proper unique keys and cascades.
- `ConnectionDAO.assertProfileUnchanged` enforces invariant #4.
- `OAuthService.upsertOauth` is reconnect-safe by design.
- OAuth callback flips status to `'active'`.
- Connection POST returns `authorizeUrl` only for OAuth.
- `payloadMapper.toApiPayload` / `toUiPayload` separate UI shape from API shape.

### ✅ Fixed in rev 2 (2026-04-22)

1. ~~`GET /api/profiles/{id}` leaks `client_secret`~~ — masked at DAO level
   (`MASK_SENTINEL = "********"`). Server-internal callers use the new
   `getByIdUnmasked(id)`.
4. ~~`ProfileDAO.update()` allows `auth_type` change~~ — column dropped
   from UPDATE; mismatch → 400.
5. ~~`ProfileDAO.update()` does `DELETE FROM profile_fields` then
   re-inserts~~ — replaced with `updateFieldsInPlace` that preserves
   `field_id`. New keys / removed keys / `field_type` changes → 400.
6. ~~`AuthProfileForm.jsx` re-sends saved client_secret~~ — recognises
   `SECRET_MASK`, treats empty input as "keep existing".
2. ~~No reconnect endpoint exists~~ — `POST /api/connection/{id}/reconnect`
   added; `ConnectionDAO.reconnect(...)`.

### ❌ Still open (priority order)

3. **No user filtering anywhere** — DEFERRED per authz stance. All
   call-sites that will need it are tagged `// FUTURE: authz`.

7. **`ConnectionServlet.buildRedirectUri` is hardcoded** to a
   `zcodecorp.in` host. Step 7.

8. **`OAuthService.STATE_STORE` is in-memory** and never expires. Step 7.

9. **`AuthUtil.buildOauth` is dead code** — remove or document. Step 7.

10. ~~UI's Reconnect button still calls `connectionApi.update()`~~ — fixed
    in Step 5 below. OAuth reconnect now bypasses the form entirely and
    routes through `connectionApi.reconnect()`. Non-OAuth reconnect still
    opens the form (the user has to retype) but submits via
    `connectionApi.reconnect()`.

### ⚠️ Defensive cleanups (not bugs but smells)
- Both `Connection.values` (Map) and `Connection.fields` (List) coexist
  and silently sync via setters — pick one wire shape, drop the other.
- `validate(profile, conn)` and `save(...)` in each `Authenticator` repeat
  the field lookup (`AuthUtil.toMap`, `findField`). Memoize once per call.
- `ConnectionDAO.updateValue` falls back to INSERT — redundant given
  invariant #6. Leave it as a guard but log a warning when it fires.

---

## 8. Implementation plan (ordered, do not skip)

Each step is independently shippable.

### ✅ Step 1 — Lock the invariants in the DAO (DONE 2026-04-22)
- `ProfileDAO.update()`: dropped `auth_type` from UPDATE; rejects with
  `IllegalArgumentException` (→ 400) if incoming `authType` differs from
  stored.
- Replaced destructive `DELETE FROM profile_fields` + re-INSERT with a
  non-destructive `updateFieldsInPlace` that updates each row by
  `field_id`, preserving every `connection_values` row downstream.
- New helpers: `assertFieldSetUnchanged` (rejects added/removed keys and
  `field_type` changes), `getByIdUnmaskedNoTx` (transactional read of the
  pre-update state).
- `ProfileServlet` now maps `IllegalArgumentException` to 400 (was 500).

### ✅ Step 2 — Stop leaking secrets in profile GETs (DONE 2026-04-22)
- `ProfileDAO.MASK_SENTINEL = "********"`. `mapField(rs, maskSecrets=true)`
  swaps non-empty password defaults for the sentinel before they leave the
  DAO. `getById` uses masked reads.
- New `getByIdUnmasked(id)` for server-internal callers that genuinely
  need the real client_secret (token exchange). All such callers
  (`OAuthService.completeAuthorization`, `ConnectionServlet` POST + PUT)
  switched to it.
- `ProfileDAO.update()`'s `resolveDefaultValueForUpdate` treats incoming
  null/empty/sentinel as "keep stored value" for password-typed fields,
  so the UI can re-PUT without round-tripping secrets.
- `AuthProfileForm.jsx`: recognises `SECRET_MASK`, surfaces the input as
  empty with a placeholder + hint ("A secret is already saved. Leave this
  empty to keep it, or type a new value to replace it."), and relaxes the
  "Client Secret required" rule on edit when a secret is already stored.

### Step 3 — Split GET into two endpoints (DEFERRED)
- The masked single-endpoint design from Step 2 is good enough until real
  authz lands. When admin/end-user roles exist, add
  `GET /api/profiles/{id}/form-schema` returning only public metadata
  (zero defaultValues, even masked) for the connection form, and keep the
  current endpoint admin-only. ConnectionForm switches to `/form-schema`.

### ✅ Step 4 — Add reconnect endpoint (DONE 2026-04-22)
- `POST /api/connection/{id}/reconnect` handled in `ConnectionServlet`:
  - non-OAuth: body `{ "values": { key: value, ... } }`; partial reconnect
    allowed; missing keys left untouched.
  - OAuth: body ignored, response `{ authorizeUrl }`. Existing callback
    upserts tokens & flips status to active.
- `ConnectionDAO.reconnect(profile, connectionId, newValues)`: profile
  lookup is forced from the stored `profile_id` (never the body), per-key
  in-place UPDATE on `connection_values`, status flipped to `'active'` on
  commit. Throws if profile mismatch or OAuth misuse.
- Frontend: new `connectionApi.reconnect(id, values)`. **The existing
  Reconnect button still routes through `connectionApi.update()`** — that
  rewire is part of Step 5 below to avoid changing two things at once.

### ✅ Step 5 — Wire Reconnect button + smart OAuth refresh (DONE 2026-04-22)
- **OAuth reconnect (`OAuthService.reconnect(connectionId)`)**: implements
  the 4-tier decision tree from UC-3a above. Returns `ReconnectOutcome`
  enum; never deletes the existing `connection_oauth_values` row; relies
  on the `UNIQUE KEY (connection_id)` to keep upserts idempotent.
- **`ConnectionServlet.handleReconnect`**: for OAuth, calls
  `oauthService.reconnect(id)` and returns either
  `{status:'active', refreshed:bool}` (STILL_VALID / REFRESHED) or
  `{status:'inactive', authorizeUrl}` (NEEDS_AUTHORIZE).
- **Frontend `App.handleReconnect`**: for OAuth, no longer opens the
  form — calls `connectionApi.reconnect(id, {})` directly. Opens the
  popup only when the server returns `authorizeUrl`. For non-OAuth,
  still opens the form (user must retype credentials) but form submit
  now routes through `connectionApi.reconnect()` (was `update()`).
- **DB migration**: idempotent `ALTER TABLE` block in `AuthConnection.sql`
  ensures `uk_connection_oauth_connection` exists on legacy DBs and
  dedupes any pre-existing duplicate rows. Without this, the UPSERT in
  `OAuthService.upsertOauth` silently degrades to plain INSERT.
- **`ConnectionDAO.update()` is intentionally NOT stripped yet** — no UI
  path reaches it after this change (rename happens nowhere in the UI
  today; reconnect routes through `/reconnect`). It's kept as defensive
  code but should be removed in a follow-up once we're confident no
  external caller depends on it.

### Step 6 — Authorization (DEFERRED — future product feature)
- See top-of-file authz stance. When implemented:
  - `RequestContext.currentUserId(req)` from session.
  - `WHERE user_id = ?` on `ConnectionDAO.list/getById`.
  - Owner check at the start of `update`/`reconnect`/`delete`.
  - Profile edit endpoints restricted to `created_by` or admin role.
- Code sites already marked with `// FUTURE: authz` — grep before
  starting.

### Step 7 — Operational hygiene (NOT DONE)
- Make redirect URI configurable (`web.xml` init-param or env var). Today
  it's hardcoded to a `zcodecorp.in` host.
- TTL on `OAuthService.STATE_STORE` (5–10 min) + scheduled eviction.
- Decide between `Connection.values` (Map) and `Connection.fields` (List)
  — keep one, drop the other.

---

## 9. Glossary

- **Profile**: the *template* for an authentication method (admin-owned).
- **Field**: a single input on the profile (e.g., `client_secret`).
- **Connection**: a *user's* live binding of a profile + their credentials.
- **Connection value**: one stored credential row keyed by `(connection_id, field_id)`.
- **Reconnect**: refresh credential values on an existing connection, in
  place, preserving its id and all workflow references.
- **System data**: configured by the profile author (URLs, client id/secret,
  field definitions). Not editable by end users.
- **User data**: supplied by the connection owner (their username, their
  API key, OAuth tokens received on their behalf). Not visible to anyone
  else.

---

## 10. When in doubt

If a proposed change would make any of the following easier:
- changing a connection's profile,
- changing a profile's auth type,
- showing stored credential values back in the UI,
- letting the UI specify `user_id`,

…it is wrong. Re-read §4 and §6.

