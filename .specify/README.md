# .specify — Wadjet Android Spec-Kit

> All project planning, architecture, and implementation docs for the Android ↔ Web parity effort.

## Structure

```
.specify/
├── README.md                       ← You are here
├── memory/
│   └── constitution.md             ← Governing principles (smart defaults, web parity, etc.)
└── specs/
    └── 001-logic-parity/
        ├── architecture.md         ← Module graph, tech stack, pipeline diagrams, Room schema
        ├── gap-analysis.md         ← Every bug/gap: B1-B5, G1-G10, M1-M8, A1-A12 + audit
        ├── smart-defaults.md       ← Why & how: scan=auto, write=smart, TTS=server, identify=auto
        ├── tasks.md                ← 10-phase implementation plan with task tables
        ├── phase-prompts.md        ← Copy-paste prompts for each phase
        └── contracts/
            └── api-contract.md     ← All 48 web API endpoints with exact schemas
```

## How to Use

1. **Starting a phase?** → Open `phase-prompts.md`, copy the prompt for that phase
2. **Need API details?** → Check `contracts/api-contract.md` (48 endpoints, full schemas)
3. **What's broken?** → Check `gap-analysis.md` (35 items, priority matrix)
4. **Architecture question?** → Check `architecture.md` (module graph, pipelines, Room schema)
5. **Design decisions?** → Check `constitution.md` (6 core principles) + `smart-defaults.md`
6. **Full task list?** → Check `tasks.md` (10 phases, dependency graph)

## Quick Reference

| Metric | Count |
|--------|-------|
| API endpoints | 48 |
| Android modules | 18 |
| Critical bugs | 5 (B1-B5) |
| Major gaps | 10 (G1-G10) |
| Minor issues | 8 (M1-M8) |
| Additional gaps | 12 (A1-A12) |
| Implementation phases | 10 (0-9) |
| Total tasks | ~55 |

## Core Principle
**Smart Defaults** — The user NEVER chooses modes. Scan=auto, Write=smart, TTS=server-picks, Identify=auto.
