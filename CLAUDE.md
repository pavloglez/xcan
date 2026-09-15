# XCan Project Instructions for Claude

When working on this project, you MUST read and adhere to the established project rules and documentation. Do NOT hallucinate architectures, dependencies, or UI patterns. 

## Mandatory Reading Before Any Task:
1. **`.agents/AGENTS.md`**
   This is the single source of truth for the project's identity, tech stack, and core rules (MVI, Compose guidelines, etc.). **You must read this file before starting any work.**
   
2. **Module `README.md` files**
   This is an 11-module Android project. Before modifying or adding code in any module (e.g., `core/` or `feature/`), you must read the `README.md` located at the root of that module's directory to understand its architectural boundaries and dependencies.

3. **`.agents/skills/`**
   When working on UI components, transitions, or animations, consult the skill files located in this directory for the required Emil Kowalski design system and UI engineering principles.

## Development & Verification Standards:
- **Test Command:** Always verify changes with `./gradlew test --daemon`.
- **Architectural Pattern:** Unidirectional Data Flow (MVI) with `StateFlow` and pure immutable state.
- **Dispatchers:** Always inject `DispatcherProvider` from `:core:model`; never hardcode `Dispatchers.IO`.
- **UI Architecture:** Use `LocalHazeState` for glassmorphic cards and `Modifier.bounceClick()` / `Modifier.pressBounce()` for touch feedback.

