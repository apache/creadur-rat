<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
# Apache Creadur (RAT) — Threat Model

## §1 Header

- **Project:** Apache Creadur — primarily **RAT (Release Audit Tool)**
  (`apache/creadur-rat`), with sibling tools **Whisker**
  (`apache/creadur-whisker`, license-documentation generator) and **Tentacles**
  (`apache/creadur-tentacles`, release-bundle analyzer). This model is written
  in `creadur-rat` and covers the Creadur dev-tool family; Whisker/Tentacles
  share RAT's trust profile (§2).
- **Written against:** `main`/`master` @ HEAD (2026-06).
- **Author:** ASF Security team, via the threat-model-producer rubric (Scovetta
  rubric) at the Creadur PMC's request (path 3).
- **Status:** DRAFT — all §14 questions answered in the PR #677 review
  (ottlinger, Claudenw; 2026-06-21); ready to ratify at the PMC's discretion.
- **Reporting cross-reference:** §8-violating findings via the ASF security
  process ([`SECURITY.md`](SECURITY.md)); §3/§9 findings closed citing this doc.
- **Provenance legend:** *(documented)* / *(maintainer)* / *(inferred)* — each
  *(inferred)* has a §14 open question.
- **Draft confidence:** ~14 documented / 16 maintainer / 1 inferred (all §14
  questions answered in the PR #677 review, 2026-06).

**What it is.** RAT is a **build-time / CLI license-auditing tool**: it walks a
source tree, matches files against configurable license/header definitions, and
reports unapproved or unknown licenses. It runs as a **CLI**, an **Ant task**,
or a **Maven plugin** — always **in the developer's or CI's own process**,
never as a network service. Whisker generates license documentation; Tentacles
inspects staged release bundles. None is a server.

## §2 Scope and intended use

Intended use: a project maintainer or CI job runs RAT over a codebase to verify
license compliance before a release or on each change. The two inputs are the
**tree being audited** (files, including archives RAT descends into) and the
**RAT configuration** (XML/text license + matcher definitions).

Caller trust level: the developer/CI invoking RAT is trusted. The **inputs are
normally trusted too** (your own source, your own config) — but RAT is
sometimes pointed at **untrusted input**: a CI job auditing an untrusted
contribution/PR, or auditing a downloaded third-party artifact. That is the
case the model cares about. *(maintainer — Claudenw, PR #677: confirmed; RAT
config is operator-trusted, the scanned files may be untrusted.)*

**Component families.**

| Family | Entry point | Untrusted-input exposure | In model? |
| --- | --- | --- | --- |
| File walking + license matching | `Reporter`, walkers | scanned file **content/paths** | **Yes** |
| **XML configuration reader** | `XMLConfigurationReader` | the **config** (if attacker-supplied) | **Yes** (XXE surface) |
| **Archive walker** | `ArchiveWalker` | archives in the tree (zip/jar/tar) | **Yes** (decompression-bomb surface) |
| CLI / Ant task / Maven plugin | wrappers | invocation args (trusted caller) | wrappers — trusted |
| **License-header insertion (write mode)** | `--addLicense` / editors | **modifies files in the audited tree** (operator-invoked) | trusted-input (§3) |
| Whisker / Tentacles | their CLIs | same dev-tool profile | sibling — §2 note |

**Note (PMC, review).** The CLI, Ant task, and Maven plugin front-ends are
generated from a common option core, so any security-relevant behaviour (or
gap) in that core transfers automatically to all three UIs — a finding in one
front-end's handling generally applies to all of them. *(maintainer.)*

## §3 Out of scope (explicit non-goals)

- **RAT as a security scanner.** RAT checks *license* compliance; it is **not**
  a vulnerability scanner or a security gate. "RAT didn't catch X security
  issue" is not in scope. *(documented — purpose.)*
- **Audit *correctness* as a security property.** A missed/false license match
  is a correctness bug, not a vulnerability (unless it crosses a resource bound,
  §8). *(inferred.)*
- **The build/CI environment** RAT runs in, and the trust of the source tree
  when RAT is deliberately run on your own (trusted) code — the dominant,
  intended case. Findings whose only impact requires running RAT on input you
  already trust are `OUT-OF-MODEL: trusted-input`.
- **Test resources** (the deliberately-odd license fixtures under
  `*/src/test/resources/`) — those are test data, not a target.
- **RAT's header-insertion / file-modification mode** (`--addLicense` and the
  editors) — RAT can *write* license headers into the audited files, mutating
  the tree. This is explicitly operator-invoked against the operator's own
  (trusted) sources; a run that modifies files the operator already controls is
  `OUT-OF-MODEL: trusted-input`. (Raised by the PMC in review — write mode is
  noted here so the boundary is explicit rather than silent.) *(maintainer.)*
- **Custom matchers / matcher extensions**
  (<https://creadur.apache.org/rat/license_def.html#Matchers>) — RAT lets the
  operator define custom matcher classes in its configuration, and a custom
  matcher sees the full text of every file selected for scanning. Because the
  matcher set is operator-defined configuration under the control of whoever
  runs RAT (not attacker-supplied), a custom matcher reading scanned text is
  `OUT-OF-MODEL: trusted-input` — the same posture as any operator-supplied
  extension code (cf. the write mode above). (Raised by the PMC in review.)
  *(maintainer — Claudenw.)*

## §4 Trust boundaries and data flow

The boundary is **the input RAT is pointed at** — files and configuration.
RAT's security questions only arise when that input is **untrusted**:

```
caller invokes RAT (CLI/Ant/Maven) on a directory + a config
   │ trusted invocation
   ▼
read configuration (XMLConfigurationReader) ── XXE surface if config is untrusted
walk tree -> for each file: read content, match licenses
   └─ ArchiveWalker descends into zip/jar/tar ── decompression-bomb / path surface if archive is untrusted
   ▼
report (approved / unapproved / unknown)
```

**Reachability precondition (triager's test):** a finding is in-model only if it
is triggered by **untrusted input** (a hostile file/archive/config) that a
*realistic* RAT deployment processes — e.g. CI auditing an untrusted PR. A
finding that requires the operator to feed RAT input they already control is
`OUT-OF-MODEL: trusted-input` (§3).

## §5 Assumptions about the environment

- A JRE; RAT reads the filesystem it is pointed at and writes a report. It opens
  **no network connections** and runs no services — RAT runs locally and only
  opens files. The one operator-reachable exception is an XSLT stylesheet using
  `xsl:include` to pull a remote resource; XSLT stylesheets are trusted,
  operator-controlled config, so that is `OUT-OF-MODEL: trusted-input` (§3).
  (Build tooling — Maven/Ant — may fetch dependencies, but RAT itself does not.)
  *(maintainer — Claudenw + ottlinger, PR #677.)*
- The XML parser behaviour depends on the platform JAXP and is configurable via
  the standard [JAXP system properties](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jaxp/jaxp.html);
  RAT disables external entities by default (§5a / §8 #2). *(maintainer —
  Claudenw, PR #677.)*

## §5a Build-time and configuration variants

RAT has no security-mode flag. The security-relevant configuration is whether
its **XML config parser disables DOCTYPE/external entities** and whether the
**archive walker bounds decompression** (depth/size/entry count). Both are
hardcoded behaviours, not operator knobs. The **archive walker does not bound
decompression** — it extracts entry contents into an in-memory buffer (Apache
Commons Compress `ArchiveStreamFactory`) with no size/depth/entry-count limit
(§8/§9, maintainer-confirmed). The XML config reader **disables external
entities**; DOCTYPE handling is further hardened by PR #679 (§8 #2). There is no
"insecure default toggle". *(maintainer — Claudenw, PR #677; hardening in #679.)*

## §6 Assumptions about inputs

| Input | Attacker-controllable? (untrusted-run) | Concern |
| --- | --- | --- |
| scanned file content | **yes** | parsed/read; resource use |
| scanned file paths / archive entry names | **yes** | reported as labels only — entries are read into memory, never extracted to disk, so no zip-slip / path-traversal-on-write surface *(maintainer)* |
| archives (zip/jar/tar) in the tree | **yes** | decompression bomb / nested-archive depth |
| RAT XML configuration | **maybe** (only if config is attacker-supplied) | XXE / external entity |
| invocation arguments | no — trusted caller | — |

## §7 Adversary model

- **In scope:** the party who controls the files/archives/config that an
  *untrusted-input* RAT run processes — e.g. a contributor whose PR is audited
  by CI, or the author of a third-party artifact being audited. Capabilities:
  craft a malicious archive (zip bomb), a hostile XML config (XXE), or
  pathological file content. *(maintainer — Claudenw, PR #677.)*
- **Out of scope:** an attacker who controls the RAT invocation or the trusted
  source tree (the normal case — they already own the build).

## §8 Security properties the project provides

1. **Bounded resource use on untrusted archives** — **not currently provided.**
   The archive walker (`ArchiveWalker`) uses Apache Commons Compress
   `ArchiveStreamFactory` and extracts entry contents into an **in-memory
   buffer** held until the document is processed, with no decompression /
   size / depth / entry-count bound — so a crafted archive can exhaust memory
   (OOM). This is therefore a **disclaimed gap (§9)** plus a downstream
   responsibility (§10), not a provided property. *(maintainer — confirmed by
   the Creadur PMC in PR #677 review, 2026-06.)*
2. **Safe XML configuration parsing (no XXE)** — **provided.** The config reader
   has **external entities disabled**; DOCTYPE handling is further hardened by
   PR #679. *Violation:* file read / SSRF via a crafted config. *Severity:*
   critical when config is untrusted. *(maintainer — Claudenw, PR #677; hardening
   in #679.)*
3. **No ambient network/side effects** — RAT does filesystem I/O only; it opens
   no network connections on default settings. (Sole exception: an
   operator-supplied XSLT `xsl:include` pointing at a remote resource — trusted
   config, `OUT-OF-MODEL`.) *Violation:* unexpected outbound connection.
   *(maintainer — Claudenw + ottlinger, PR #677.)*

(Item 1 is a disclaimed §9 gap per the maintainer's archive answer; item 2 is a
provided property — external entities disabled, with PR #679 hardening DOCTYPE.)

## §9 Security properties the project does *not* provide

- **No safety guarantee when run on fully untrusted input without sandboxing**,
  if the §14 answers reveal the XML parser/archive walker are not hardened. In
  that case: treat RAT-on-untrusted-input as you would any parser — sandbox it.
- **It is not a security/vulnerability scanner** (§3); a clean RAT report says
  nothing about security.
- **Decompression-bomb / archive resource exhaustion** — **confirmed not
  bounded.** Archives are extracted into an in-memory buffer with no
  size/depth/entry-count limit (Commons Compress `ArchiveStreamFactory`), so
  RAT pointed at untrusted archives can OOM. Runs over untrusted archives must
  be sandboxed / resource-limited (§10). *(maintainer.)*
- **Well-known classes (parser/archive tools):** decompression bombs /
  nested-archive blowup remain the live untrusted-archive risk. XXE via
  configuration is mitigated (external entities disabled, §8 #2). Path-traversal
  on archive entries does **not** apply: RAT reads entries into memory and never
  extracts them to disk, so an entry label like `bar/baz.zip#/junk.txt` is a
  report string, not a write path. *(maintainer — Claudenw, PR #677.)*

## §10 Downstream responsibilities

- When auditing **untrusted** input (CI on untrusted PRs, third-party
  artifacts), run RAT with resource limits / in a sandbox, and do not feed it
  attacker-controlled **configuration**.
- Keep RAT updated; pin the version in your build.
- For your own (trusted) source tree — the normal case — no special handling.

## §11 Known misuse patterns

- **Running RAT on untrusted archives/config in CI** without resource limits,
  expecting it to be hardened against decompression bombs / XXE.
- **Treating a clean RAT report as a security sign-off** (it is a license check).

## §11a Known non-findings (recurring false positives)

- **"RAT reads/parses files it is told to scan"** on a **trusted** tree — that
  is the function; `OUT-OF-MODEL: trusted-input` (§3/§6).
- **Odd/invalid license fixtures under `src/test/resources/`** — test data, not
  a target. `OUT-OF-MODEL: unsupported-component` (§3).
- **"RAT didn't detect a security vulnerability"** — out of purpose (§3).
- **XML parsing / archive reading flagged generically** without an untrusted-
  input path — non-finding unless the reachability precondition (§4) is met.

## §12 Conditions that would change this model

- RAT gaining a network surface or a server mode.
- A change to the XML parser hardening or archive-walker bounds (§5a/§8).
- A report unroutable to a §13 disposition → revise §8/§9.

## §13 Triage dispositions

| Disposition | Meaning | Licensed by |
| --- | --- | --- |
| `VALID` | A §8 property breaks via untrusted input on a realistic run. | §8, §6, §7 |
| `VALID-HARDENING` | A §11 misuse is too easy (e.g. no archive bound). | §11 |
| `OUT-OF-MODEL: trusted-input` | Requires RAT to process input the operator already trusts. | §6 |
| `OUT-OF-MODEL: adversary-not-in-scope` | Needs control of the RAT invocation/host. | §7 |
| `OUT-OF-MODEL: unsupported-component` | Test fixtures / out-of-purpose. | §3 |
| `BY-DESIGN: property-disclaimed` | "Not a security scanner", trusted-input runs. | §9 |
| `KNOWN-NON-FINDING` | Matches §11a. | §11a |
| `MODEL-GAP` | Unroutable. | triggers §12 |

## §14 Open questions for the maintainers

All wave-1/2/3 questions were answered by the Creadur PMC in the PR #677 review
(ottlinger, Claudenw; 2026-06) and folded above. Kept here as a resolved record.

- **Q1 — trust posture (answered, Claudenw).** Confirmed: RAT configuration
  (XSLT stylesheets, config files, license definitions, custom matchers) is
  trusted/operator-controlled; the scanned **files** may be untrusted (CI
  auditing a third-party PR/artifact). The attack surface is whatever can break
  out of the scanning stream under default settings. Folded into §2 / §7.
- **Q2 — no network (answered, Claudenw + ottlinger).** Confirmed: RAT opens no
  network connections; it only reads files. Sole exception is an operator-set
  XSLT `xsl:include` to a remote resource (trusted config → `OUT-OF-MODEL`).
  Build tooling (Maven/Ant) may fetch dependencies, but RAT itself does not.
  Folded into §5 / §8 #3.
- **Q3 — XXE / XML parser (answered, Claudenw).** External entities are
  **disabled** in the config reader; DOCTYPE handling is further hardened by
  PR #679. JAXP behaviour is configurable via the standard JAXP system
  properties. §8 #2 is now a **provided** property.
- **Q4 — archive bound (answered, PMC).** No bound — entries are read into an
  in-memory buffer (Commons Compress) with no size/depth/entry-count limit, and
  OOM is **not** guarded ("we probably should add a limit but at this time we do
  not"). Resolved as a §9 gap + §10 responsibility; §8 #1 is **not** provided.
  Entries are never extracted to disk, so there is no path-traversal-on-write
  surface (§6 / §9).
- **Q5 / Q6 — Whisker / Tentacles coexistence (answered, ottlinger).**
  Development on Whisker/Tentacles is low right now; the PMC prefers to **start
  with RAT** and add the sibling pointer files (AGENTS.md → SECURITY.md → model)
  to `creadur-whisker` / `creadur-tentacles` later, to reduce noise. This PR is
  therefore scoped to `creadur-rat`; the sibling chain is a deferred follow-up.

With every question resolved, this model is ready to move from DRAFT to ratified
at the PMC's discretion.

## §15 Appendix — existing-policy back-map

A basic `SECURITY.md` was introduced via #671 (ASF security-process pointer);
this PR **appends** the `AGENTS.md` → `SECURITY.md` → `THREAT_MODEL.md`
discoverability pointer to it and adds `AGENTS.md`. With every §14 question
answered — Q4 archive walker unbounded → §9 gap, Q3 external entities disabled +
PR #679 hardening DOCTYPE → §8 #2 provided — the §8/§9 split is settled. The same
pointer chain will be added to `creadur-whisker` and `creadur-tentacles` as a
deferred follow-up (§14 Q5/Q6).
