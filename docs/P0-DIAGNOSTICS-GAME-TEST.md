# P0 Diagnostics and Dedicated Server — Owner Game-Test Gate

## Automated acceptance
- An exception in one module callback does not stop other module callbacks.
- The third consecutive failure in the same callback phase disables only that module.
- A successful execution of that same callback phase clears its failure streak; successful callbacks in other phases do not mask it. Manual enable starts a fresh session.
- Client log: `config/skyop-client/skyop.log`, approximately 1 MiB with `skyop.log.1` rotation.
- Dedicated-server startup log: `config/skyop-server/skyop-server.log`.
- Universal JAR includes a dedicated-server entrypoint and scopes the existing mixin config to the client environment.
- Server bootstrap is startup diagnostics only. Server gameplay modules, Usage Gate, and a client/server version handshake are not part of this P0.

## Owner game checks (CI is not a substitute)
1. Open Diagnostics from ClickGUI with Home; verify module readiness, enabled/active state, errors and reasons.
2. Use COPY and paste the report; verify world/connection, runtime side, game mode, health, module states and last 20 log lines.
3. Use LOG and verify the log directory opens and rotation retains `skyop.log` plus `skyop.log.1`.
4. Reproduce Auto Totem no-op reasons: HP above threshold, occupied offhand, already-equipped totem, no carried totem, cooldown and creative/spectator. Attach report and `skyop.log`.
5. Reproduce Auto MLG: flat surface, horizontal drift in both X and Z directions, partial collision surface, unloaded landing column, predicted position beyond 4.5 blocks and aim/prediction mismatch. Confirm the temporary yaw/pitch points at the predicted surface and both values plus the original selected hotbar slot are restored on each exit, including a rejected first attempt followed by a retry. Attach report and `skyop.log`.
6. Install the JAR on a Fabric dedicated server. Verify startup without client mixins/classes and creation of the server log. This validates bootstrap only, not server gameplay.
7. Do not mark Auto Totem, Auto MLG or any other module `LULUS UJI GAME` without owner evidence, `latest.log`, report and `skyop.log`.
