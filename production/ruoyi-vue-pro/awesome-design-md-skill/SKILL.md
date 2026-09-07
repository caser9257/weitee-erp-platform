---
name: awesome-design-md
description: Use local DESIGN.md files from VoltAgent/awesome-design-md to apply real-world design systems to pages and components.
---

# Awesome Design MD

This skill gives local access to the cloned `VoltAgent/awesome-design-md` repository.

Use this skill when the user wants:

- a page or component to match a known product's design language
- a `DESIGN.md` reference selected for a UI build
- a quick way to inspect available design systems from the local clone

## Local Asset Path

The repository is installed here:

`C:\Users\Administrator\.codex\skills\awesome-design-md\assets\awesome-design-md`

The DESIGN.md collection is here:

`C:\Users\Administrator\.codex\skills\awesome-design-md\assets\awesome-design-md\design-md`

## Workflow

1. List available folders under `design-md`.
2. Choose the target site or product.
3. Read that folder's `DESIGN.md`.
4. Use it as the design reference for implementation.

## Notes

1. This is a wrapper skill around the upstream DESIGN.md collection.
2. The upstream repo is not a native Codex skill repo, so this wrapper is required for Codex to load it as a skill.
3. If the repo is updated later, refresh the local clone under `assets/awesome-design-md`.
