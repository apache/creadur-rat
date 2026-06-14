# Agent guidance

This file is read by automated agents (security scanners, code
analyzers, AI assistants) operating on this repository. It
points them at the human-authored references they should
consult before producing output.

# CODE STYLE
- Use consistent naming conventions
- Follow language-specific style guides
- Keep functions small and focused
- Use meaningful variable and function names
- Add comments for complex logic
- Add package-info.java to each new package in the source tree

# BEST PRACTICES
- **DRY (Don't Repeat Yourself)**: Avoid code duplication
- **SOLID Principles**: Follow object-oriented design principles
- **Error Handling**: Always handle potential errors gracefully
- **Security**: Consider security implications in your code
- **Version Control**: Write clear commit messages, referencing Apache Creadur Jira tickets if possible

# COMMUNICATION
- Explain your approach before implementing
- Break down complex solutions into steps
- Provide examples when helpful
- Ask clarifying questions when requirements are unclear

# RESTRICTIONS
- Always ask before making breaking changes
- Don't add unnecessary dependencies
- Follow the existing codebase patterns and conventions
- Test your solutions when possible with unit or integration tests

## Security

Security model: [SECURITY.md](./SECURITY.md), which links to the project's
threat model at [THREAT_MODEL.md](./THREAT_MODEL.md). Consult the threat model
for the project's in-scope / out-of-scope declarations and known non-findings
before reporting security issues.
