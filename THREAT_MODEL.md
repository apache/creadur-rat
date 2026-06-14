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
- **Status:** DRAFT — under maintainer review (2026-06-10). Not yet ratified.
- **Reporting cross-reference:** §8-violating findings via the ASF security
  process ([`SECURITY.md`](SECURITY.md)); §3/§9 findings closed citing this doc.
- **Provenance legend:** *(documented)* / *(maintainer)* / *(inferred)* — each
  *(inferred)* has a §14 open question.
- **Draft confidence:** ~14 documented / 0 maintainer / 16 inferred.

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
case the model cares about. *(inferred — Q1.)*

**Component families.**

| Family | Entry point | Untrusted-input exposure | In model? |
| --- | --- | --- | --- |
| File walking + license matching | `Reporter`, walkers | scanned file **content/paths** | **Yes** |
| **XML configuration reader** | `XMLConfigurationReader` | the **config** (if attacker-supplied) | **Yes** (XXE surface) |
| **Archive walker** | `ArchiveWalker` | archives in the tree (zip/jar/tar) | **Yes** (decompression-bomb surface) |
| CLI / Ant task / Maven plugin | wrappers | invocation args (trusted caller) | wrappers — trusted |
| Whisker / Tentacles | their CLIs | same dev-tool profile | sibling — §2 note |

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
  **no network connections** and runs no services. *(inferred — Q2, the
  no-network claim is high-value to confirm.)*
- The XML parser behaviour depends on the platform JAXP unless RAT configures it
  (§5a/§8). *(inferred — Q3.)*

## §5a Build-time and configuration variants

RAT has no security-mode flag. The security-relevant configuration is whether
its **XML config parser disables DOCTYPE/external entities** and whether the
**archive walker bounds decompression** (depth/size/entry count). Both are
hardcoded behaviours, not operator knobs — confirmed in §14. There is no
"insecure default toggle"; the question is simply what the parser/walker do by
default. *(inferred — Q3/Q4.)*

## §6 Assumptions about inputs

| Input | Attacker-controllable? (untrusted-run) | Concern |
| --- | --- | --- |
| scanned file content | **yes** | parsed/read; resource use |
| scanned file paths / archive entry names | **yes** | path handling on archive extraction |
| archives (zip/jar/tar) in the tree | **yes** | decompression bomb / nested-archive depth |
| RAT XML configuration | **maybe** (only if config is attacker-supplied) | XXE / external entity |
| invocation arguments | no — trusted caller | — |

## §7 Adversary model

- **In scope:** the party who controls the files/archives/config that an
  *untrusted-input* RAT run processes — e.g. a contributor whose PR is audited
  by CI, or the author of a third-party artifact being audited. Capabilities:
  craft a malicious archive (zip bomb), a hostile XML config (XXE), or
  pathological file content. *(inferred — Q1.)*
- **Out of scope:** an attacker who controls the RAT invocation or the trusted
  source tree (the normal case — they already own the build).

## §8 Security properties the project provides

1. **Bounded resource use on untrusted archives** — the archive walker should
   not allow a small input to cause unbounded CPU/memory (decompression-bomb /
   nested-archive defence). *Violation:* OOM/hang from a crafted archive.
   *Severity:* security (DoS) when RAT audits untrusted input. *(inferred —
   Q4: confirm whether bounds exist; this may be a §8 property or a §9 gap.)*
2. **Safe XML configuration parsing** — the config reader should reject
   DOCTYPE/external entities (no XXE). *Violation:* file read / SSRF via a
   crafted config. *Severity:* critical when config is untrusted. *(inferred —
   Q3: confirm DOCTYPE handling — may be §8 or §9.)*
3. **No ambient network/side effects** — RAT does filesystem I/O only.
   *Violation:* unexpected outbound connection. *(inferred — Q2.)*

(Whether items 1–2 are *provided* properties or *disclaimed* gaps depends on the
maintainer's answers in §14; they are listed here as the relevant questions.)

## §9 Security properties the project does *not* provide

- **No safety guarantee when run on fully untrusted input without sandboxing**,
  if the §14 answers reveal the XML parser/archive walker are not hardened. In
  that case: treat RAT-on-untrusted-input as you would any parser — sandbox it.
- **It is not a security/vulnerability scanner** (§3); a clean RAT report says
  nothing about security.
- **Well-known classes (parser/archive tools):** XXE via configuration,
  decompression bombs / nested-archive blowup, and path handling on archive
  entries — the standard risks of any tool that parses XML and descends into
  archives.

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

**Wave 1 — the load-bearing ones.**

- **Q1.** Confirm the intended trust posture: RAT runs in-process for a
  trusted caller; inputs are normally trusted, but the security-relevant case is
  RAT auditing **untrusted** input (CI on untrusted PRs, third-party artifacts).
  Is that the case you want modelled, or do you consider all RAT input trusted
  (which would move XXE/archive items to `OUT-OF-MODEL: trusted-input`)? (§2/§7.)
- **Q3.** Does `XMLConfigurationReader` disable DOCTYPE / external entities
  (XXE-safe)? If yes, §8 #2 stands; if no, it's a §9 gap + a §10 responsibility.
- **Q4.** Does `ArchiveWalker` bound decompression (size/depth/entry-count) so a
  crafted archive can't exhaust memory/CPU? §8 #1 vs §9 gap.

**Wave 2 — surface.**

- **Q2.** Confirm RAT makes no network connections and has no side effects beyond
  reading the scanned tree and writing the report. (§5/§8.)
- **Q5.** Whisker and Tentacles — same trust profile (in-process dev tool,
  inputs normally trusted)? Any input either processes that this RAT model
  doesn't cover (e.g. Tentacles fetching/inspecting remote bundles)? (§2.)

**Wave 3 — coexistence.**

- **Q6.** This adds `THREAT_MODEL.md` + `SECURITY.md` + `AGENTS.md` to
  `creadur-rat`. Want matching pointer files (AGENTS.md → SECURITY.md → this
  model) added to `creadur-whisker` and `creadur-tentacles` so all three are
  discoverable, or will you add them? (§1/§15.)

## §15 Appendix — existing-policy back-map

No in-repo `SECURITY.md` exists today; this PR adds one (ASF security-process
pointer) plus `AGENTS.md`. Once the §14 answers land (especially Q3/Q4), the
§8/§9 split firms up and the same chain can be added to `creadur-whisker` and
`creadur-tentacles`.
