# DESIGN A1 — Firebase-primary auth unification (D-05, supervisor-reversed to KEEP Firebase)

Date: 2026-07-06 · Phase 3 · Status: DESIGN (implementation follows on isolated commits)

## Problem (D-05 recap)

Today every credential operation runs against BOTH identity systems and they must agree:
- `AuthRepositoryImpl.signInWithEmail` = Firebase `signInWithEmailAndPassword` **then** backend `POST /api/auth/login` with the same raw password. Two password stores that can (and do) drift: a password change on one side bricks the account on the other; web-created accounts don't exist in Firebase; Android-created accounts hold two independent password hashes.
- `register` / `signInWithGoogle` / `forgotPassword` are similarly dual-written.
- "Logged in" truth = backend JWT (`TokenManager`), while identity/verification truth = Firebase — the `currentUser` flow requires both to be non-null simultaneously.

## Decision (supervisor)

Firebase Auth is the SINGLE identity source (email/password + Google via Credential Manager).
The backend verifies Firebase-issued ID tokens; its own JWT remains the resource-session token.

## Design: token EXCHANGE, not middleware

The backend already has the exact pattern needed: `POST /api/auth/google` verifies a Google ID
token and issues the app JWT + `wadjet_refresh` cookie. We add the Firebase analog and stop
sending passwords to the backend at all.

### Backend (Wadjet-v3-beta, branch `feat/firebase-integration`, ADDITIVE only)

New endpoint: **`POST /api/auth/firebase`** — body `{ "id_token": "<Firebase ID token>" }`.

1. Verify with Firebase Admin SDK (`firebase_admin.auth.verify_id_token`), project `wadjet-android`.
   Init from `FIREBASE_PROJECT_ID` env (no service-account file needed for verify-only;
   `GOOGLE_APPLICATION_CREDENTIALS`/`FIREBASE_CREDENTIALS_JSON` supported optionally for future Admin use).
2. Extract `uid`, `email`, `email_verified`, `name`, `picture`, sign-in provider.
3. **Trust policy (prevents account takeover by unverified Firebase accounts):**
   - provider `google.com` → trusted (Google verified the email), or
   - `email_verified == true` → trusted.
   - Otherwise **403 `email_not_verified`** — no backend session for unverified users.
4. Find user by email (same linking rule `/auth/google` already uses). If provider is google.com,
   also match/set `google_id`. Create if missing: `auth_provider="firebase"`, `email_verified=True`.
5. Respond exactly like `/auth/login`: `{access_token, token_type, user}` + rotated `wadjet_refresh`
   cookie (+201 on creation). **No existing endpoint/middleware/dependency is touched** —
   `get_current_user`, web session flow, and all web behavior stay byte-identical.

No DB schema change (linking is by email, like `/auth/google`) → no Alembic risk, no SQLite
create_all drift on the deployed HF Space.

### Android (branch `audit/fable-2026-07-06`)

`AuthRepositoryImpl` becomes Firebase-only for credentials; the backend session is derived:

- **exchange(idToken)** (new private step): `POST api/auth/firebase` → store `access_token`
  (+ refresh cookie harvested by the existing `AuthInterceptor`). Everything downstream
  (Bearer header, 401 refresh, logout revoke) is unchanged.
- **signInWithEmail**: Firebase sign-in → reload verification → if verified: `getIdToken()` +
  exchange → success. If unverified: NO backend session; UI keeps the existing VERIFY_EMAIL gate
  (pure Firebase). When `checkEmailVerified()` passes, THEN exchange and emit AuthSuccess.
  (Fixes the pre-existing hole where a backend token was stored before the verify gate.)
- **register**: Firebase `createUserWithEmailAndPassword` + `sendEmailVerification` only.
  No backend call until verified (exchange happens post-verification, same path as above).
- **signInWithGoogle**: Credential Manager Google ID token → Firebase `signInWithCredential`
  → **Firebase** ID token → exchange at `/api/auth/firebase` (the raw Google token no longer
  goes to the backend; `/api/auth/google` remains for the web).
- **forgotPassword**: Firebase `sendPasswordResetEmail` only (backend passwords are no longer an
  app concern; backend call removed).
- **signOut**: unchanged (best-effort backend logout to revoke the refresh token, clear Room,
  Firebase signOut).
- **Session resilience**: `TokenAuthenticator` on `RefreshOutcome.Rejected` now attempts ONE
  re-exchange with a fresh Firebase ID token (`getIdToken(forceRefresh=true)`) before declaring
  the session dead — while the Firebase session lives, the app session self-heals.
- **isLoggedIn/currentUser**: unchanged shape; still keyed on the backend token presence, but the
  token can now only exist as a derivative of a Firebase identity, so the dual-authority
  disagreement disappears by construction.

### Migration / compatibility

- Android accounts created by the CURRENT app already exist in both stores with `email_verified`
  synced through Firebase → they just work (test account `wadjetaudit11803@…` verifies this E2E).
- **Web-created accounts** exist only in the backend. They can enter the app via Google Sign-In
  (auto-links by email) or a Firebase password reset. Optional bulk fix documented in
  FIREBASE_RUNBOOK.md: `firebase_admin.auth.import_users` supports importing the backend's
  bcrypt hashes directly (user-run script; NOT part of this change).
- Backend `/auth/login|register` remain fully functional for the web — untouched.

### Test plan

- Backend: pytest suite green + new tests for `/api/auth/firebase` (mocked verifier: verified /
  unverified / google-provider / new-user / existing-user linking).
- Android: unit tests for the new repository flow (Mockk’d FirebaseAuthManager + AuthApiService);
  emulator E2E: email/password sign-in + verify gate + exchange, sign-out, cold-start refresh,
  Rejected-refresh self-heal. Google Sign-In E2E requires Play-services credentials on the
  emulator + console SHA — implement + unit-verify, runtime marked PENDING-USER-CONSOLE.

### Risk & rollback

Isolated to: `AuthRepositoryImpl`, `AuthViewModel` (verify-then-exchange ordering),
`AuthApiService` (+1 method), `TokenAuthenticator` (+re-exchange hook), backend `auth.py`
(+1 route) + `app/auth/firebase.py` (new). Each side lands as its own commit(s), revertible
independently; the old backend endpoints stay live, so reverting Android alone restores today's
behavior exactly.
