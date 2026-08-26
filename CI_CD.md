# CI/CD Pipeline Documentation

Complete GitHub Actions setup for automated testing, building, and deployment of MCP Users Server.

## Overview

The CI/CD pipeline includes:
- Automated testing on multiple Java versions
- Code quality analysis with SonarCloud
- Security scanning with OWASP Dependency Check
- Automated artifact publishing
- Release management
- Dependency updates with auto-merge
- Coverage reporting with Codecov

## Workflows

### 1. CI (ci.yml)
**Trigger**: Push to main/develop, Pull Requests
**Purpose**: Build, test, and quality analysis

#### Steps:
1. **Test Job** (Runs on matrix: Java 11, 17, 21)
   - Checkout code
   - Setup Java with cache
   - Build with Maven
   - Run all tests
   - Generate coverage report
   - Upload to Codecov

2. **Code Quality Job** (After test succeeds)
   - Build with Maven
   - Run tests with coverage
   - Check test results

3. **SonarCloud Job** (On push only)
   - Checkout with full history
   - Analyze code quality
   - Report to SonarCloud

4. **Publish Job** (On main branch push only)
   - Build JAR artifact
   - Create GitHub release
   - Upload artifact (retention: 30 days)

5. **Notify Job** (Final status)
   - Check all job statuses
   - Notify success/failure

### 2. Lint (lint.yml)
**Trigger**: Push to main/develop, Pull Requests
**Purpose**: Code linting and security checks

#### Jobs:
- **Lint Job**
  - SpotBugs analysis (optional)
  - Code formatting check (optional)
  - POM structure verification

- **Security Job**
  - OWASP Dependency Check
  - Vulnerable dependencies detection
  - SARIF upload to GitHub Security

### 3. Release (release.yml)
**Trigger**: Git tags starting with `v*`
**Purpose**: Build and publish releases

#### Steps:
1. **Build Release Job**
   - Checkout tagged commit
   - Build release JAR
   - Run tests
   - Create GitHub Release
   - Upload artifacts (retention: 90 days)

2. **Publish JavaDoc Job**
   - Generate JavaDoc documentation
   - Deploy to GitHub Pages
   - Configure custom domain (optional)

### 4. Auto-merge Dependabot (auto-merge-dependabot.yml)
**Trigger**: Pull requests from Dependabot
**Purpose**: Automatically merge dependency updates

#### Jobs:
- **Auto-merge Job**: Enable auto-merge with squash
- **Approve Job**: Approve the PR automatically

## Configuration Files

### .github/dependabot.yml
Automated dependency management:

```yaml
Maven Dependencies:
  - Schedule: Weekly (Monday, 3:00 AM)
  - Updates: Minor and patch versions
  - Ignored: Major version updates (manual review)
  - Limits: 10 open PRs max

GitHub Actions:
  - Schedule: Weekly (Monday, 4:00 AM)
  - Updates: All versions
  - Limits: 5 open PRs max
```

### .github/CODEOWNERS
Defines code ownership and review requirements:
- `@vicboma1` - Default owner for all code
- Specific paths assigned to maintainers

### Issue Templates

#### Bug Report (.github/ISSUE_TEMPLATE/bug_report.md)
- Description and reproduction steps
- Expected vs actual behavior
- Environment details
- Screenshots/logs

#### Feature Request (.github/ISSUE_TEMPLATE/feature_request.md)
- Feature description
- Motivation and use cases
- Proposed solution
- Alternative solutions

### Pull Request Template (.github/PULL_REQUEST_TEMPLATE.md)
- Description and related issues
- Type of change
- Testing details
- Code quality checklist
- Deployment notes

## Environment Variables & Secrets

### Required GitHub Secrets
```
SONAR_TOKEN          - SonarCloud authentication
GITHUB_TOKEN         - Default (auto-provided)
```

### Codecov Configuration
- Automatic upload of coverage reports
- Coverage tracking on PRs
- Coverage badges available

## Branch Protection Rules

Recommended branch protection settings for `main`:

```yaml
Require status checks to pass before merging:
  - test (Java 11, 17, 21)
  - code-quality
  - lint

Require code reviews before merging:
  - Require 1 review
  - Dismiss stale reviews
  - Require review from code owners

Other protections:
  - Require branches to be up to date
  - Require conversation resolution
  - Include administrators
```

## Workflow Triggers

### On Every Push to main/develop
-  CI (test + quality)
-  Lint (code style + security)
-  SonarCloud analysis

### On Every Pull Request
-  CI (test + quality)
-  Lint (code style + security)
-  Coverage reporting

### On Git Tag (v*.*.*)
-  Release build
-  GitHub Release creation
-  JavaDoc generation

### On Dependabot PR
-  Auto-merge and approve (minor/patch updates)

## Test Matrix

Java versions tested:
-  Java 11 (LTS, minimum)
-  Java 17 (LTS)
-  Java 21 (LTS, latest)

Each version:
- Compiles successfully
- Passes all unit tests
- Generates coverage report
- Uploads to Codecov

## Coverage Reporting

### Codecov Integration
- Automatic upload from all CI runs
- Per-commit coverage tracking
- Pull request coverage reports
- Coverage badges

### Local Coverage Check
```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

## Release Process

### Creating a Release

1. **Tag the commit**
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

2. **GitHub Actions automatically**
   - Builds release JAR
   - Creates GitHub Release
   - Generates release notes
   - Publishes JavaDoc

3. **Manual steps**
   - Review auto-generated release notes
   - Edit release description if needed
   - Create release announcement

### Release Artifacts
- JAR file: `mcp-users-server-1.0.0.jar`
- JavaDoc: GitHub Pages
- Release notes: GitHub Release page

## Dependency Updates

### Automatic Updates
- Maven: Weekly (Monday 3 AM)
- GitHub Actions: Weekly (Monday 4 AM)

### Approval Process
1. Dependabot creates PR
2. Tests run automatically
3. PR is auto-approved
4. PR is auto-merged (squash)

### Manual Major Updates
- Major version updates create PR but don't auto-merge
- Requires manual review and approval
- Run tests before approving

## Monitoring & Alerts

### GitHub Actions Status
- Check workflow status: Actions tab
- View logs: Click workflow run
- Download artifacts: Artifacts section

### Coverage Tracking
- View coverage on Codecov
- Badge integration in README
- Coverage thresholds (optional)

### SonarCloud Monitoring
- View quality metrics
- Track bugs and vulnerabilities
- Monitor code smells
- Track technical debt

## Troubleshooting

### Tests Failing Locally But Passing in CI
- Clean build: `mvn clean`
- Check Java version: `java -version`
- Verify dependencies: `mvn dependency:resolve`

### CI Passing But Tests Failing
- Usually indicates environment difference
- Check Java version matrix
- Review test logs in Actions

### Coverage Report Not Uploading
- Verify Codecov token
- Check GITHUB_TOKEN permissions
- Ensure jacoco.xml is generated

### Release Not Creating
- Verify tag format: `v*` (e.g., v1.0.0)
- Check git tag push: `git push origin v1.0.0`
- Review release workflow logs

## Performance Optimization

### Build Caching
- Maven cache enabled: `cache: maven`
- Speeds up dependency resolution
- Significant time savings on CI runs

### Parallel Testing
- Multiple Java versions test in parallel
- Reduced total CI execution time
- Comprehensive coverage maintained

### Artifact Retention
- PR artifacts: 30 days
- Release artifacts: 90 days
- Automatic cleanup after retention

## Security Considerations

### Dependency Security
- OWASP Dependency Check scans
- Vulnerable version detection
- Automated update PRs

### Secret Management
- GitHub Secrets for tokens
- No secrets in code or logs
- Rotation recommended quarterly

### Code Review
- Required for main branch
- Codeowners review enforcement
- Branch protection enabled

## Integration with External Services

### SonarCloud
- Automatic analysis on push
- PR decorations for changes
- Quality gates enforcement

### Codecov
- Coverage tracking
- Trend analysis
- PR coverage reports

### GitHub Pages
- JavaDoc hosted automatically
- Custom domain support
- Automatic deployment on release

## Custom Configuration

### Adding New Checks
Edit `.github/workflows/ci.yml` and add step:
```yaml
- name: Custom Check
  run: your-command-here
```

### Changing Test Matrix
Edit `strategy.matrix.java-version`:
```yaml
java-version: [ '11', '17', '21' ]
```

### Updating Dependencies
- Edit `pom.xml` directly
- Or use Dependabot PRs
- Test all changes

## Best Practices

1. **Always use tags for releases**: Ensures release workflow triggers
2. **Keep main branch protected**: Requires passing tests
3. **Review Dependabot PRs**: Even with auto-merge enabled
4. **Monitor coverage trends**: Prevent regressions
5. **Update actions regularly**: Security and features
6. **Document breaking changes**: In release notes
7. **Test locally before push**: Faster feedback loop

## Links & Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [Codecov Documentation](https://docs.codecov.io/)
- [OWASP Dependency Check](https://jeremylong.github.io/DependencyCheck/)
- [Maven Documentation](https://maven.apache.org/guides/)
