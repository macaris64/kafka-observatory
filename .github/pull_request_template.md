# Summary
<!-- Briefly describe your changes. What problem does this solve? -->

# Related Issue / Step
<!-- Link to the issue or mention the step number e.g. "Step 3" -->

# Kafka Impact Checklist
<!-- Check all that apply. If not applicable, leave unchecked. -->
- [ ] **Admin**: Changes to topic creation, deletion, or configuration
- [ ] **Consumer**: Changes to how messages are consumed
- [ ] **Producer**: Changes to how messages are produced
- [ ] **None**: No changes to Kafka interactions

# Testing Checklist
<!-- How was this tested? -->
- [ ] **Unit Tests**: Added/Updated unit tests
- [ ] **Integration Tests**: Added/Updated Testcontainers-based tests
- [ ] **Manual Verification**: Verified locally (please describe below)

# Risk Assessment
<!-- Are there any risks? Schema changes? Breaking changes? -->
- [ ] Low Risk
- [ ] Medium Risk (requires careful review)
- [ ] High Risk (breaking changes or critical path)

# Architecture Compliance
<!-- Verify the following before merging -->
- [ ] Changes respect Hexagonal Architecture boundaries
- [ ] Kafka dependencies are restricted to adapters (no Kafka imports in domain)
- [ ] CI pipeline passes
